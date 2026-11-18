package com.freetime.notifications

import java.nio.file.Path
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * In-process polling. The host supplies its installed version and a private, app-specific directory.
 * Use exactly one instance per directory. Call close() on application shutdown.
 */
class DesktopFreetimeNotifications(
    storageDirectory: Path,
    private val installedVersionName: String,
    private val installedVersionCode: Long = 0,
    appName: String = "Freetime",
) : NotificationService, AutoCloseable {
    private val store = DesktopNotificationStore(storageDirectory)
    private val poster = DesktopNotificationPoster(appName)
    private val runtime = NotificationRuntimeState()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var polling: Job? = null
    @Volatile private var closed = false
    private val logger = Logger.getLogger(DesktopFreetimeNotifications::class.java.name)

    override suspend fun initialize(configuration: NotificationConfiguration) = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            check(!closed) { "Notification service is closed" }
            runtime.invalidate()
            store.saveConfiguration(configuration)
            startPolling(configuration)
        }
    }

    suspend fun initialize(configure: NotificationConfigurationBuilder.() -> Unit) =
        initialize(NotificationConfigurationBuilder().apply(configure).build())

    /** Reopens persisted configuration and starts polling immediately; false if never initialized/stopped. */
    suspend fun resume(): Boolean = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            check(!closed) { "Notification service is closed" }
            val configuration = store.configuration() ?: return@withLock false
            runtime.invalidate()
            startPolling(configuration)
            true
        }
    }

    private fun startPolling(configuration: NotificationConfiguration) {
        polling?.cancel()
        polling = scope.launch {
            while (isActive) {
                try {
                    val result = checkNow()
                    if (result.failures.isNotEmpty()) logger.warning("Notification check: ${result.failures.size} source failure(s)")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    logger.log(Level.WARNING, "Notification polling failed; will retry next interval", error)
                }
                delay(configuration.checkIntervalMinutes.coerceAtMost(Long.MAX_VALUE / 60_000L) * 60_000L)
            }
        }
    }

    override suspend fun checkNow(): NotificationCheckResult = withContext(Dispatchers.IO) {
        check(!closed) { "Notification service is closed" }
        NotificationEngine(store, poster, installedVersionName, installedVersionCode, runtime).check()
    }

    fun requestCheck() {
        check(!closed) { "Notification service is closed" }
        scope.launch {
            try { checkNow() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { logger.log(Level.WARNING, "Notification check failed", error) }
        }
    }

    override suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            check(!closed) { "Notification service is closed" }
            runtime.invalidate()
            store.setCategoryEnabled(category, enabled)
            if (!enabled) poster.cancelCategory(category)
            store.configuration()?.let { startPolling(it) }
        }
    }

    override suspend fun isCategoryEnabled(category: NotificationCategory): Boolean = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            val configuration = store.configuration() ?: return@withLock false
            store.isCategoryEnabled(category, configuration)
        }
    }
    override fun hasPermission(): Boolean = !closed && poster.hasPermission()

    override suspend fun stop() = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            runtime.invalidate()
            polling?.cancel()
            polling = null
            store.clearConfiguration()
        }
    }

    /** Releases resources without blocking a UI thread; retains configuration for resume(). */
    suspend fun shutdown() = withContext(Dispatchers.IO) {
        runtime.mutex.withLock {
            closed = true
            runtime.invalidate()
            scope.cancel()
        }
        poster.close()
    }

    /** Blocking shutdown for non-AWT threads. UI hosts should launch shutdown() in their app scope. */
    override fun close() {
        check(!java.awt.EventQueue.isDispatchThread()) { "Use suspending shutdown() from the AWT UI thread" }
        kotlinx.coroutines.runBlocking { shutdown() }
    }
}
