package com.freetime.notifications

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

internal class NotificationChecker(private val context: Context) {
    suspend fun check(): NotificationCheckResult {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return NotificationEngine(
            NotificationStore(context), NotificationPoster(context), info.versionName.orEmpty(),
            PackageInfoCompat.getLongVersionCode(info), NotificationRuntime,
        ).check()
    }
}
