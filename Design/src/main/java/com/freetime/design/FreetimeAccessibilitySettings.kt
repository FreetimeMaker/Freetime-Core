package com.freetime.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freetime.core.FreetimeStoredThemeMode

@Composable
fun FreetimeAccessibilitySettings(
    controller: FreetimePreferencesController,
    modifier: Modifier = Modifier,
    title: String = "Appearance & accessibility",
) {
    val state = controller.state
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        FreetimeGlassTitle(title)

        FreetimeSettingsGroup("Theme") {
            FreetimeStoredThemeMode.entries.forEach { mode ->
                FreetimeChoiceSetting(
                    title = when (mode) {
                        FreetimeStoredThemeMode.SYSTEM -> "System"
                        FreetimeStoredThemeMode.LIGHT -> "Light"
                        FreetimeStoredThemeMode.DARK -> "Dark"
                        FreetimeStoredThemeMode.OLED -> "OLED black"
                        FreetimeStoredThemeMode.AUTO_TIME -> "Automatic by time"
                    },
                    selected = state.themeMode == mode,
                    onClick = { controller.update { it.copy(themeMode = mode) } },
                )
            }

            AnimatedVisibility(state.themeMode == FreetimeStoredThemeMode.AUTO_TIME) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FreetimeText(
                        "Light from %02d:00 · Dark from %02d:00".format(state.lightHour, state.darkHour),
                        style = FreetimeDesign.typography.bodyMedium,
                    )
                    FreetimeSlider(
                        value = state.lightHour.toFloat(),
                        onValueChange = { hour ->
                            controller.update { it.copy(lightHour = hour.toInt().coerceIn(0, 23)) }
                        },
                        valueRange = 0f..23f,
                    )
                    FreetimeSlider(
                        value = state.darkHour.toFloat(),
                        onValueChange = { hour ->
                            controller.update { it.copy(darkHour = hour.toInt().coerceIn(0, 23)) }
                        },
                        valueRange = 0f..23f,
                    )
                }
            }
        }

        FreetimeSettingsGroup("Accessibility") {
            FreetimeSwitchSetting(
                title = "Reduce motion",
                checked = state.reduceMotion,
                onCheckedChange = { value -> controller.update { it.copy(reduceMotion = value) } },
                description = "Minimizes decorative animations and transitions.",
            )
            FreetimeDivider()
            FreetimeSwitchSetting(
                title = "Reduce transparency",
                checked = state.reduceTransparency,
                onCheckedChange = { value -> controller.update { it.copy(reduceTransparency = value) } },
                description = "Uses more opaque glass surfaces and disables refraction.",
            )
            FreetimeDivider()
            FreetimeSwitchSetting(
                title = "High contrast",
                checked = state.highContrast,
                onCheckedChange = { value -> controller.update { it.copy(highContrast = value) } },
                description = "Strengthens glass edges, scrims and visual separation.",
            )
        }

        FreetimeButton(
            text = "Reset appearance settings",
            onClick = controller::reset,
        )
    }
}
