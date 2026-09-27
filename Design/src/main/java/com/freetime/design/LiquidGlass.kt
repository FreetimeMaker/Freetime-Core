package com.freetime.design

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.lerp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.IntBuffer
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.coroutines.coroutineContext

private val LocalFloatingBottomNavigationBackdrop =
    staticCompositionLocalOf<LayerBackdrop?> { null }

private val NavigationCapsuleShape = RoundedCornerShape(percent = 50)
private val NavigationTabWidth = 96.dp
private val NavigationBarHeight = 64.dp
private val NavigationIndicatorHeight = 56.dp
private val NavigationInset = 6.dp
private val SearchButtonSize = 56.dp
private val SearchGap = 12.dp

data class FloatingBottomNavigationItem(
    val label: String,
    val icon: @Composable () -> Unit,
)

/**
 * Backdrop host for the floating bottom navigation.
 *
 * Normal content remains Material 3 Expressive. Only [FloatingBottomNavigationBar]
 * reads the recorded backdrop.
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
 * Floating Liquid Glass navigation styled after SimpMusic's current Android bar:
 * 64dp capsule, 56dp sliding frosted indicator, up-to-96dp tabs, a 6dp inset,
 * and a separate 56dp circular search action with a 12dp gap.
 */
@Composable
fun FloatingBottomNavigationBar(
    items: List<FloatingBottomNavigationItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    searchItem: FloatingBottomNavigationItem,
    onSearchSelected: () -> Unit,
    modifier: Modifier = Modifier,
    searchSelected: Boolean = false,
) {
    if (items.isEmpty()) return

    val backdrop = LocalFloatingBottomNavigationBackdrop.current
    val glassEnabled = LocalFloatingBottomNavigationGlassEnabled.current && backdrop != null
    val isDark = LocalIsDarkTheme.current
    val layer = rememberGraphicsLayer()
    val barPress = rememberNavigationGlassPress()
    val searchPress = rememberNavigationGlassPress()
    val luminance = remember { Animatable(0.5f) }

    LaunchedEffect(layer, glassEnabled) {
        if (!glassEnabled) return@LaunchedEffect

        val buffer = IntBuffer.allocate(25)
        while (isActive) {
            var average = luminance.value
            try {
                average = withContext(Dispatchers.Default) {
                    val image = layer.toImageBitmap().asAndroidBitmap()
                    if (image.width <= 0 || image.height <= 0) {
                        return@withContext average
                    }
                    val thumbnail = Bitmap.createScaledBitmap(image, 5, 5, false)
                    buffer.rewind()
                    thumbnail.copyPixelsToBuffer(buffer)
                    var sum = 0.0
                    repeat(25) { index ->
                        val packed = buffer.get(index)
                        val r = ((packed shr 16) and 0xFF) / 255f
                        val g = ((packed shr 8) and 0xFF) / 255f
                        val b = (packed and 0xFF) / 255f
                        sum += 0.2126 * r + 0.7152 * g + 0.0722 * b
                    }
                    thumbnail.recycle()
                    (sum / 25.0).toFloat().coerceIn(0.3f, 0.8f)
                }
            } catch (_: Throwable) {
                // Keep the previous value if the graphics layer has not produced a frame yet.
            }
            luminance.animateTo(average, tween(500))
            delay(1_000L)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .padding(bottom = 8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                FloatingNavigationCapsule(
                    items = items,
                    selectedItemIndex = selectedItemIndex,
                    onItemSelected = onItemSelected,
                    availableWidth = maxWidth,
                    backdrop = backdrop,
                    glassEnabled = glassEnabled,
                    isDark = isDark,
                    layer = layer,
                    luminance = luminance.value,
                    press = barPress,
                )
            }

            Spacer(Modifier.size(SearchGap))

            FloatingNavigationSearchButton(
                item = searchItem,
                selected = searchSelected,
                onClick = onSearchSelected,
                backdrop = backdrop,
                glassEnabled = glassEnabled,
                isDark = isDark,
                layer = layer,
                luminance = luminance.value,
                press = searchPress,
            )
        }
    }
}

@Composable
private fun FloatingNavigationCapsule(
    items: List<FloatingBottomNavigationItem>,
    selectedItemIndex: Int,
    onItemSelected: (Int) -> Unit,
    availableWidth: Dp,
    backdrop: LayerBackdrop?,
    glassEnabled: Boolean,
    isDark: Boolean,
    layer: GraphicsLayer,
    luminance: Float,
    press: NavigationGlassPress,
) {
    val density = LocalDensity.current
    val count = items.size
    val tabWidth = ((availableWidth - NavigationInset * 2) / count).coerceAtMost(NavigationTabWidth)
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val insetPx = with(density) { NavigationInset.toPx() }
    val dragThresholdPx = with(density) { 8.dp.toPx() }
    val ltr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val scope = rememberCoroutineScope()
    val motion = remember(count) {
        NavigationIndicatorMotion(
            initialIndex = selectedItemIndex.coerceIn(0, count - 1),
            maxIndex = count - 1,
        )
    }

    LaunchedEffect(selectedItemIndex, count) {
        if (selectedItemIndex in 0 until count) {
            motion.animateTo(selectedItemIndex)
        }
    }

    Box(
        modifier = Modifier
            .height(NavigationBarHeight)
            .width(tabWidth * count + NavigationInset * 2)
            .pointerInput(press) { observeNavigationPress(press) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .navigationGlass(
                    enabled = glassEnabled,
                    isDark = isDark,
                    backdrop = backdrop,
                    layer = layer,
                    luminance = luminance,
                    shape = NavigationCapsuleShape,
                    press = press,
                ),
        )

        val indicatorPress = motion.pressProgress
        val indicatorVelocity = motion.velocity

        Box(
            modifier = Modifier
                .graphicsLayer {
                    val value = if (ltr) motion.position else (count - 1) - motion.position
                    translationX = value * tabWidthPx + insetPx

                    val velocityWarp = (indicatorVelocity / 10f).coerceIn(-0.2f, 0.2f)
                    scaleX = motion.scale / (1f - velocityWarp * 0.75f)
                    scaleY = motion.scale * (1f - velocityWarp * 0.25f)
                }
                .selectionGlass(
                    enabled = glassEnabled,
                    isDark = isDark,
                    backdrop = backdrop,
                    luminance = luminance,
                    pressProgress = indicatorPress,
                )
                .width(tabWidth)
                .height(NavigationIndicatorHeight),
        )

        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = NavigationInset)
                .pointerInput(motion, tabWidthPx, ltr) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        motion.press(scope)

                        var pointer = down.id
                        var previous = down.position
                        var totalDrag = 0f
                        var dragging = false

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.fastFirstOrNull { it.id == pointer } ?: break

                            if (!change.pressed) {
                                if (dragging) {
                                    val target = motion.nearestIndex()
                                    motion.release(scope, target)
                                    onItemSelected(target)
                                } else {
                                    motion.release(scope, motion.nearestIndex())
                                }
                                break
                            }

                            val delta = change.position.x - previous.x
                            previous = change.position
                            totalDrag += delta

                            if (!dragging && abs(totalDrag) >= dragThresholdPx) {
                                dragging = true
                            }

                            if (dragging && delta != 0f) {
                                change.consume()
                                motion.dragBy(
                                    deltaPx = delta,
                                    tabWidthPx = tabWidthPx,
                                    direction = if (ltr) 1f else -1f,
                                )
                            }
                            pointer = change.id
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                val selected = index == selectedItemIndex
                val contentColor = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Column(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(NavigationCapsuleShape)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                        ) {
                            motion.animateTo(scope, index)
                            onItemSelected(index)
                        },
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        item.icon()
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingNavigationSearchButton(
    item: FloatingBottomNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    backdrop: LayerBackdrop?,
    glassEnabled: Boolean,
    isDark: Boolean,
    layer: GraphicsLayer,
    luminance: Float,
    press: NavigationGlassPress,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(SearchButtonSize)
            .navigationGlass(
                enabled = glassEnabled,
                isDark = isDark,
                backdrop = backdrop,
                layer = layer,
                luminance = luminance,
                shape = CircleShape,
                press = press,
            )
            .pointerInput(press) { observeNavigationPress(press) }
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            item.icon()
        }
    }
}

@Composable
private fun Modifier.navigationGlass(
    enabled: Boolean,
    isDark: Boolean,
    backdrop: LayerBackdrop?,
    layer: GraphicsLayer,
    luminance: Float,
    shape: Shape,
    press: NavigationGlassPress,
): Modifier {
    if (!enabled || backdrop == null) {
        return clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f))
    }

    val progress = press.progress
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        highlight = { Highlight.Default },
        effects = {
            val normalized = (luminance * 2f - 1f).let { sign(it) * it * it }

            vibrancy()
            colorControls(
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(
                (if (normalized > 0f) {
                    lerp(8.dp.toPx(), 16.dp.toPx(), normalized)
                } else {
                    lerp(8.dp.toPx(), 2.dp.toPx(), -normalized)
                }) + 2.dp.toPx() * progress,
            )
            lens(
                size.minDimension / 4f + 2.dp.toPx() * progress,
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
                0.12f,
                0.50f,
                ((luminance - 0.3f) / 0.5f).coerceIn(0f, 1f),
            )
            drawRect((if (isDark) Color.Black else Color.White).copy(alpha = scrim))

            if (progress > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.18f * progress),
                            Color.Transparent,
                        ),
                        center = press.position,
                        radius = size.minDimension * 1.2f,
                    ),
                    blendMode = BlendMode.Plus,
                )
            }
        },
        layerBlock = {
            val scale = lerp(1f, 1.12f, progress)
            scaleX = scale
            scaleY = scale
        },
    )
}

private fun Modifier.selectionGlass(
    enabled: Boolean,
    isDark: Boolean,
    backdrop: LayerBackdrop?,
    luminance: Float,
    pressProgress: Float,
): Modifier {
    if (!enabled || backdrop == null) {
        return clip(NavigationCapsuleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f))
    }

    return drawBackdrop(
        backdrop = backdrop,
        shape = { NavigationCapsuleShape },
        effects = {
            val normalized = (luminance * 2f - 1f).let { sign(it) * it * it }
            vibrancy()
            colorControls(
                brightness = 0.05f,
                contrast = 1f,
                saturation = 1.5f,
            )
            blur(
                (if (normalized > 0f) {
                    lerp(8.dp.toPx(), 16.dp.toPx(), normalized)
                } else {
                    lerp(8.dp.toPx(), 2.dp.toPx(), -normalized)
                }) + 20.dp.toPx(),
            )
            lens(
                10.dp.toPx() * pressProgress,
                14.dp.toPx() * pressProgress,
                chromaticAberration = true,
            )
        },
        highlight = { Highlight.Default.copy(alpha = 0.6f) },
        shadow = { Shadow(radius = 4.dp, alpha = 0.4f) },
        innerShadow = {
            InnerShadow(
                radius = 8.dp * pressProgress,
                alpha = pressProgress,
            )
        },
        onDrawSurface = {
            val normalized = ((luminance - 0.3f) / 0.5f).coerceIn(0f, 1f)
            val alpha = if (isDark) {
                lerp(0.22f, 0.55f, normalized)
            } else {
                lerp(0.06f, 0.14f, normalized)
            }
            drawRect(Color.Black.copy(alpha = alpha))
        },
    )
}

private class NavigationGlassPress {
    private val animation = Animatable(0f)
    var position by mutableStateOf(Offset.Zero)
        private set

    val progress: Float
        get() = animation.value

    fun down(scope: kotlinx.coroutines.CoroutineScope, offset: Offset) {
        position = offset
        scope.launch {
            animation.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f,
                    visibilityThreshold = 0.001f,
                ),
            )
        }
    }

    fun move(offset: Offset) {
        position = offset
    }

    fun up(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            animation.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f,
                    visibilityThreshold = 0.001f,
                ),
            )
        }
    }
}

@Composable
private fun rememberNavigationGlassPress(): NavigationGlassPress =
    remember { NavigationGlassPress() }

private suspend fun PointerInputScope.observeNavigationPress(
    press: NavigationGlassPress,
) {
    val scope = kotlinx.coroutines.CoroutineScope(coroutineContext)
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val down = awaitFirstDown(requireUnconsumed = false)
        press.down(scope, down.position)
        press.move(down.position)

        val up = dragNavigationPointer(down.id) { change ->
            press.move(change.position)
        }

        press.up(scope)
        if (up != null) press.move(up.position)
    }
}

private suspend inline fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.dragNavigationPointer(
    pointerId: PointerId,
    onMove: (PointerInputChange) -> Unit,
): PointerInputChange? {
    if (currentEvent.changes.fastFirstOrNull { it.id == pointerId }?.pressed != true) return null

    var pointer = pointerId
    while (true) {
        val change = awaitNavigationDragOrUp(pointer) ?: return null
        if (change.isConsumed) return null
        if (change.changedToUpIgnoreConsumed()) return change
        onMove(change)
        pointer = change.id
    }
}

private suspend inline fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.awaitNavigationDragOrUp(
    pointerId: PointerId,
): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (change.changedToUpIgnoreConsumed()) {
            val other = event.changes.fastFirstOrNull { it.pressed }
            if (other == null) return change
            pointer = other.id
        } else if (change.previousPosition != change.position) {
            return change
        }
    }
}

private class NavigationIndicatorMotion(
    initialIndex: Int,
    private val maxIndex: Int,
) {
    private val positionAnimation = Animatable(initialIndex.toFloat())
    private val pressAnimation = Animatable(0f)
    private val scaleAnimation = Animatable(1f)

    var velocity by mutableFloatStateOf(0f)
        private set

    val position: Float
        get() = positionAnimation.value

    val pressProgress: Float
        get() = pressAnimation.value

    val scale: Float
        get() = scaleAnimation.value

    suspend fun dragBy(
        deltaPx: Float,
        tabWidthPx: Float,
        direction: Float,
    ) {
        if (tabWidthPx <= 0f) return
        val deltaTabs = deltaPx / tabWidthPx * direction
        velocity = deltaTabs * 10f
        positionAnimation.snapTo(
            (positionAnimation.value + deltaTabs).coerceIn(0f, maxIndex.toFloat()),
        )
    }

    fun nearestIndex(): Int =
        positionAnimation.value.roundToInt().coerceIn(0, maxIndex)

    suspend fun animateTo(index: Int) {
        positionAnimation.animateTo(
            targetValue = index.coerceIn(0, maxIndex).toFloat(),
            animationSpec = spring(
                dampingRatio = 1f,
                stiffness = Spring.StiffnessHigh,
                visibilityThreshold = 0.001f,
            ),
        )
        velocity = 0f
    }

    fun animateTo(
        scope: kotlinx.coroutines.CoroutineScope,
        index: Int,
    ) {
        scope.launch { animateTo(index) }
    }

    fun press(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            launch {
                pressAnimation.animateTo(
                    1f,
                    spring(
                        dampingRatio = 1f,
                        stiffness = Spring.StiffnessHigh,
                        visibilityThreshold = 0.001f,
                    ),
                )
            }
            launch {
                scaleAnimation.animateTo(
                    76f / 56f,
                    spring(
                        dampingRatio = 0.6f,
                        stiffness = 250f,
                        visibilityThreshold = 0.001f,
                    ),
                )
            }
        }
    }

    fun release(
        scope: kotlinx.coroutines.CoroutineScope,
        target: Int,
    ) {
        scope.launch {
            launch { animateTo(target) }
            launch {
                pressAnimation.animateTo(
                    0f,
                    spring(
                        dampingRatio = 1f,
                        stiffness = Spring.StiffnessHigh,
                        visibilityThreshold = 0.001f,
                    ),
                )
            }
            launch {
                scaleAnimation.animateTo(
                    1f,
                    spring(
                        dampingRatio = 0.7f,
                        stiffness = 250f,
                        visibilityThreshold = 0.001f,
                    ),
                )
            }
            velocity = 0f
        }
    }
}
