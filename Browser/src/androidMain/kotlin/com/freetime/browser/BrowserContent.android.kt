package com.freetime.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal actual fun BrowserContent(initialUrl: String, modifier: Modifier, options: BrowserOptions, javaScriptEnabled: Boolean, state: FreetimeBrowserState, onState: (FreetimeBrowserState) -> Unit, onBackHandler: (() -> Unit) -> Unit, onExternalNavigation: ((String) -> Unit)?) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val currentState by androidx.compose.runtime.rememberUpdatedState(state)
    val currentOptions by androidx.compose.runtime.rememberUpdatedState(options)
    val currentExternal by androidx.compose.runtime.rememberUpdatedState(onExternalNavigation)
    val goBack: () -> Unit = remember { { webView?.goBack() } }
    androidx.compose.runtime.SideEffect { onBackHandler(goBack) }
    androidx.compose.runtime.key(initialUrl) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = javaScriptEnabled
                    settings.domStorageEnabled = javaScriptEnabled
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val requested = request?.url?.toString() ?: return false
                            val allowed = FreetimeBrowser.isAllowed(requested, currentOptions)
                            if (!allowed) currentExternal?.invoke(requested)
                            return !allowed
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?,
                        ) {
                            onState(currentState.copy(
                                url = url ?: currentState.url,
                                loading = true,
                                canGoBack = view?.canGoBack() == true,
                            ))
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            onState(currentState.copy(
                                url = url ?: currentState.url,
                                title = view?.title,
                                loading = false,
                                canGoBack = view?.canGoBack() == true,
                            ))
                        }
                    }
                    webView = this
                    if (FreetimeBrowser.isAllowed(initialUrl, options)) loadUrl(initialUrl)
                    else onState(FreetimeBrowserState(initialUrl, loading = false))
                }
            },
            update = {
                webView = it
                it.settings.javaScriptEnabled = javaScriptEnabled
                it.settings.domStorageEnabled = javaScriptEnabled
            },
        )
    }
    DisposableEffect(webView) {
        val view = webView
        onDispose { view?.stopLoading(); view?.destroy() }
    }
}
