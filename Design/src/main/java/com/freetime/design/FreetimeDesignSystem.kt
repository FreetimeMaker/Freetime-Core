package com.freetime.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FreetimeGlassTokens(
    val blur: Dp = 8.dp,
    val pressedBlurBoost: Dp = 2.dp,
    val darkSurfaceAlpha: Float = 0.14f,
    val lightSurfaceAlpha: Float = 0.12f,
    val darkFallbackAlpha: Float = 0.24f,
    val lightFallbackAlpha: Float = 0.28f,
    val saturation: Float = 1.55f,
    val brightness: Float = 0.05f,
    val pressedScale: Float = 1.10f,
    val highlightAlpha: Float = 0.22f,
    val tintAlpha: Float = 0.20f,
    val tintFallbackAlpha: Float = 0.28f,
    val edgeAlpha: Float = 0.22f,
)

@Immutable
data class FreetimeDesignColors(
    val glassHighlight: Color,
    val glassBorder: Color,
    val contentStrong: Color,
    val contentMuted: Color,
)

val LocalFreetimeGlassTokens = staticCompositionLocalOf { FreetimeGlassTokens() }
val LocalFreetimeDesignColors = staticCompositionLocalOf {
    FreetimeDesignColors(
        glassHighlight = Color.White,
        glassBorder = Color.White.copy(alpha = 0.18f),
        contentStrong = Color.White,
        contentMuted = Color.White.copy(alpha = 0.72f),
    )
}

object FreetimeDesign {
    val glass: FreetimeGlassTokens @Composable get() = LocalFreetimeGlassTokens.current
    val colors: FreetimeDesignColors @Composable get() = LocalFreetimeDesignColors.current
    val spacing: FreetimeSpacing @Composable get() = LocalFreetimeSpacing.current
    val sizes: FreetimeSizes @Composable get() = LocalFreetimeSizes.current
    val motion: FreetimeMotion @Composable get() = LocalFreetimeMotion.current
    val shapes: FreetimeShapes @Composable get() = LocalFreetimeShapes.current
    val typography: FreetimeTypography @Composable get() = LocalFreetimeTypography.current
    val palette: FreetimePalette @Composable get() = LocalFreetimePalette.current
}
