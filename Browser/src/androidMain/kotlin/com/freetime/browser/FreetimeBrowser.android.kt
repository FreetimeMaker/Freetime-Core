package com.freetime.browser

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URI

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable

actual object FreetimeBrowser {
    actual fun openExternal(url: String): Result<Unit> = Result.failure(
        IllegalStateException("Android requires a Context; use openExternal(context, url) or rememberBrowserLauncher().")
    )
    actual fun isAllowed(url: String, options: BrowserOptions): Boolean = allowedBrowserUrl(url, options)
    actual fun open(url: String, options: BrowserOptions, openInApp: ((String) -> Boolean)?): Result<Unit> =
        routeBrowserUrl(url, options, openInApp, ::openExternal)

    fun openExternal(context: Context, url: String): Result<Unit> = runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
    fun open(context: Context, url: String, options: BrowserOptions = BrowserOptions(), openInApp: ((String) -> Boolean)? = null): Result<Unit> =
        routeBrowserUrl(url, options, openInApp) { openExternal(context, it) }
}

internal actual fun allowedBrowserUrl(url: String, options: BrowserOptions): Boolean = runCatching {
    val uri = URI(url)
    val host = uri.host ?: return@runCatching false
    host.isNotBlank() && (uri.scheme.equals("https", true) || (options.allowHttp && uri.scheme.equals("http", true))) &&
        (options.allowedHosts.isEmpty() || options.allowedHosts.any { it.equals(host, true) })
}.getOrDefault(false)

@Composable
actual fun rememberBrowserLauncher(): BrowserLauncher {
    val context = LocalContext.current.applicationContext
    return androidx.compose.runtime.remember(context) { BrowserLauncher { FreetimeBrowser.openExternal(context, it) } }
}
