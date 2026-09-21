package me.free_time.design

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
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
): Modifier = freetimeLiquidGlass(LocalFreetimeBackdrop.current, shape, interactive)

@Composable
fun Modifier.freetimeGlassCapsule(interactive: Boolean = true): Modifier =
    freetimeLiquidGlass(LocalFreetimeBackdrop.current, Capsule(), interactive)

@Composable
fun Modifier.freetimeLiquidGlass(
    backdrop: Backdrop?,
    shape: Shape,
    interactive: Boolean = true,
): Modifier {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Keep the fallback translucent, but use the real Kyant backdrop path whenever
    // Android can provide one. The real path intentionally follows SimpMusic's
    // liquid-glass recipe instead of behaving like a blurred Material surface.
    val fallbackSurface = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.28f)
    } else {
        Color.White.copy(alpha = 0.32f)
    }
    if (backdrop == null) return clip(shape).background(fallbackSurface)

    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }
    val touchPosition = remember { androidx.compose.runtime.mutableStateOf(Offset.Zero) }

    val glass = drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        highlight = { Highlight.Default },
        effects = {
            val p = press.value
            vibrancy()
            colorControls(
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(tokens.blur.toPx() + tokens.pressedBlurBoost.toPx() * p)
            // SimpMusic keeps refraction below the shape inradius. This produces
            // the crisp curved edge instead of the old heavy 24dp blur.
            lens(
                size.minDimension / 4f + tokens.pressedBlurBoost.toPx() * p,
                size.minDimension / 2f,
                depthEffect = false,
            )
        },
        layerBlock = if (interactive) {
            {
                // SimpMusic's small controls visibly bulge outward while pressed.
                val scale = lerp(1f, tokens.pressedScale, press.value)
                scaleX = scale
                scaleY = scale
            }
        } else null,
        onDrawSurface = {
            val base = if (isDarkTheme) Color.Black else Color.White
            drawRect(base.copy(alpha = if (isDarkTheme) tokens.darkSurfaceAlpha else tokens.lightSurfaceAlpha))
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
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            touchPosition.value = down.position
            scope.launch {
                press.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 300f))
            }

            var pressed = true
            while (pressed) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change != null) touchPosition.value = change.position
                pressed = change?.pressed == true
            }

            scope.launch {
                press.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 300f))
            }
        }
    }
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
        Text(text, color = MaterialTheme.colorScheme.onSurface)
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
