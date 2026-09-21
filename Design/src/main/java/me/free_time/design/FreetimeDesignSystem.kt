package me.free_time.design

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
    val highlightAlpha: Float = 0.16f,
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
    val glass: FreetimeGlassTokens
        @androidx.compose.runtime.Composable get() = LocalFreetimeGlassTokens.current

    val colors: FreetimeDesignColors
        @androidx.compose.runtime.Composable get() = LocalFreetimeDesignColors.current
}
