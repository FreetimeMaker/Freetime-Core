package com.freetime.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Local notifications backed by public-source polling; no push service is required. */
object FreetimeNotifications {
    private const val PERIODIC_WORK = "com.freetime.notifications.poll"
    private const val IMMEDIATE_WORK = "com.freetime.notifications.check-now"

    /** Call from an Application-owned coroutine. Settings and sources survive process death. */
    suspend fun initialize(context: Context, configuration: NotificationConfiguration) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        NotificationRuntime.mutex.withLock {
            require(app.resources.getResourceTypeName(configuration.smallIcon) in setOf("drawable", "mipmap")) {
                "smallIcon must be a drawable or mipmap resource"
            }
            NotificationRuntime.invalidate()
            val store = NotificationStore(app)
            store.saveConfiguration(configuration)
            NotificationPoster(app).ensureChannels(configuration)
            schedule(app, store, configuration)
        }
        requestCheck(app)
    }

    suspend fun initialize(context: Context, configure: NotificationConfigurationBuilder.() -> Unit) {
        initialize(context, NotificationConfigurationBuilder().apply(configure).build())
    }

    /** Performs a foreground check now, off the main thread; failures are returned per source. */
    suspend fun checkNow(context: Context): NotificationCheckResult = withContext(Dispatchers.IO) {
        NotificationChecker(context.applicationContext).check()
    }

    /** Enqueues a best-effort one-time check, subject to Android scheduling and connectivity. */
    fun requestCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<NotificationPollingWorker>()
            .setConstraints(networkConstraints())
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            IMMEDIATE_WORK, ExistingWorkPolicy.REPLACE, request,
        )
    }

    /** A user override persists across initialize calls; Android channel settings still take precedence. */
    suspend fun setCategoryEnabled(context: Context, category: NotificationCategory, enabled: Boolean) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        NotificationRuntime.mutex.withLock {
            NotificationRuntime.invalidate()
            val store = NotificationStore(app)
            store.setCategoryEnabled(category, enabled)
            if (!enabled) NotificationPoster(app).cancelCategory(category)
            store.configuration()?.let { schedule(app, store, it) }
        }
        if (enabled) requestCheck(app)
    }

    suspend fun isCategoryEnabled(context: Context, category: NotificationCategory): Boolean {
        val store = NotificationStore(context.applicationContext)
        val configuration = store.configuration() ?: return false
        return store.isCategoryEnabled(category, configuration)
    }

    /** Does not prompt. The host Activity requests POST_NOTIFICATIONS on Android 13+. */
    fun hasPermission(context: Context): Boolean = NotificationPoster(context.applicationContext).hasPermission()

    /** Stops checks without forgetting delivered IDs or the user's category choices. */
    suspend fun stop(context: Context) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        NotificationRuntime.mutex.withLock {
            NotificationRuntime.invalidate()
            NotificationStore(app).clearConfiguration()
            val work = WorkManager.getInstance(app)
            work.cancelUniqueWork(PERIODIC_WORK)
            work.cancelUniqueWork(IMMEDIATE_WORK)
        }
    }

    private suspend fun schedule(context: Context, store: NotificationStore, configuration: NotificationConfiguration) {
        val anySourceEnabled = configuration.sources.any { source ->
            store.isCategoryEnabled(source.category, configuration) ||
                (source is GitHubReleaseSource && store.isCategoryEnabled(NotificationCategory.CHANGELOGS, configuration))
        }
        val work = WorkManager.getInstance(context)
        if (!anySourceEnabled) {
            work.cancelUniqueWork(PERIODIC_WORK)
            work.cancelUniqueWork(IMMEDIATE_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<NotificationPollingWorker>(
            configuration.checkIntervalMinutes, TimeUnit.MINUTES,
        ).setConstraints(networkConstraints()).build()
        work.enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun networkConstraints() = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
}

class NotificationConfigurationBuilder {
    var smallIcon: Int = 0
    var checkIntervalMinutes: Long = 360
    var notifyOnFirstCheck: Boolean = false
    var maxNotificationsPerCheck: Int = 5
    private val sources = mutableListOf<NotificationSource>()
    private val enabled = NotificationCategory.entries.toMutableSet()
    private val channels = mutableMapOf<NotificationCategory, NotificationChannelOptions>()

    fun github(owner: String, repository: String, includePrereleases: Boolean = false) {
        sources += GitHubReleaseSource(owner, repository, includePrereleases)
    }

    fun fdroid(packageName: String, apiUrl: String? = null) {
        sources += FDroidVersionSource(packageName, apiUrl)
    }

    fun developerMessages(vararg categories: String) {
        sources += DeveloperBlogSource(categories.toSet())
    }

    fun source(source: NotificationSource) { sources += source }

    fun category(category: NotificationCategory, enabled: Boolean = true, options: NotificationChannelOptions? = null) {
        if (enabled) this.enabled += category else this.enabled -= category
        options?.let { channels[category] = it }
    }

    fun build() = NotificationConfiguration(
        smallIcon, sources.toList(), checkIntervalMinutes, enabled.toSet(), channels.toMap(),
        notifyOnFirstCheck, maxNotificationsPerCheck,
    )
}
