package com.freetime.design

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.sign
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.launch

object FreetimeGlassDefaults {
    val shape = RoundedCornerShape(30.dp)
    val compactShape = RoundedCornerShape(24.dp)
}

val LocalFreetimeBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
fun rememberFreetimeBackdrop(): LayerBackdrop? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) rememberLayerBackdrop() else null

fun Modifier.freetimeBackdropSource(backdrop: LayerBackdrop?): Modifier =
    if (backdrop != null) layerBackdrop(backdrop) else this

data class FreetimeDynamicBackdrop(
    val colors: List<Color>,
    val vertical: Boolean = true,
)

@Composable
fun FreetimeGlassRoot(
    dynamicBackdrop: FreetimeDynamicBackdrop? = null,
    content: @Composable () -> Unit,
) {
    val backdrop = rememberFreetimeBackdrop()
    val backgroundModifier = if (dynamicBackdrop != null && dynamicBackdrop.colors.isNotEmpty()) {
        val brush = if (dynamicBackdrop.vertical) {
            Brush.verticalGradient(dynamicBackdrop.colors)
        } else {
            Brush.horizontalGradient(dynamicBackdrop.colors)
        }
        Modifier.background(brush)
    } else {
        Modifier
    }
    CompositionLocalProvider(LocalFreetimeBackdrop provides backdrop) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .freetimeBackdropSource(backdrop)
                    .then(backgroundModifier)
            )
            Box(Modifier.fillMaxSize()) { content() }
        }
    }
}

@Composable
fun Modifier.freetimeGlass(
    shape: Shape = FreetimeGlassDefaults.shape,
    interactive: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    pressedScale: Float = 1.12f,
    highlight: Highlight = Highlight.Default,
): Modifier = freetimeLiquidGlass(LocalFreetimeBackdrop.current, shape, interactive, tint, backdropLuminance, pressedScale, highlight)

@Composable
fun Modifier.freetimeGlassCapsule(
    interactive: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    pressedScale: Float = 1.12f,
    highlight: Highlight = Highlight.Default,
): Modifier = freetimeLiquidGlass(LocalFreetimeBackdrop.current, Capsule(), interactive, tint, backdropLuminance, pressedScale, highlight)

@Composable
fun Modifier.freetimeLiquidGlass(
    backdrop: Backdrop?,
    shape: Shape,
    interactive: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    pressedScale: Float = 1.12f,
    highlight: Highlight = Highlight.Default,
    recordingLayer: GraphicsLayer? = null,
): Modifier {
    val isDarkTheme = LocalFreetimePalette.current.background.luminance() < 0.5f
    val tokens = LocalFreetimeGlassTokens.current
    val designColors = LocalFreetimeDesignColors.current
    val reduceTransparency = LocalFreetimeReduceTransparency.current
    val highContrast = LocalFreetimeHighContrast.current
    val liquidGlassEnabled = LocalFreetimeLiquidGlassEnabled.current

    if (!liquidGlassEnabled) {
        val flat = (if (isDarkTheme) Color.Black else Color.White).copy(alpha = 0.80f)
        return clip(shape).background(flat)
    }

    // Keep the fallback translucent, but use the real Kyant backdrop path whenever
    // Android can provide one. The real path intentionally follows SimpMusic's
    // liquid-glass recipe instead of behaving like a blurred Material surface.
    val fallbackSurface = if (isDarkTheme) {
        Color.Black.copy(alpha = if (reduceTransparency) .88f else tokens.darkFallbackAlpha)
    } else {
        Color.White.copy(alpha = if (reduceTransparency) .92f else tokens.lightFallbackAlpha)
    }
    val tintColor = tint.takeIf { it != Color.Unspecified }
    if (backdrop == null) {
        val fallback = if (tintColor != null) {
            Brush.linearGradient(
                listOf(
                    tintColor.copy(alpha = tokens.tintFallbackAlpha),
                    fallbackSurface,
                    tintColor.copy(alpha = tokens.tintFallbackAlpha * .55f),
                )
            )
        } else {
            Brush.linearGradient(listOf(fallbackSurface, fallbackSurface))
        }
        return clip(shape).background(fallback)
    }

    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val touchPosition = remember { mutableStateOf(Offset.Zero) }

    val glass = drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        highlight = { highlight },
        effects = {
            val p = press.value
            if (!reduceTransparency) vibrancy()
            colorControls(
                brightness = tokens.brightness,
                contrast = if (highContrast) 1.22f else 1f,
                saturation = if (reduceTransparency) 1f else tokens.saturation,
            )
            val normalized = (backdropLuminance * 2f - 1f).let { value ->
                kotlin.math.sign(value) * value * value
            }
            val adaptiveBlur = if (normalized > 0f) {
                lerp(tokens.blur.toPx(), tokens.maxBlur.toPx(), normalized)
            } else {
                lerp(tokens.blur.toPx(), tokens.minBlur.toPx(), -normalized)
            }
            blur((if (reduceTransparency) tokens.minBlur.toPx() else adaptiveBlur) + tokens.pressedBlurBoost.toPx() * p)
            // Refraction is the most expensive part of the glass pipeline. Keep the
            // idle surface blurred/vibrant, but only render the lens while the user is
            // actually interacting with an interactive glass control.
            if (!reduceTransparency && interactive && p > 0.001f) {
                lens(
                    size.minDimension / 4f + tokens.pressedBlurBoost.toPx() * p,
                    size.minDimension / 2f,
                    depthEffect = false,
                )
            }
        },
        layerBlock = if (interactive) {
            {
                // SimpMusic's small controls visibly bulge outward while pressed.
                val scale = lerp(1f, pressedScale, press.value)
                scaleX = scale
                scaleY = scale
            }
        } else null,
        onDrawBackdrop = { drawBackdropContent ->
            drawBackdropContent()
            recordingLayer?.record { drawBackdropContent() }
        },
        onDrawSurface = {
            val base = if (isDarkTheme) Color.Black else Color.White
            val lumNorm = ((backdropLuminance - 0.3f) / 0.5f).coerceIn(0f, 1f)
            val adaptiveScrim = lerp(tokens.minScrimAlpha, tokens.maxScrimAlpha, lumNorm)
            val baseAlpha = when {
                reduceTransparency -> if (isDarkTheme) .88f else .92f
                else -> adaptiveScrim
            }
            drawRect(base.copy(alpha = baseAlpha))
            if (tintColor != null) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            tintColor.copy(alpha = tokens.tintAlpha),
                            tintColor.copy(alpha = tokens.tintAlpha * .42f),
                            Color.Transparent,
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    ),
                    blendMode = BlendMode.SrcOver,
                )
            }
            if (highContrast) {
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            designColors.glassHighlight.copy(alpha = maxOf(tokens.edgeAlpha, .55f)),
                            Color.Transparent,
                        )
                    ),
                    blendMode = BlendMode.SrcOver,
                )
            }
            val p = press.value
            if (p > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            designColors.glassHighlight.copy(alpha = tokens.highlightAlpha * p),
                            Color.Transparent,
                        ),
                        center = touchPosition.value.takeUnless { it == Offset.Zero }
                            ?: Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 1.2f,
                    ),
                    blendMode = BlendMode.Plus,
                )
            }
        },
    )

    if (!interactive) return glass

    return glass.pointerInput(Unit) {
        inspectFreetimeGlassGestures(
            onDragStart = { down ->
                touchPosition.value = down.position
                scope.launch {
                    press.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f))
                }
            },
            onDragEnd = {
                scope.launch {
                    press.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
                }
            },
            onDragCancel = {
                scope.launch {
                    press.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
                }
            },
        ) { change, _ ->
            touchPosition.value = change.position
        }
    }
}

private suspend fun PointerInputScope.inspectFreetimeGlassGestures(
    onDragStart: (PointerInputChange) -> Unit = {},
    onDragEnd: (PointerInputChange) -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (PointerInputChange, Offset) -> Unit,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val down = awaitFirstDown(requireUnconsumed = false)
        onDragStart(down)
        onDrag(down, Offset.Zero)
        val up = freetimeDrag(down.id) { onDrag(it, it.positionChange()) }
        if (up == null) onDragCancel() else onDragEnd(up)
    }
}

private suspend inline fun AwaitPointerEventScope.freetimeDrag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    if (currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true) return null
    var pointer = pointerId
    while (true) {
        val change = freetimeAwaitDragOrUp(pointer) ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) return change
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.freetimeAwaitDragOrUp(
    pointerId: PointerId,
): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) return dragEvent
            pointer = otherDown.id
        } else if (dragEvent.previousPosition != dragEvent.position) {
            return dragEvent
        }
    }
}


@Composable
fun rememberFreetimeGlassLayer(): GraphicsLayer = rememberGraphicsLayer()

@Composable
fun Modifier.freetimeWideGlass(
    shape: Shape = FreetimeGlassDefaults.shape,
    interactive: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
    highlight: Highlight = Highlight.Default,
): Modifier = freetimeLiquidGlass(
    backdrop = LocalFreetimeBackdrop.current,
    shape = shape,
    interactive = interactive,
    tint = tint,
    backdropLuminance = backdropLuminance,
    pressedScale = 1.04f,
    highlight = highlight,
)

@Composable
fun Modifier.freetimeRoundGlass(
    interactive: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropLuminance: Float = 0.5f,
): Modifier = freetimeGlassCapsule(
    interactive = interactive,
    tint = tint,
    backdropLuminance = backdropLuminance,
    pressedScale = 1.12f,
    highlight = Highlight(width = 1.dp),
)

@Composable
fun Modifier.freetimeSelectedGlassCapsule(
    tint: Color = FreetimeDesign.palette.primary,
    backdropLuminance: Float = 0.5f,
    recordingLayer: GraphicsLayer? = null,
    pressProgress: Float = 0f,
): Modifier {
    val backdrop = LocalFreetimeBackdrop.current
    val tokens = LocalFreetimeGlassTokens.current
    val isDark = LocalFreetimePalette.current.background.luminance() < 0.5f
    val reduceTransparency = LocalFreetimeReduceTransparency.current
    val highContrast = LocalFreetimeHighContrast.current
    val liquidGlassEnabled = LocalFreetimeLiquidGlassEnabled.current
    if (!liquidGlassEnabled) {
        return clip(Capsule()).background(
            (if (isDark) Color.Black else Color.White).copy(alpha = .80f)
        )
    }
    if (backdrop == null || reduceTransparency) {
        return clip(Capsule()).background(
            Brush.linearGradient(
                listOf(
                    tint.copy(alpha = tokens.tintFallbackAlpha),
                    (if (isDark) Color.Black else Color.White).copy(
                        alpha = if (reduceTransparency) .90f else .10f
                    ),
                )
            )
        )
    }
    return drawBackdrop(
        backdrop = backdrop,
        shape = { Capsule() },
        effects = {
            vibrancy()
            colorControls(brightness = .05f, contrast = if (highContrast) 1.22f else 1f, saturation = 1.5f)
            val normalized = (backdropLuminance * 2f - 1f).let { value ->
                kotlin.math.sign(value) * value * value
            }
            val adaptive = if (normalized > 0f) {
                lerp(tokens.blur.toPx(), tokens.maxBlur.toPx(), normalized)
            } else {
                lerp(tokens.blur.toPx(), tokens.minBlur.toPx(), -normalized)
            }
            blur(adaptive + 20.dp.toPx())
            lens(
                10.dp.toPx() * pressProgress,
                14.dp.toPx() * pressProgress,
                depthEffect = false,
                chromaticAberration = true,
            )
        },
        onDrawBackdrop = { drawBackdropContent ->
            drawBackdropContent()
            recordingLayer?.record { drawBackdropContent() }
        },
        highlight = { Highlight.Default.copy(alpha = if (highContrast) 1f else tokens.selectedHighlightAlpha) },
        shadow = { Shadow(radius = 4.dp, alpha = .4f) },
        innerShadow = {
            InnerShadow(
                radius = 8.dp * pressProgress,
                alpha = pressProgress,
            )
        },
        onDrawSurface = {
            val lumNorm = ((backdropLuminance - .3f) / .5f).coerceIn(0f, 1f)
            val shade = if (isDark) lerp(.035f, .09f, lumNorm) else lerp(.015f, .045f, lumNorm)
            drawRect(Color.Black.copy(alpha = shade))
            drawRect(
                Brush.linearGradient(
                    listOf(tint.copy(alpha = tokens.tintAlpha), Color.Transparent)
                )
            )
        },
    )
}

@Composable
fun FreetimeGlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .freetimeGlass(LocalFreetimeShapes.current.surface, interactive = false)
            .padding(16.dp)
    ) { content() }
}

@Composable
fun FreetimeGlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .freetimeGlassCapsule()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(text, style = FreetimeDesign.typography.labelLarge.copy(color = FreetimeDesign.colors.contentStrong))
    }
}

@Composable
fun FreetimeGlassNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .freetimeGlassCapsule(interactive = false)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
