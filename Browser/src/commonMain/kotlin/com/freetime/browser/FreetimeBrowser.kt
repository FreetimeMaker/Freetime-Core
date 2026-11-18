package com.freetime.browser

enum class BrowserMode { EXTERNAL, IN_APP }

data class BrowserOptions(
    val mode: BrowserMode = BrowserMode.EXTERNAL,
    val allowExternalFallback: Boolean = true,
    val allowedHosts: Set<String> = emptySet(),
    val allowHttp: Boolean = false,
)

expect object FreetimeBrowser {
    fun openExternal(url: String): Result<Unit>
    fun isAllowed(url: String, options: BrowserOptions): Boolean
    fun open(url: String, options: BrowserOptions = BrowserOptions(), openInApp: ((String) -> Boolean)? = null): Result<Unit>
}

internal expect fun allowedBrowserUrl(url: String, options: BrowserOptions): Boolean

internal fun routeBrowserUrl(
    url: String,
    options: BrowserOptions,
    openInApp: ((String) -> Boolean)?,
    external: (String) -> Result<Unit>,
): Result<Unit> = runCatching {
    require(allowedBrowserUrl(url, options)) { "URL is not allowed by BrowserOptions" }
    if (options.mode == BrowserMode.IN_APP && openInApp?.invoke(url) == true) return@runCatching
    check(options.mode == BrowserMode.EXTERNAL || options.allowExternalFallback) { "No in-app browser handled the URL" }
    external(url).getOrThrow()
}
