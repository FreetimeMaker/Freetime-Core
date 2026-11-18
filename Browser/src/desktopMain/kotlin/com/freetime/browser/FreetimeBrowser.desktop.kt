package com.freetime.browser

import java.awt.Desktop
import java.net.URI
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual object FreetimeBrowser {
    actual fun openExternal(url: String): Result<Unit> = runCatching {
        check(Desktop.isDesktopSupported()) { "Desktop integration is unavailable" }
        val desktop = Desktop.getDesktop()
        check(desktop.isSupported(Desktop.Action.BROWSE)) { "No desktop browser is available" }
        desktop.browse(URI(url))
    }
    actual fun isAllowed(url: String, options: BrowserOptions): Boolean = allowedBrowserUrl(url, options)
    actual fun open(url: String, options: BrowserOptions, openInApp: ((String) -> Boolean)?): Result<Unit> =
        routeBrowserUrl(url, options, openInApp, ::openExternal)
}

internal actual fun allowedBrowserUrl(url: String, options: BrowserOptions): Boolean = runCatching {
    val uri = URI(url)
    val host = uri.host ?: return@runCatching false
    host.isNotBlank() && (uri.scheme.equals("https", true) || (options.allowHttp && uri.scheme.equals("http", true))) &&
        (options.allowedHosts.isEmpty() || options.allowedHosts.any { it.equals(host, true) })
}.getOrDefault(false)

@Composable
actual fun rememberBrowserLauncher(): BrowserLauncher = remember { BrowserLauncher(FreetimeBrowser::openExternal) }
