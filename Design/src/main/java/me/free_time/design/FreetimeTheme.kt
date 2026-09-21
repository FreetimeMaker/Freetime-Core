package me.free_time.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val FreetimeDarkColors = darkColorScheme(
    primary = Color(0xFFF2F2F2),
    onPrimary = Color(0xFF171717),
    primaryContainer = Color(0x24FFFFFF),
    onPrimaryContainer = Color(0xFFF7F7F7),
    secondary = Color(0xFFD7D7D7),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0x18FFFFFF),
    onSecondaryContainer = Color(0xFFEDEDED),
    tertiary = Color(0xFFC9C9C9),
    background = Color(0xFF090909),
    onBackground = Color(0xFFF4F4F4),
    surface = Color(0xFF101010),
    onSurface = Color(0xFFF4F4F4),
    surfaceVariant = Color(0xFF181818),
    onSurfaceVariant = Color(0xFFC9C9C9),
    outline = Color(0x2EFFFFFF),
    outlineVariant = Color(0x18FFFFFF),
    surfaceTint = Color.Transparent,
)

private val FreetimeLightColors = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color.White,
    primaryContainer = Color(0x1F000000),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF3F3F3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0x14000000),
    onSecondaryContainer = Color(0xFF222222),
    tertiary = Color(0xFF555555),
    background = Color(0xFFF4F4F4),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFF8F8F8),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0x24000000),
    outlineVariant = Color(0x12000000),
    surfaceTint = Color.Transparent,
)

@Composable
fun FreetimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    oledBlack: Boolean = false,
    glassTokens: FreetimeGlassTokens = FreetimeGlassTokens(),
    shapes: FreetimeShapes = FreetimeShapes(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        if (oledBlack) FreetimeDarkColors.copy(background = Color.Black, surface = Color.Black) else FreetimeDarkColors
    } else FreetimeLightColors

    val designColors = if (darkTheme) {
        FreetimeDesignColors(
            glassHighlight = Color.White,
            glassBorder = Color.White.copy(alpha = 0.16f),
            contentStrong = Color(0xFFF7F7F7),
            contentMuted = Color(0xFFC9C9C9),
        )
    } else {
        FreetimeDesignColors(
            glassHighlight = Color.White,
            glassBorder = Color.Black.copy(alpha = 0.10f),
            contentStrong = Color(0xFF111111),
            contentMuted = Color(0xFF555555),
        )
    }

    CompositionLocalProvider(
        LocalFreetimeGlassTokens provides glassTokens,
        LocalFreetimeShapes provides shapes,
        LocalFreetimeDesignColors provides designColors,
    ) {
        MaterialTheme(
            colorScheme = colors,
            shapes = MaterialTheme.shapes.copy(
                extraSmall = shapes.compact,
                small = shapes.compact,
                medium = shapes.control,
                large = shapes.surface,
                extraLarge = shapes.dialog,
            ),
            content = content,
        )
    }
}
