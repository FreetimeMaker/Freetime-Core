package com.freetime.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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
        Surface(tonalElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        val view = webView
                        if (view != null && view.canGoBack()) view.goBack() else onClose()
                    },
                ) {
                    Text(if (state.canGoBack) "Back" else "Close")
                }

                Text(
                    text = state.title?.takeIf { it.isNotBlank() } ?: title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )

                if (state.loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
        }

        AndroidView(
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
                            val allowed = FreetimeBrowser.isAllowed(requested, options)
                            if (!allowed) onExternalNavigation?.invoke(requested)
                            return !allowed
                        }

                        override fun onPageStarted(
                            view: WebView?,
                            url: String?,
                            favicon: Bitmap?,
                        ) {
                            state = state.copy(
                                url = url ?: state.url,
                                loading = true,
                                canGoBack = view?.canGoBack() == true,
                            )
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
