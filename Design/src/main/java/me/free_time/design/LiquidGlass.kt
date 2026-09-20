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
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
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

@Composable
fun FreetimeGlassRoot(content: @Composable () -> Unit) {
    val backdrop = rememberFreetimeBackdrop()
    CompositionLocalProvider(LocalFreetimeBackdrop provides backdrop) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .freetimeBackdropSource(backdrop)
                    .background(MaterialTheme.colorScheme.background)
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

    // Keep the material tint extremely light so the backdrop remains visible,
    // closer to SimpMusic's floating glass controls than an opaque M3 surface.
    val surface = if (isDarkTheme) {
        Color.White.copy(alpha = 0.055f)
    } else {
        Color.White.copy(alpha = 0.085f)
    }

    val fallbackSurface = if (isDarkTheme) {
        Color.White.copy(alpha = 0.09f)
    } else {
        Color.White.copy(alpha = 0.46f)
    }

    if (backdrop == null) return clip(shape).background(fallbackSurface)

    val scope = rememberCoroutineScope()
    val press = remember { Animatable(0f) }

    val glass = drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(1.25.dp.toPx())
            lens(
                30.dp.toPx(),
                62.dp.toPx(),
                depthEffect = true,
                chromaticAberration = true,
            )
        },
        layerBlock = {
            val scale = lerp(1f, 0.972f, press.value)
            scaleX = scale
            scaleY = scale
        },
        onDrawSurface = { drawRect(surface) },
    )

    if (!interactive) return glass

    return glass.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            scope.launch {
                press.animateTo(1f, spring(dampingRatio = 0.52f, stiffness = 360f))
            }

            var pressed = true
            while (pressed) {
                val change = awaitPointerEvent(PointerEventPass.Initial)
                    .changes
                    .firstOrNull { it.id == down.id }
                pressed = change?.pressed == true
            }

            scope.launch {
                press.animateTo(0f, spring(dampingRatio = 0.68f, stiffness = 300f))
            }
        }
    }
}

@Composable
fun FreetimeGlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .freetimeGlass(FreetimeGlassDefaults.shape, interactive = false)
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
