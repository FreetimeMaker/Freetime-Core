package com.freetime.design.templates.expressiveglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.freetime.design.templates.fullglass.FullGlassScreen
import com.freetime.design.templates.fullglass.MaterialNavigation

/** Liquid Glass content paired with an opaque Material 3 Expressive short navigation bar. */
@Composable
fun MaterialNavigationTemplate(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
) {
    FreetimeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        FullGlassScreen(title = "Glass + Expressive", expressive = true, modifier = modifier) { selected, onSelect ->
            MaterialNavigation(selectedIndex = selected, onItemSelected = onSelect, expressive = true)
        }
    }
}

@Preview(name = "Glass + Expressive Navigation · Light", showBackground = true)
@Composable
private fun MaterialNavigationLightPreview() { MaterialNavigationTemplate(darkTheme = false) }

@Preview(name = "Glass + Expressive Navigation · Dark", showBackground = true)
@Composable
private fun MaterialNavigationDarkPreview() { MaterialNavigationTemplate(darkTheme = true) }
