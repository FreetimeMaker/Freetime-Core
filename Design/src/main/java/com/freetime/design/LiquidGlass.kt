package com.freetime.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.lerp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

private val LocalFloatingBottomNavigationBackdrop =
    staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Root used only for the floating bottom navigation glass backdrop.
 *
 * Normal app content remains Material 3 Expressive and must not use Liquid Glass.
 */
@Composable
fun FloatingBottomNavigationGlassRoot(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    source: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLayerBackdrop()

    CompositionLocalProvider(LocalFloatingBottomNavigationBackdrop provides backdrop) {
        Box(modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(backgroundColor)
                    .layerBackdrop(backdrop),
                content = source,
            )
            Box(
                modifier = Modifier.matchParentSize(),
                content = content,
            )
        }
    }
}

/**
 * The only public Liquid Glass surface in Freetime Core.
 *
 * Apply this exclusively to the floating Material 3 bottom navigation.
 */
@Composable
fun Modifier.floatingBottomNavigationGlass(): Modifier {
    val backdrop = LocalFloatingBottomNavigationBackdrop.current
    val isDark = LocalIsDarkTheme.current
    val shape = Capsule()

    if (!LocalFloatingBottomNavigationGlassEnabled.current || backdrop == null) {
        return clip(shape)
            .background(
                MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f),
            )
    }

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        highlight = { Highlight.Default },
        effects = {
            vibrancy()
            colorControls(
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(8.dp.toPx())
            lens(
                size.minDimension / 4f,
                size.minDimension / 2f,
                depthEffect = false,
            )
        },
        onDrawBackdrop = { drawBackdropContent ->
            drawBackdropContent()
        },
        onDrawSurface = {
            val scrim = lerp(0.12f, 0.50f, 0.4f)
            drawRect(
                (if (isDark) Color.Black else Color.White)
                    .copy(alpha = scrim),
            )
        },
    )
}
