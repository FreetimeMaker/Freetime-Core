package com.freetime.design

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text whose glyphs are filled with the current Freetime liquid-glass surface.
 *
 * The text mask is rendered first and the liquid-glass layer is composited through
 * that mask, so only the glyphs show glass. Tint is an additive Freetime extension
 * and does not replace the shared glass optics.
 */
@Composable
fun FreetimeGlassText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = FreetimeDesign.typography.bodyLarge,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
) {
    val fallback = if (tint != Color.Unspecified) tint else FreetimeDesign.colors.contentStrong
    val glassTint = if (tint != Color.Unspecified) tint else FreetimeDesign.palette.primary

    BasicText(
        text = text,
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .freetimeWideGlass(
                interactive = false,
                tint = glassTint,
                backdropLuminance = backdropLuminance,
            )
            .drawWithCache {
                onDrawWithContent {
                    // BasicText supplies the glyph alpha; DstIn clips the glass surface to it.
                    drawContent()
                    drawRect(Color.White, blendMode = BlendMode.DstIn)
                }
            },
        style = style.copy(color = fallback),
        maxLines = maxLines,
        overflow = overflow,
        softWrap = softWrap,
    )
}

/**
 * High-contrast glass text for display titles.
 */
@Composable
fun FreetimeGlassTitle(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) = FreetimeGlassText(
    text = text,
    modifier = modifier,
    style = FreetimeDesign.typography.headlineMedium,
    tint = tint,
    backdropLuminance = backdropLuminance,
    maxLines = maxLines,
    overflow = overflow,
)
