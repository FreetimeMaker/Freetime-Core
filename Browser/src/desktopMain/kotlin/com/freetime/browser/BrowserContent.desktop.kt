package com.freetime.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun BrowserContent(initialUrl: String, modifier: Modifier, options: BrowserOptions, javaScriptEnabled: Boolean, state: FreetimeBrowserState, onState: (FreetimeBrowserState) -> Unit, onBackHandler: (() -> Unit) -> Unit, onExternalNavigation: ((String) -> Unit)?) {
    ExternalBrowserContent(initialUrl, modifier, options, javaScriptEnabled, state, onState, onBackHandler, onExternalNavigation)
}
