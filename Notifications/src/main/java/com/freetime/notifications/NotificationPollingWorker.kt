package com.freetime.notifications

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import java.io.IOException

/** WorkManager can recreate this worker using only persisted configuration after process death. */
@Keep
class NotificationPollingWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = try {
        val result = FreetimeNotifications.checkNow(applicationContext)
        for ((source, message) in result.failures) {
            val sourceLabel = notificationKey(NotificationCategory.UPDATES, source, "log").takeLast(12)
            Log.w("FreetimeNotifications", "Check failed for source $sourceLabel: $message")
        }
        if (result.failures.isNotEmpty() && runAttemptCount < 3) Result.retry() else Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: IOException) {
        Log.w("FreetimeNotifications", "Notification state could not be read", error)
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    } catch (error: IllegalArgumentException) {
        Log.e("FreetimeNotifications", "Invalid persisted notification configuration; initialize the SDK again")
        Result.failure()
    }
}
