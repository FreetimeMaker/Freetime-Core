package me.free_time.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun FreetimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    oledBlack: Boolean = false,
    glassTokens: FreetimeGlassTokens = FreetimeGlassTokens(),
    shapes: FreetimeShapes = FreetimeShapes(),
    spacing: FreetimeSpacing = FreetimeSpacing(),
    sizes: FreetimeSizes = FreetimeSizes(),
    motion: FreetimeMotion = FreetimeMotion(),
    typography: FreetimeTypography = FreetimeTypography(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) {
        FreetimePalette(
            background = if (oledBlack) Color.Black else Color(0xFF090909),
            surface = if (oledBlack) Color.Black else Color(0xFF101010),
            primary = Color(0xFFF2F2F2),
            onPrimary = Color(0xFF171717),
            contentStrong = Color(0xFFF7F7F7),
            contentMuted = Color(0xFFC9C9C9),
            outline = Color.White.copy(alpha = .16f),
        )
    } else {
        FreetimePalette(
            background = Color(0xFFF4F4F4),
            surface = Color(0xFFF8F8F8),
            primary = Color(0xFF171717),
            onPrimary = Color.White,
            contentStrong = Color(0xFF111111),
            contentMuted = Color(0xFF555555),
            outline = Color.Black.copy(alpha = .10f),
        )
    }
    val designColors = FreetimeDesignColors(
        glassHighlight = Color.White,
        glassBorder = palette.outline,
        contentStrong = palette.contentStrong,
        contentMuted = palette.contentMuted,
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
