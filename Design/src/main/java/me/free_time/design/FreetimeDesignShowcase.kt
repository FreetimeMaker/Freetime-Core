package me.free_time.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

        Text("Liquid Glass", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        FreetimeCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md)) {
                Text("Glass surface", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(
                    "Reusable translucent surfaces, controls and motion driven by Freetime tokens.",
                    color = FreetimeDesign.colors.contentMuted,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm)) {
                    FreetimeButton("Primary", onClick = {})
                    FreetimeChip(text = "One", selected = selectedChip == 0, onClick = { selectedChip = 0 })
                    FreetimeChip(text = "Two", selected = selectedChip == 1, onClick = { selectedChip = 1 })
                }
            }
        }

        FreetimeTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = "Search",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md),
        ) {
            Text("Enabled", Modifier.weight(1f))
            FreetimeSwitch(enabled, { enabled = it })
        }

        FreetimeSlider(
            value = slider,
            onValueChange = { slider = it },
            modifier = Modifier.fillMaxWidth(),
        )

        FreetimeProgressIndicator(progress = slider)
        FreetimeSnackbar("Freetime glass is active", actionLabel = "OK", onAction = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101010)
@Composable
private fun FreetimeDesignDarkPreview() {
    FreetimeTheme(darkTheme = true) {
        FreetimeGlassRoot(
            dynamicBackdrop = FreetimeDynamicBackdrop(
                listOf(
                    androidx.compose.ui.graphics.Color(0xFF171A24),
                    androidx.compose.ui.graphics.Color(0xFF101010),
                )
            )
        ) {
            FreetimeDesignShowcase()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
private fun FreetimeDesignLightPreview() {
    FreetimeTheme(darkTheme = false) {
        FreetimeGlassRoot(
            dynamicBackdrop = FreetimeDynamicBackdrop(
                listOf(
                    androidx.compose.ui.graphics.Color(0xFFE9EEF8),
                    androidx.compose.ui.graphics.Color(0xFFF4F4F4),
                )
            )
        ) {
            FreetimeDesignShowcase()
        }
    }
}
