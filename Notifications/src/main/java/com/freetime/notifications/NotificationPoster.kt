package com.freetime.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

internal class NotificationPoster(private val context: Context) {
    fun hasPermission(): Boolean =
        (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED) && NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun ensureChannels(configuration: NotificationConfiguration) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        for (category in NotificationCategory.entries) {
            val options = configuration.channels[category] ?: when (category) {
                NotificationCategory.UPDATES -> NotificationChannelOptions("App updates", "New GitHub and F-Droid versions")
                NotificationCategory.CHANGELOGS -> NotificationChannelOptions("Changelogs", "Release notes for new versions")
                NotificationCategory.DEVELOPER_MESSAGES -> NotificationChannelOptions("Developer messages", "New posts from the developer")
            }
            val importance = when (options.importance) {
                1 -> NotificationManager.IMPORTANCE_MIN
                2 -> NotificationManager.IMPORTANCE_LOW
                4 -> NotificationManager.IMPORTANCE_HIGH
                else -> NotificationManager.IMPORTANCE_DEFAULT
            }
            manager.createNotificationChannel(NotificationChannel(channelId(category), options.name, importance).apply {
                description = options.description
            })
        }
    }

    fun post(configuration: NotificationConfiguration, category: NotificationCategory, sourceId: String, entry: NotificationEntry): Boolean {
        if (!canPost(category)) return false
        val tag = notificationKey(category, sourceId, entry.id)
        val title = when (category) {
            NotificationCategory.CHANGELOGS -> "Changelog: ${entry.title}"
            else -> entry.title
        }.take(200)
        val builder = NotificationCompat.Builder(context, channelId(category))
            .setSmallIcon(configuration.smallIcon)
            .setContentTitle(title)
            .setContentText(entry.body.take(300))
            .setStyle(NotificationCompat.BigTextStyle().bigText(entry.body.take(8_000)))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val url = entry.url?.takeIf { runCatching { requireHttpsUrl(it) }.isSuccess }
        val intent = url?.let { Intent(Intent.ACTION_VIEW, it.toUri()) }
            ?: context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            builder.setContentIntent(PendingIntent.getActivity(
                context, tag.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ))
        }
        return try {
            NotificationManagerCompat.from(context).notify(tag, 0, builder.build())
            true
        } catch (_: SecurityException) {
            // Permission may be revoked after the check. Keep the entry pending.
            false
        }
    }

    fun canPost(category: NotificationCategory): Boolean = hasPermission() &&
        (Build.VERSION.SDK_INT < 26 || context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(channelId(category))?.importance != NotificationManager.IMPORTANCE_NONE)

    fun cancelCategory(category: NotificationCategory) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val prefix = "freetime.notifications:${category.name}:"
        for (notification in manager.activeNotifications) {
            if (notification.tag?.startsWith(prefix) == true) manager.cancel(notification.tag, notification.id)
        }
    }

    private fun channelId(category: NotificationCategory) = "freetime.notifications.${category.name.lowercase(java.util.Locale.ROOT)}"
}
