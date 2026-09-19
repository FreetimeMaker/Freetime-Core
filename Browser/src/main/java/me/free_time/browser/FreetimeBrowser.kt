package me.free_time.browser

import android.content.Context
import android.content.Intent
import android.net.Uri

enum class BrowserMode { EXTERNAL, IN_APP }

data class BrowserOptions(
    val mode: BrowserMode = BrowserMode.EXTERNAL,
    val allowExternalFallback: Boolean = true,
)

object FreetimeBrowser {
    fun openExternal(context: Context, url: String): Result<Unit> = runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * IN_APP is intentionally host-driven so the SDK does not force a WebView implementation.
     * Return false from [openInApp] to allow the external fallback.
     */
    fun open(
        context: Context,
        url: String,
        options: BrowserOptions = BrowserOptions(),
        openInApp: ((String) -> Boolean)? = null,
    ): Result<Unit> {
        if (options.mode == BrowserMode.IN_APP && openInApp?.invoke(url) == true) return Result.success(Unit)
        return if (options.mode == BrowserMode.EXTERNAL || options.allowExternalFallback) openExternal(context, url)
        else Result.failure(IllegalStateException("No in-app browser handled the URL"))
    }
}
