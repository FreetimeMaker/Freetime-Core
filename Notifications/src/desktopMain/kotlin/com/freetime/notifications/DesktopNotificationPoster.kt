package com.freetime.notifications

import java.awt.Desktop
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage
import java.net.URI

internal class DesktopNotificationPoster(private val appName: String) : NotificationDelivery, AutoCloseable {
    private val icons = mutableMapOf<NotificationCategory, TrayIcon>()
    private fun <T> onEdt(action: () -> T): T {
        if (EventQueue.isDispatchThread()) return action()
        val task = java.util.concurrent.FutureTask(java.util.concurrent.Callable { action() })
        EventQueue.invokeAndWait(task)
        return task.get()
    }

    override fun hasPermission(): Boolean = !GraphicsEnvironment.isHeadless() && SystemTray.isSupported()
    override fun canPost(category: NotificationCategory): Boolean = hasPermission()
    override fun ensureChannels(configuration: NotificationConfiguration) = Unit

    override fun post(configuration: NotificationConfiguration, category: NotificationCategory, sourceId: String, entry: NotificationEntry): Boolean {
        if (!hasPermission()) return false
        return runCatching {
            onEdt {
                val icon = icons.getOrPut(category) {
                    val image = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
                    image.createGraphics().apply {
                        color = java.awt.Color(48, 113, 190)
                        fillOval(2, 2, 28, 28)
                        color = java.awt.Color.WHITE
                        drawString("F", 12, 21)
                        dispose()
                    }
                    TrayIcon(image, appName).apply {
                        isImageAutoSize = true
                        SystemTray.getSystemTray().add(this)
                    }
                }
                icon.actionListeners.forEach { icon.removeActionListener(it) }
                entry.url?.takeIf { runCatching { requireHttpsUrl(it) }.isSuccess }?.let { url ->
                    icon.addActionListener {
                        runCatching {
                            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                Desktop.getDesktop().browse(URI(url))
                            }
                        }
                    }
                }
                val title = if (category == NotificationCategory.CHANGELOGS) "Changelog: ${entry.title}" else entry.title
                icon.displayMessage(title.take(200), entry.body.take(8_000), TrayIcon.MessageType.INFO)
            }
        }.isSuccess
    }

    override fun cancelCategory(category: NotificationCategory) {
        if (hasPermission()) onEdt { icons.remove(category)?.let { SystemTray.getSystemTray().remove(it) }; Unit }
    }
    override fun close() {
        if (hasPermission()) onEdt {
            icons.values.forEach { SystemTray.getSystemTray().remove(it) }
            icons.clear()
        }
    }
}
