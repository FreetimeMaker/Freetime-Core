package com.freetime.browser

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Immutable
data class FreetimeBrowserState(
    val url: String,
    val title: String? = null,
    val loading: Boolean = true,
    val canGoBack: Boolean = false,
)

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
    var back by remember { mutableStateOf<() -> Unit>({}) }

    Column(modifier.fillMaxSize()) {
        Surface(tonalElevation = 2.dp) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        if (state.canGoBack) back() else onClose()
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

        BrowserContent(initialUrl, Modifier.weight(1f).fillMaxWidth(), options, javaScriptEnabled, state, { state = it }, { back = it }, onExternalNavigation)
    }
}

fun interface BrowserLauncher {
    fun openExternal(url: String): Result<Unit>

    fun open(
        url: String,
        options: BrowserOptions = BrowserOptions(),
        openInApp: ((String) -> Boolean)? = null,
    ): Result<Unit> = routeBrowserUrl(url, options, openInApp, ::openExternal)
}

@Composable
expect fun rememberBrowserLauncher(): BrowserLauncher

@Composable
internal expect fun BrowserContent(initialUrl: String, modifier: Modifier, options: BrowserOptions, javaScriptEnabled: Boolean, state: FreetimeBrowserState, onState: (FreetimeBrowserState) -> Unit, onBackHandler: (() -> Unit) -> Unit, onExternalNavigation: ((String) -> Unit)?)
