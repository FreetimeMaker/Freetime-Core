package com.freetime.design.templates.expressiveglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.freetime.design.LocalIsDarkTheme
import com.freetime.design.ProvideFloatingBottomNavigationGlass

// Liquid Glass is confined to the floating navigation.
@Composable
fun FreetimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    AppTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
            ProvideFloatingBottomNavigationGlass(enabled = true, content = content)
        }
    }
}
