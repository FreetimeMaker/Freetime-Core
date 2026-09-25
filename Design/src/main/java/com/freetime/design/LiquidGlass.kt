package com.freetime.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.sign

/** Backdrop shared by glass surfaces under the current root. */
val LocalLiquidGlassBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
fun rememberLiquidGlassBackdrop(): LayerBackdrop = rememberLayerBackdrop()

fun Modifier.liquidGlassSource(backdrop: LayerBackdrop): Modifier = kyantLayerBackdrop(backdrop)

/**
 * Convenience root that keeps the sampled backdrop source separate from glass
 * foreground content, avoiding render-feedback loops.
 */
@Composable
fun LiquidGlassRoot(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    source: @Composable BoxScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val backdrop = rememberLiquidGlassBackdrop()
    CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdrop) {
        Box(modifier.fillMaxSize()) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(backgroundColor)
                    .liquidGlassSource(backdrop),
                content = source,
            )
            Box(Modifier.matchParentSize(), content = content)
        }
    }
}

/**
 * Shared SimpMusic-style glass primitive.
 *
 * The global switch is handled here so callers never need to branch themselves.
 * When disabled (or without a backdrop), the exact same shape falls back to
 * Material 3 surfaceContainerHighest at 80% opacity.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape? = null,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    backdrop: LayerBackdrop? = LocalLiquidGlassBackdrop.current,
): Modifier {
    val resolvedShape = shape ?: MaterialTheme.shapes.extraLarge

    if (!LocalLiquidGlassEnabled.current || backdrop == null) {
        return clip(resolvedShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f))
    }

    val layer = rememberGraphicsLayer()
    val interaction = rememberGlassInteraction()
    return drawInteractiveGlass(
        isDark = LocalIsDarkTheme.current,
        backdrop = backdrop,
        layer = layer,
        luminanceAnimation = 0.5f,
        shape = resolvedShape,
        interaction = if (interactive) interaction else null,
        highlight = highlight,
    )
}

@Composable
fun Modifier.liquidGlassCapsule(
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    backdrop: LayerBackdrop? = LocalLiquidGlassBackdrop.current,
): Modifier = liquidGlass(
    shape = Capsule(),
    interactive = interactive,
    highlight = highlight,
    backdrop = backdrop,
)

@Composable
fun Modifier.liquidGlassCircle(
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    backdrop: LayerBackdrop? = LocalLiquidGlassBackdrop.current,
): Modifier = liquidGlass(
    shape = CircleShape,
    interactive = interactive,
    highlight = highlight,
    backdrop = backdrop,
)

/**
 * Advanced overload for large surfaces that continuously sample backdrop luminance.
 */
@Composable
fun Modifier.liquidGlass(
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape? = null,
    interactive: Boolean = true,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
    backdrop: LayerBackdrop? = LocalLiquidGlassBackdrop.current,
): Modifier {
    val resolvedShape = shape ?: MaterialTheme.shapes.extraLarge

    if (!LocalLiquidGlassEnabled.current || backdrop == null) {
        return clip(resolvedShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f))
    }

    val interaction = rememberGlassInteraction()
    return drawInteractiveGlass(
        isDark = LocalIsDarkTheme.current,
        backdrop = backdrop,
        layer = layer,
        luminanceAnimation = luminanceAnimation,
        shape = resolvedShape,
        interaction = if (interactive) interaction else null,
        pressedScale = 1.04f,
        blurScale = blurScale,
        minScrim = minScrim,
        maxScrim = maxScrim,
    )
}

@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(shape, interactive, highlight),
        contentAlignment = contentAlignment,
        content = content,
    )
}

@Composable
fun LiquidGlassIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(48.dp),
    tint: Color = MaterialTheme.colorScheme.onSurface,
    interactive: Boolean = true,
) {
    LiquidGlassContainer(
        modifier = modifier,
        shape = CircleShape,
        interactive = interactive,
        highlight = Highlight.Plain,
    ) {
        IconButton(onClick = onClick) {
            Icon(imageVector = imageVector, contentDescription = contentDescription, tint = tint)
        }
    }
}

private class GlassInteraction(
    private val animationScope: CoroutineScope,
) {
    private val pressSpec = spring(dampingRatio = 0.5f, stiffness = 300f, visibilityThreshold = 0.001f)
    private val pressAnimation = Animatable(0f, 0.001f)

    val pressProgress: Float get() = pressAnimation.value

    var touchPosition by mutableStateOf(Offset.Zero)
        private set

    suspend fun detectPress(pointer: PointerInputScope) =
        with(pointer) {
            inspectGlassGestures(
                onDragStart = { down ->
                    touchPosition = down.position
                    animationScope.launch { pressAnimation.animateTo(1f, pressSpec) }
                },
                onDragEnd = {
                    animationScope.launch { pressAnimation.animateTo(0f, pressSpec) }
                },
                onDragCancel = {
                    animationScope.launch { pressAnimation.animateTo(0f, pressSpec) }
                },
            ) { change, _ ->
                touchPosition = change.position
            }
        }
}

@Composable
private fun rememberGlassInteraction(): GlassInteraction {
    val scope = rememberCoroutineScope()
    return remember(scope) { GlassInteraction(scope) }
}

/**
 * Shared optical recipe aligned with SimpMusic:
 * vibrancy + 1.5 saturation, 0.05 brightness, adaptive 2..16dp blur,
 * half-height refraction, 0.12..0.50 scrim and press-to-bulge interaction.
 */
fun Modifier.drawInteractiveGlass(
    isDark: Boolean,
    backdrop: LayerBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape,
    interaction: Any?,
    pressedScale: Float = 1.12f,
    highlight: Highlight = Highlight.Default,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
): Modifier {
    val glassInteraction = interaction as? GlassInteraction

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        highlight = { highlight },
        effects = {
            val l = (luminanceAnimation * 2f - 1f).let { sign(it) * it * it }
            val press = glassInteraction?.pressProgress ?: 0f

            vibrancy()
            colorControls(
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(
                (
                    if (l > 0f) {
                        lerp(8.dp.toPx(), 16.dp.toPx(), l)
                    } else {
                        lerp(8.dp.toPx(), 2.dp.toPx(), -l)
                    }
                ) * blurScale + 2.dp.toPx() * press,
            )
            lens(
                size.minDimension / 4f + 2.dp.toPx() * press,
                size.minDimension / 2f,
                depthEffect = false,
            )
        },
        onDrawBackdrop = { drawBackdropContent ->
            drawBackdropContent()
            layer.record { drawBackdropContent() }
        },
        onDrawSurface = {
            val scrim = lerp(
                minScrim,
                maxScrim,
                ((luminanceAnimation - 0.3f) / 0.5f).coerceIn(0f, 1f),
            )
            drawRect((if (isDark) Color.Black else Color.White).copy(alpha = scrim))

            val press = glassInteraction?.pressProgress ?: 0f
            if (press > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f * press),
                            Color.Transparent,
                        ),
                        center = glassInteraction?.touchPosition
                            ?: Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension * 1.2f,
                    ),
                    blendMode = BlendMode.Plus,
                )
            }
        },
        layerBlock = if (glassInteraction != null) {
            {
                val scale = lerp(1f, pressedScale, glassInteraction.pressProgress)
                scaleX = scale
                scaleY = scale
            }
        } else {
            null
        },
    ).then(
        if (glassInteraction != null) {
            Modifier.pointerInput(glassInteraction) { glassInteraction.detectPress(this) }
        } else {
            Modifier
        },
    )
}

private suspend fun PointerInputScope.inspectGlassGestures(
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

        val up = dragGlass(down.id) { change ->
            onDrag(change, change.positionChange())
        }

        if (up == null) onDragCancel() else onDragEnd(up)
    }
}

private suspend inline fun AwaitPointerEventScope.dragGlass(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
): PointerInputChange? {
    if (currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true) return null

    var pointer = pointerId
    while (true) {
        val change = awaitGlassDragOrUp(pointer) ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) return change
        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitGlassDragOrUp(
    pointerId: PointerId,
): PointerInputChange? {
    var pointer = pointerId

    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.fastFirstOrNull { it.id == pointer } ?: return null

        if (change.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) return change
            pointer = otherDown.id
        } else if (change.previousPosition != change.position) {
            return change
        }
    }
}
