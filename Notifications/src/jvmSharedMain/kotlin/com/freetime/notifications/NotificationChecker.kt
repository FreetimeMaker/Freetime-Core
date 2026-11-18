package com.freetime.notifications

import com.freetime.core.FreetimeCore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal object NotificationRuntime : NotificationRuntimeState()

internal open class NotificationRuntimeState {
    val checkMutex = Mutex()
    // Lifecycle operations take this short-lived lock, never the network/check lock.
    val mutex = Mutex()
    var generation = 0L
    var activeCheck: Job? = null

    fun invalidate() {
        generation++
        activeCheck?.cancel(CancellationException("Notification configuration changed"))
    }

    fun requireCurrent(expected: Long) {
        if (generation != expected) throw CancellationException("Notification configuration changed")
    }
}

internal class NotificationEngine(
    private val store: NotificationStateStore,
    private val poster: NotificationDelivery,
    private val installedName: String,
    private val installedCode: Long,
    private val runtime: NotificationRuntimeState,
    private val fetcher: SourceFetcher = SourceFetcher(HttpUrlConnectionClient("${FreetimeCore.SDK_NAME}/${FreetimeCore.SDK_VERSION}")),
) {
    suspend fun check(): NotificationCheckResult = runtime.checkMutex.withLock {
        coroutineScope {
            val job = currentCoroutineContext()[Job]!!
            val snapshot = runtime.mutex.withLock {
                val configuration = store.configuration() ?: return@withLock null
                runtime.activeCheck = job
                configuration to runtime.generation
            } ?: return@coroutineScope NotificationCheckResult(0, 0, emptyMap(), poster.hasPermission())
            val (configuration, generation) = snapshot
            try {
                poster.ensureChannels(configuration)
                val failures = linkedMapOf<String, String>()
                var posted = 0
                var checked = 0
                for (source in configuration.sources) {
                    currentCoroutineContext().ensureActive()
                    val categories = if (source is GitHubReleaseSource) {
                        listOf(NotificationCategory.UPDATES, NotificationCategory.CHANGELOGS)
                    } else listOf(source.category)
                    val enabled = runtime.mutex.withLock {
                        runtime.requireCurrent(generation)
                        categories.filter { store.isCategoryEnabled(it, configuration) }
                    }
                    if (enabled.isEmpty()) continue
                    val entries = try {
                        fetcher.fetch(source).distinctBy { it.id }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (error: Exception) {
                        failures[source.id] = error.message ?: error.javaClass.simpleName
                        continue
                    }
                    checked++
                    for (category in enabled) {
                        val stateKey = "${source.id.length}:${source.id}:${category.name}"
                        val previous = runtime.mutex.withLock {
                            runtime.requireCurrent(generation)
                            store.seenIds(stateKey)
                        }
                        val plan = planNotificationBatch(
                            entries, previous, configuration.notifyOnFirstCheck, category,
                            installedName, installedCode,
                        )
                        val seen = plan.seen.toMutableSet()
                        suspend fun saveSeen() {
                            val currentSeen = entries.map { it.id }.filter { it in seen }
                            store.setSeenIds(stateKey, (currentSeen + seen).take(500).toSet())
                        }
                        runtime.mutex.withLock {
                            runtime.requireCurrent(generation)
                            saveSeen()
                        }
                        for (entry in plan.pending) {
                            if (posted >= configuration.maxNotificationsPerCheck || !poster.canPost(category)) break
                            val message = if (source is DeveloperBlogSource) {
                                try {
                                    fetcher.blogMessage(source, entry)
                                } catch (cancelled: CancellationException) {
                                    throw cancelled
                                } catch (_: Exception) {
                                    // A broken detail endpoint must not starve the feed or other sources.
                                    failures[source.id] = "Full blog text unavailable; delivered the post headline instead"
                                    entry.copy(body = "Open the post to read the developer's message.")
                                }
                            } else entry
                            currentCoroutineContext().ensureActive()
                            runtime.mutex.withLock {
                                runtime.requireCurrent(generation)
                                if (store.isCategoryEnabled(category, configuration) &&
                                    poster.post(configuration, category, source.id, message)) {
                                    seen.add(entry.id)
                                    posted++
                                    // Finish the acknowledgement even if this run is cancelled immediately after notify().
                                    withContext(NonCancellable) { saveSeen() }
                                }
                            }
                        }
                    }
                }
                NotificationCheckResult(checked, posted, failures, poster.hasPermission())
            } finally {
                withContext(NonCancellable) {
                    runtime.mutex.withLock {
                        if (runtime.activeCheck === job) runtime.activeCheck = null
                    }
                }
            }
        }
    }
}

internal data class NotificationBatchPlan(val seen: Set<String>, val pending: List<NotificationEntry>)

internal fun planNotificationBatch(
    entries: List<NotificationEntry>,
    previous: Set<String>?,
    notifyOnFirstCheck: Boolean,
    category: NotificationCategory,
    installedName: String,
    installedCode: Long,
): NotificationBatchPlan {
    if (previous == null && !notifyOnFirstCheck) return NotificationBatchPlan(entries.map { it.id }.toSet(), emptyList())
    val seen = previous.orEmpty().toMutableSet()
    val pending = mutableListOf<NotificationEntry>()
    for (entry in entries.distinctBy { it.id }) {
        if (entry.id in seen) continue
        val releaseCategory = category == NotificationCategory.UPDATES || category == NotificationCategory.CHANGELOGS
        if ((releaseCategory && !shouldNotifyRelease(entry, installedName, installedCode)) ||
            (category == NotificationCategory.CHANGELOGS && entry.body.isBlank())) {
            seen.add(entry.id)
        } else {
            pending.add(entry)
        }
    }
    return NotificationBatchPlan(seen, pending)
}
