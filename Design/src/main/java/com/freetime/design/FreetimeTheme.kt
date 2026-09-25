package com.freetime.design

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 / Material You theme for all Freetime UI.
 *
 * MaterialTheme is the actual design-system source of truth. The legacy
 * Freetime palette/typography locals are kept as a compatibility bridge for
 * existing Freetime apps while they migrate to MaterialTheme directly.
 */
@Composable
fun FreetimeTheme(
    darkTheme: Boolean = rememberFreetimeDarkTheme(FreetimeThemeMode.AUTO_TIME),
    dynamicColor: Boolean = true,
    oledBlack: Boolean = false,
    glassTokens: FreetimeGlassTokens = FreetimeGlassTokens(),
    shapes: FreetimeShapes = FreetimeShapes(),
    spacing: FreetimeSpacing = FreetimeSpacing(),
    sizes: FreetimeSizes = FreetimeSizes(),
    motion: FreetimeMotion = FreetimeMotion(),
    typography: FreetimeTypography = FreetimeTypography(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    val colorScheme = if (oledBlack && darkTheme) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
        )
    } else {
        baseScheme
    }

    val materialTypography = Typography(
        displayLarge = typography.displayLarge,
        displayMedium = typography.displayMedium,
        headlineLarge = typography.headlineLarge,
        headlineMedium = typography.headlineMedium,
        titleLarge = typography.titleLarge,
        titleMedium = typography.titleMedium,
        bodyLarge = typography.bodyLarge,
        bodyMedium = typography.bodyMedium,
        bodySmall = typography.bodySmall,
        labelLarge = typography.labelLarge,
        labelMedium = typography.labelMedium,
        labelSmall = typography.labelSmall,
    )
    val materialShapes = Shapes(
        extraSmall = shapes.compact,
        small = shapes.compact,
        medium = shapes.control,
        large = shapes.surface,
        extraLarge = shapes.dialog,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = materialTypography,
        shapes = materialShapes,
    ) {
        val scheme = MaterialTheme.colorScheme
        val palette = FreetimePalette(
            background = scheme.background,
            surface = scheme.surface,
            primary = scheme.primary,
            onPrimary = scheme.onPrimary,
            contentStrong = scheme.onSurface,
            contentMuted = scheme.onSurfaceVariant,
            outline = scheme.outlineVariant,
        )
        val designColors = FreetimeDesignColors(
            glassHighlight = Color.White,
            glassBorder = scheme.outlineVariant,
            contentStrong = scheme.onSurface,
            contentMuted = scheme.onSurfaceVariant,
        )

        CompositionLocalProvider(
            LocalFreetimeGlassTokens provides glassTokens,
            LocalFreetimeShapes provides shapes,
            LocalFreetimeDesignColors provides designColors,
            LocalFreetimeSpacing provides spacing,
            LocalFreetimeSizes provides sizes,
            LocalFreetimeMotion provides motion,
            LocalFreetimeTypography provides typography,
            LocalFreetimePalette provides palette,
            content = content,
        )
    }
}
