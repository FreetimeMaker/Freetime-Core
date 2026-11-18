package com.freetime.browser

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ExternalBrowserContent(initialUrl: String, modifier: Modifier, options: BrowserOptions, javaScriptEnabled: Boolean, state: FreetimeBrowserState, onState: (FreetimeBrowserState) -> Unit, onBackHandler: (() -> Unit) -> Unit, onExternalNavigation: ((String) -> Unit)?) {
    var error by remember(initialUrl) { mutableStateOf<String?>(null) }
    val allowed = FreetimeBrowser.isAllowed(initialUrl, options)
    LaunchedEffect(initialUrl, options) { onState(FreetimeBrowserState(initialUrl, loading = false)) }
    Column(modifier.padding(16.dp)) {
        Text("Web pages open in your system browser on desktop.")
        Text(initialUrl)
        if (!allowed) Text("This URL is not allowed by BrowserOptions.")
        TextButton(enabled = allowed, onClick = {
            error = FreetimeBrowser.openExternal(initialUrl).exceptionOrNull()?.let { it.message ?: it.toString() }
        }) { Text("Open in browser") }
        error?.let { Text(it) }
    }
}
