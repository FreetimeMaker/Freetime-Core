package me.free_time.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FreetimeDesignShowcase(modifier: Modifier = Modifier) {
    var enabled by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var slider by remember { mutableFloatStateOf(.55f) }
    var selectedChip by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.padding(FreetimeDesign.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.lg),
    ) {
        FreetimeTopBar(title = "Freetime Design")
        BasicText("Liquid Glass", style = FreetimeDesign.typography.headlineMedium.copy(color = FreetimeDesign.colors.contentStrong))

        FreetimeCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md)) {
                BasicText("Glass surface", style = FreetimeDesign.typography.titleMedium.copy(color = FreetimeDesign.colors.contentStrong))
                BasicText(
                    "Reusable translucent surfaces, controls and motion driven by Freetime tokens.",
                    style = FreetimeDesign.typography.bodyMedium.copy(color = FreetimeDesign.colors.contentMuted),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm)) {
                    FreetimeButton("Primary", onClick = {})
                    FreetimeChip(text = "One", selected = selectedChip == 0, onClick = { selectedChip = 0 })
                    FreetimeChip(text = "Two", selected = selectedChip == 1, onClick = { selectedChip = 1 })
                }
            }
        }

        FreetimeTextField(query, { query = it }, placeholder = "Search")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md)) {
            BasicText("Enabled", Modifier.weight(1f), style = FreetimeDesign.typography.bodyLarge.copy(color = FreetimeDesign.colors.contentStrong))
            FreetimeSwitch(enabled, { enabled = it })
        }

        FreetimeSlider(slider, { slider = it }, Modifier.fillMaxWidth())
        FreetimeProgressIndicator(progress = slider)
        FreetimeSnackbar("Freetime glass is active", actionLabel = "OK", onAction = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101010)
@Composable
private fun FreetimeDesignDarkPreview() {
    FreetimeTheme(darkTheme = true) {
        FreetimeGlassRoot(FreetimeDynamicBackdrop(listOf(Color(0xFF171A24), Color(0xFF101010)))) {
            FreetimeDesignShowcase()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
private fun FreetimeDesignLightPreview() {
    FreetimeTheme(darkTheme = false) {
        FreetimeGlassRoot(FreetimeDynamicBackdrop(listOf(Color(0xFFE9EEF8), Color(0xFFF4F4F4)))) {
            FreetimeDesignShowcase()
        }
    }
}
