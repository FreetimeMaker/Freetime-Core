package com.freetime.design.templates.fullglass

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance

import androidx.compose.ui.unit.dp

import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

internal val LocalPageBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

/**
 * Supplies glass to arbitrary app content, not just the example screen. [source] must draw only the
 * background: it must never contain surfaces which sample this same backdrop. All content stays
 * sharp in the sibling foreground layer. Provide a Material theme before calling this root.
 */
@Composable
fun LiquidGlassRoot(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    source: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLayerBackdrop()
    Box(modifier.fillMaxSize()) {
        Box(Modifier.matchParentSize().background(backgroundColor).layerBackdrop(backdrop), content = source)
        CompositionLocalProvider(LocalPageBackdrop provides backdrop) {
            Box(Modifier.matchParentSize(), content = content)
        }
    }
}

/**
 * Safe fallback for a separately rooted Dialog or Popup. A window-local backdrop must not cross
 * windows. This also isolates any nested glass controls, which inherit an opaque Material surface.
 */
@Composable
fun LiquidGlassOverlaySurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    CompositionLocalProvider(LocalPageBackdrop provides null) {
        LiquidGlassSurface(modifier, shape, contentPadding, content)
    }
}

/** Use inside LiquidGlassRoot or FullGlassScreen. No backdrop means a readable Material fallback. */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = LocalPageBackdrop.current
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val tint = MaterialTheme.colorScheme.surface.copy(alpha = if (dark) 0.65f else 0.72f)
    val paint = if (backdrop == null) {
        Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
    } else {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            highlight = { Highlight(width = 1.dp) },
            effects = {
                vibrancy()
                colorControls(brightness = 0.03f, contrast = 1f, saturation = 1.35f)
                blur(12.dp.toPx())
                lens(size.minDimension / 4f, size.minDimension / 2f, depthEffect = false)
            },
            onDrawSurface = { drawRect(tint) },
        )
    }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(
                    modifier = modifier.then(paint).clip(shape).padding(contentPadding),
                    // A full-width or enlarged glass action must have an equally sized native hit target.
                    propagateMinConstraints = true,
                    content = content,
                )
    }
}

@Composable
fun LiquidGlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    LiquidGlassButton(onClick = onClick, modifier = modifier) { Text(text) }
}

@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    LiquidGlassSurface(modifier = modifier, contentPadding = PaddingValues(0.dp)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )
    }
}
