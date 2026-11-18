package com.freetime.notifications

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

class NotificationLifecycleTest {
    @Test fun lifecycleChangesDoNotWaitForTheNetworkLock() = runBlocking {
        val active = Job()
        NotificationRuntime.checkMutex.lock()
        try {
            val original = NotificationRuntime.mutex.withLock {
                NotificationRuntime.activeCheck = active
                NotificationRuntime.generation
            }
            withTimeout(1_000) {
                NotificationRuntime.mutex.withLock { NotificationRuntime.invalidate() }
            }
            assertTrue(active.isCancelled)
            NotificationRuntime.mutex.withLock {
                assertThrows(CancellationException::class.java) { NotificationRuntime.requireCurrent(original) }
                NotificationRuntime.requireCurrent(NotificationRuntime.generation)
            }
        } finally {
            NotificationRuntime.mutex.withLock { NotificationRuntime.activeCheck = null }
            NotificationRuntime.checkMutex.unlock()
        }
    }
}
