package com.freetime.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import java.util.Calendar

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    OLED,
    AUTO_TIME,
}

/** Shared by the glass renderer so every surface resolves the same light/dark state. */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

/**
 * Global Liquid Glass switch.
 *
 * This mirrors SimpMusic's approach: the app theme provides one boolean and the
 * shared liquidGlass modifier handles either real glass or the Material fallback.
 */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }

@Composable
fun rememberDarkTheme(
    themeMode: ThemeMode,
    lightHour: Int = 7,
    darkHour: Int = 19,
): Boolean {
    val systemDark = isSystemInDarkTheme()
    val safeLightHour = lightHour.coerceIn(0, 23)
    val safeDarkHour = darkHour.coerceIn(0, 23)
    var currentHour by remember(themeMode, safeLightHour, safeDarkHour) {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    }

    LaunchedEffect(themeMode, safeLightHour, safeDarkHour) {
        if (themeMode != ThemeMode.AUTO_TIME) return@LaunchedEffect
        while (true) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(60_000L)
        }
    }

    return when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.OLED -> true
        ThemeMode.AUTO_TIME -> {
            if (safeLightHour == safeDarkHour) {
                systemDark
            } else if (safeDarkHour > safeLightHour) {
                currentHour >= safeDarkHour || currentHour < safeLightHour
            } else {
                currentHour in safeDarkHour until safeLightHour
            }
        }
    }
}

/**
 * Material 3 Expressive / Material You root theme.
 *
 * Defaults:
 * - Material You dynamic colors on Android 12+
 * - light theme from 07:00
 * - dark theme from 19:00
 * - Liquid Glass enabled globally
 */
@Composable
fun AppTheme(
    themeMode: ThemeMode = ThemeMode.AUTO_TIME,
    lightHour: Int = 7,
    darkHour: Int = 19,
    dynamicColor: Boolean = true,
    liquidGlassEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isDark = rememberDarkTheme(themeMode, lightHour, darkHour)

    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        isDark -> darkColorScheme()
        else -> lightColorScheme()
    }

    val colorScheme = if (themeMode == ThemeMode.OLED) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
        )
    } else {
        baseScheme
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
    ) {
        CompositionLocalProvider(
            LocalIsDarkTheme provides isDark,
            LocalLiquidGlassEnabled provides liquidGlassEnabled,
            content = content,
        )
    }
}

/** Use this when an app already owns its MaterialTheme but still wants the global glass toggle. */
@Composable
fun ProvideLiquidGlass(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLiquidGlassEnabled provides enabled, content = content)
}
