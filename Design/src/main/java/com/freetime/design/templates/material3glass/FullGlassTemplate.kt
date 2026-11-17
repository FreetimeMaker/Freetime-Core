package com.freetime.design.templates.material3glass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.freetime.design.templates.fullglass.FullGlassScreen

/** Complete, themed glass screen; navigation changes only its selection highlight. */
@Composable
fun FullGlassTemplate(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
) {
    FreetimeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        FullGlassScreen(title = "Material 3 Glass", modifier = modifier)
    }
}

@Preview(name = "Full Material 3 Glass · Light", showBackground = true)
@Composable
private fun FullGlassLightPreview() { FullGlassTemplate(darkTheme = false) }

@Preview(name = "Full Material 3 Glass · Dark", showBackground = true)
@Composable
private fun FullGlassDarkPreview() { FullGlassTemplate(darkTheme = true) }
