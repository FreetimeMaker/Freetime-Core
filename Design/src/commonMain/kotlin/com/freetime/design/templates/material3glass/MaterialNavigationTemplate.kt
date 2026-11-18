package com.freetime.design.templates.material3glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.freetime.design.templates.fullglass.FullGlassScreen
import com.freetime.design.templates.fullglass.MaterialNavigation

/** Liquid Glass content paired with an opaque Material 3 navigation bar. */
@Composable
fun MaterialNavigationTemplate(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
) {
    FreetimeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        FullGlassScreen(title = "Glass + Material 3", modifier = modifier) { selected, onSelect ->
            MaterialNavigation(selectedIndex = selected, onItemSelected = onSelect)
        }
    }
}

@Preview(name = "Glass + Material 3 Navigation · Light", showBackground = true)
@Composable
private fun MaterialNavigationLightPreview() { MaterialNavigationTemplate(darkTheme = false) }

@Preview(name = "Glass + Material 3 Navigation · Dark", showBackground = true)
@Composable
private fun MaterialNavigationDarkPreview() { MaterialNavigationTemplate(darkTheme = true) }
