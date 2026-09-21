package com.freetime.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class FreetimeThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    OLED,
    AUTO_TIME,
}

@Immutable
data class FreetimeAppConfig(
    val themeMode: FreetimeThemeMode = FreetimeThemeMode.SYSTEM,
    val darkHour: Int = 19,
    val lightHour: Int = 7,
    val dynamicColor: Boolean = false,
    val backdropColors: List<Color> = emptyList(),
)

val LocalFreetimeReducedMotion = staticCompositionLocalOf { false }

object FreetimeAppEnvironment {
    val reducedMotion: Boolean @Composable get() = LocalFreetimeReducedMotion.current
}

@Composable
fun rememberFreetimeDarkTheme(
    mode: FreetimeThemeMode,
    darkHour: Int = 19,
    lightHour: Int = 7,
): Boolean {
    val systemDark = isSystemInDarkTheme()
    return when (mode) {
        FreetimeThemeMode.SYSTEM -> systemDark
        FreetimeThemeMode.LIGHT -> false
        FreetimeThemeMode.DARK, FreetimeThemeMode.OLED -> true
        FreetimeThemeMode.AUTO_TIME -> {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (darkHour == lightHour) systemDark
            else if (darkHour > lightHour) hour >= darkHour || hour < lightHour
            else hour in darkHour until lightHour
        }
    }
}

/**
 * Root for Freetime applications. It combines theme selection, accessibility
 * motion state and the shared Liquid Glass backdrop into one entry point.
 */
@Composable
fun FreetimeApp(
    config: FreetimeAppConfig = FreetimeAppConfig(),
    content: @Composable () -> Unit,
) {
    val dark = rememberFreetimeDarkTheme(
        mode = config.themeMode,
        darkHour = config.darkHour.coerceIn(0, 23),
        lightHour = config.lightHour.coerceIn(0, 23),
    )
    val reducedMotion = rememberFreetimeReducedMotion()
    FreetimeTheme(
        darkTheme = dark,
        dynamicColor = config.dynamicColor,
        oledBlack = config.themeMode == FreetimeThemeMode.OLED,
    ) {
        CompositionLocalProvider(LocalFreetimeReducedMotion provides reducedMotion) {
            if (config.backdropColors.isEmpty()) {
                FreetimeGlassRoot { content() }
            } else {
                FreetimeGlassRoot(FreetimeDynamicBackdrop(config.backdropColors)) { content() }
            }
        }
    }
}
