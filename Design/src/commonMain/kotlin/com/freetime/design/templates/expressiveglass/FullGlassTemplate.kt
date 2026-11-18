package com.freetime.design.templates.expressiveglass

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.freetime.design.templates.fullglass.FullGlassScreen

/** Complete, themed glass screen; navigation changes only its selection highlight. */
@Composable
fun FullGlassTemplate(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
) {
    FreetimeTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        FullGlassScreen(title = "Expressive Glass", expressive = true, modifier = modifier)
    }
}

@Preview(name = "Full Expressive Glass · Light", showBackground = true)
@Composable
private fun FullGlassLightPreview() { FullGlassTemplate(darkTheme = false) }

@Preview(name = "Full Expressive Glass · Dark", showBackground = true)
@Composable
private fun FullGlassDarkPreview() { FullGlassTemplate(darkTheme = true) }
