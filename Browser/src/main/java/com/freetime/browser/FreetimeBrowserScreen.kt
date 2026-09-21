package com.freetime.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.freetime.design.FreetimeButton
import com.freetime.design.FreetimeText
import com.freetime.design.FreetimeTopBar

@Immutable
data class FreetimeBrowserState(
    val url: String,
    val title: String? = null,
    val loading: Boolean = true,
    val canGoBack: Boolean = false,
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FreetimeBrowserScreen(
    initialUrl: String,
    modifier: Modifier = Modifier,
    options: BrowserOptions = BrowserOptions(mode = BrowserMode.IN_APP),
    title: String = "Browser",
    javaScriptEnabled: Boolean = false,
    onClose: () -> Unit = {},
    onExternalNavigation: ((String) -> Unit)? = null,
) {
    var state by remember(initialUrl) { mutableStateOf(FreetimeBrowserState(initialUrl)) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    Column(modifier.fillMaxSize()) {
        FreetimeTopBar(
            title = state.title?.takeIf { it.isNotBlank() } ?: title,
            navigation = {
                FreetimeButton(
                    text = if (state.canGoBack) "Back" else "Close",
                    onClick = {
                        val view = webView
                        if (view != null && view.canGoBack()) view.goBack() else onClose()
                    },
                )
            },
            actions = {
                FreetimeText(if (state.loading) "Loading…" else "")
            },
        )
        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = javaScriptEnabled
                    settings.domStorageEnabled = javaScriptEnabled
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val requested = request?.url?.toString() ?: return false
                            val allowed = FreetimeBrowser.isAllowed(requested, options)
                            if (!allowed) onExternalNavigation?.invoke(requested)
                            return !allowed
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            state = state.copy(url = url ?: state.url, loading = true, canGoBack = view?.canGoBack() == true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            state = state.copy(
                                url = url ?: state.url,
                                title = view?.title,
                                loading = false,
                                canGoBack = view?.canGoBack() == true,
                            )
                        }
                    }
                    webView = this
                    if (FreetimeBrowser.isAllowed(initialUrl, options)) loadUrl(initialUrl)
                }
            },
            update = { webView = it },
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }
}
