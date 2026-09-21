package com.freetime.design

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kyant.shapes.Capsule

@Deprecated("Use FreetimeDepth instead")
enum class FreetimeGlassDepth { SUBTLE, STANDARD, ELEVATED }
enum class FreetimeHaptic { NONE, LIGHT, CONFIRM, WARNING }

@Deprecated("Use FreetimeDesign.spacing, FreetimeDesign.sizes and FreetimeDesign.shapes")
object FreetimeDesignTokens {
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val touchTarget = 48.dp
    val radiusSubtle = 22.dp
    val radiusStandard = 30.dp
    val radiusElevated = 36.dp
}

@Composable
fun rememberFreetimeReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

@Composable
fun rememberFreetimeGlassPerformance(): Float =
    remember { if (Build.VERSION.SDK_INT >= 33) 1f else if (Build.VERSION.SDK_INT >= 29) .72f else .5f }

@Composable
fun Modifier.freetimeDepth(depth: FreetimeGlassDepth, interactive: Boolean = false): Modifier {
    val shape = when (depth) {
        FreetimeGlassDepth.SUBTLE -> RoundedCornerShape(FreetimeDesignTokens.radiusSubtle)
        FreetimeGlassDepth.STANDARD -> RoundedCornerShape(FreetimeDesignTokens.radiusStandard)
        FreetimeGlassDepth.ELEVATED -> RoundedCornerShape(FreetimeDesignTokens.radiusElevated)
    }
    return freetimeGlass(shape, interactive)
}

@Composable
fun FreetimeGlassPanel(
    modifier: Modifier = Modifier,
    depth: FreetimeGlassDepth = FreetimeGlassDepth.STANDARD,
    interactive: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = when (depth) {
        FreetimeGlassDepth.SUBTLE -> RoundedCornerShape(FreetimeDesignTokens.radiusSubtle)
        FreetimeGlassDepth.STANDARD -> RoundedCornerShape(FreetimeDesignTokens.radiusStandard)
        FreetimeGlassDepth.ELEVATED -> RoundedCornerShape(FreetimeDesignTokens.radiusElevated)
    }
    val padding = when (depth) {
        FreetimeGlassDepth.SUBTLE -> 14.dp
        FreetimeGlassDepth.STANDARD -> 18.dp
        FreetimeGlassDepth.ELEVATED -> 22.dp
    }
    Box(modifier.freetimeGlass(shape, interactive).padding(padding), content = content)
}

@Composable
fun FreetimeGlassAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    haptic: FreetimeHaptic = FreetimeHaptic.LIGHT,
    content: @Composable RowScope.() -> Unit,
) {
    val feedback = LocalHapticFeedback.current
    Row(
        modifier = modifier.defaultMinSize(minHeight = FreetimeDesignTokens.touchTarget)
            .freetimeGlassCapsule()
            .clickable {
                when (haptic) {
                    FreetimeHaptic.NONE -> Unit
                    FreetimeHaptic.LIGHT -> feedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    FreetimeHaptic.CONFIRM -> feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    FreetimeHaptic.WARNING -> feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                onClick()
            }.padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun FreetimeGlassTopBar(
    title: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    subtitle: String? = null,
    navigation: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = if (compact) 26.dp else 16.dp, vertical = if (compact) 6.dp else 10.dp)
            .freetimeGlass(RoundedCornerShape(if (compact) 26.dp else 32.dp), false)
            .animateContentSize().padding(horizontal = 10.dp, vertical = if (compact) 4.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigation()
        Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
            BasicText(title, style = if (compact) FreetimeDesign.typography.titleMedium else FreetimeDesign.typography.titleLarge, maxLines = 1)
            if (compact && !subtitle.isNullOrBlank()) BasicText(subtitle, style = FreetimeDesign.typography.labelSmall.copy(color = FreetimeDesign.colors.contentMuted), maxLines = 1)
        }
        actions()
    }
}

@Composable
fun FreetimeFloatingNavigation(
    compact: Boolean,
    modifier: Modifier = Modifier,
    primary: @Composable RowScope.() -> Unit,
    secondary: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.freetimeGlassCapsule(false).animateContentSize().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        primary()
        AnimatedVisibility(!compact, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), content = secondary)
        }
    }
}

@Composable
fun rememberFreetimeCompactNavigation(state: LazyListState, thresholdPx: Int = 120): Boolean =
    remember(state, thresholdPx) { derivedStateOf { state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > thresholdPx } }.value

@Composable
fun FreetimeSettingsGroup(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    FreetimeGlassPanel(modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BasicText(title, style = FreetimeDesign.typography.titleMedium)
            content()
        }
    }
}

@Composable
fun FreetimeSwitchSetting(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, description: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            BasicText(title)
            if (description != null) BasicText(description, style = FreetimeDesign.typography.bodySmall.copy(color = FreetimeDesign.colors.contentMuted))
        }
        FreetimeSwitch(checked, onCheckedChange)
    }
}

@Composable
fun FreetimeChoiceSetting(title: String, selected: Boolean, onClick: () -> Unit) {
    FreetimeGlassAction(onClick, Modifier.fillMaxWidth(), haptic = FreetimeHaptic.LIGHT) {
        BasicText(title, Modifier.weight(1f))
        if (selected) BasicText("✓")
    }
}

@Composable
fun FreetimeGlassSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String = "", modifier: Modifier = Modifier) {
    FreetimeTextField(value, onValueChange, modifier, placeholder = placeholder, singleLine = true)
}

@Composable
fun FreetimeGlassSnackbar(message: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.freetimeGlassCapsule(false).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        BasicText(message, Modifier.weight(1f))
        if (actionLabel != null && onAction != null) FreetimeButton(actionLabel, onAction)
    }
}

@Composable
fun FreetimeGlassSkeleton(modifier: Modifier = Modifier, height: Dp = 72.dp) {
    val reduced = rememberFreetimeReducedMotion()
    val transition = rememberInfiniteTransition(label = "freetime-skeleton")
    val alpha = if (reduced) .32f else transition.animateFloat(.18f, .42f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "alpha").value
    Box(modifier.fillMaxWidth().height(height).freetimeGlass(RoundedCornerShape(24.dp), false).alpha(alpha))
}

@Composable
fun FreetimeGlassPullRefreshIndicator(refreshing: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(refreshing, modifier = modifier) {
        Box(Modifier.freetimeGlassCapsule(false).padding(12.dp), contentAlignment = Alignment.Center) {
            FreetimeProgressIndicator(Modifier.size(22.dp))
        }
    }
}


@Composable
private fun FreetimeIcon(imageVector: ImageVector, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = FreetimeDesign.colors.contentStrong) {
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint),
    )
}

@Composable
fun FreetimeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    tint: Color = Color.Unspecified,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (pressed) .82f else if (enabled) 1f else .45f, label = "freetime-button")
    Row(
        modifier.defaultMinSize(minHeight = FreetimeDesign.sizes.buttonHeight)
            .freetimeGlassCapsule(enabled, tint = tint)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp).alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (leadingIcon != null) FreetimeIcon(leadingIcon, null, Modifier.size(20.dp), FreetimeDesign.colors.contentStrong)
        BasicText(text, style = FreetimeDesign.typography.labelLarge.copy(color = FreetimeDesign.colors.contentStrong))
    }
}

@Composable
fun FreetimeIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    Box(
        modifier.size(FreetimeDesign.sizes.iconButton).freetimeRoundGlass(enabled, tint = tint)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else .45f),
        contentAlignment = Alignment.Center,
    ) {
        FreetimeIcon(icon, contentDescription, Modifier.size(22.dp), FreetimeDesign.colors.contentStrong)
    }
}

@Composable
fun FreetimeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val base = modifier.freetimeGlass(LocalFreetimeShapes.current.surface, interactive = onClick != null, tint = tint)
    Box(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base).padding(18.dp),
        content = content,
    )
}

@Composable
fun FreetimeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.defaultMinSize(minHeight = 60.dp).freetimeGlassCapsule(false).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigation?.invoke()
        BasicText(title, Modifier.weight(1f).padding(horizontal = 12.dp), style = FreetimeDesign.typography.titleMedium.copy(color = FreetimeDesign.colors.contentStrong))
        actions()
    }
}

@Composable
fun RowScope.FreetimeNavigationItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(if (selected) FreetimeDesign.colors.contentStrong else FreetimeDesign.colors.contentMuted, label = "freetime-nav-color")
    Column(
        modifier.weight(1f).clickable(role = Role.Tab, onClick = onClick).padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            (if (selected) Modifier.freetimeSelectedGlassCapsule() else Modifier).padding(horizontal = 15.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            FreetimeIcon(icon, label, Modifier.size(22.dp), color)
        }
        BasicText(label, style = FreetimeDesign.typography.labelSmall.copy(color = color), maxLines = 1)
    }
}

@Composable
fun FreetimeChip(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = if (selected) FreetimeDesign.palette.primary else Color.Unspecified,
) {
    val color by animateColorAsState(if (selected) FreetimeDesign.colors.contentStrong else FreetimeDesign.colors.contentMuted, label = "freetime-chip-color")
    Box(
        modifier.defaultMinSize(minHeight = 38.dp).freetimeGlassCapsule(tint = tint)
            .clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = FreetimeDesign.typography.labelMedium.copy(color = color))
    }
}


@Immutable
data class FreetimeNavigationDestination(
    val label: String,
    val icon: ImageVector,
)

@Composable
fun FreetimeBottomBar(
    destinations: List<FreetimeNavigationDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    if (destinations.isEmpty()) return
    val reducedMotion = rememberFreetimeReducedMotion()
    val safeIndex = selectedIndex.coerceIn(destinations.indices)
    BoxWithConstraints(
        modifier = modifier
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .freetimeGlassCapsule(interactive = false)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, destination ->
                val selected = index == safeIndex
                val color by animateColorAsState(
                    targetValue = if (selected) FreetimeDesign.palette.primary else FreetimeDesign.colors.contentMuted,
                    animationSpec = tween(if (reducedMotion) 0 else FreetimeDesign.motion.normalMillis),
                    label = "freetime-bottom-item-color-$index",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = if (compact) 42.dp else 56.dp)
                        .clickable(role = Role.Tab) { onDestinationSelected(index) }
                        .padding(horizontal = 3.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FreetimeIcon(
                        destination.icon,
                        destination.label,
                        Modifier.size(if (compact) FreetimeDesign.sizes.navigationIcon - 2.dp else FreetimeDesign.sizes.navigationIcon),
                        color,
                    )
                    AnimatedVisibility(
                        visible = !compact,
                        enter = fadeIn(tween(if (reducedMotion) 0 else 160)),
                        exit = fadeOut(tween(if (reducedMotion) 0 else 120)),
                    ) {
                        BasicText(
                            destination.label,
                            style = FreetimeDesign.typography.labelSmall.copy(color = color),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FreetimeAdaptiveBottomBar(
    destinations: List<FreetimeNavigationDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thresholdPx: Int = 120,
) {
    FreetimeBottomBar(
        destinations = destinations,
        selectedIndex = selectedIndex,
        onDestinationSelected = onDestinationSelected,
        modifier = modifier,
        compact = rememberFreetimeCompactNavigation(listState, thresholdPx),
    )
}


@Composable
fun FreetimeSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Row(
        modifier = modifier.defaultMinSize(minWidth = 52.dp, minHeight = 32.dp)
            .freetimeGlassCapsule(interactive = enabled)
            .clickable(enabled = enabled, role = Role.Switch) { onCheckedChange(!checked) }
            .padding(4.dp),
        horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(24.dp).background(FreetimeDesign.colors.contentStrong, RoundedCornerShape(12.dp)))
    }
}

@Composable
fun FreetimeTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, placeholder: String = "", label: String? = null, singleLine: Boolean = true, enabled: Boolean = true) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.xs)) {
        if (!label.isNullOrBlank()) BasicText(label, style = FreetimeDesign.typography.labelMedium.copy(color = FreetimeDesign.colors.contentMuted))
        BasicTextField(
            value = value, onValueChange = onValueChange, enabled = enabled, singleLine = singleLine,
            textStyle = FreetimeDesign.typography.bodyLarge.copy(color = FreetimeDesign.colors.contentStrong),
            modifier = Modifier.fillMaxWidth().freetimeGlass(FreetimeDesign.shapes.control, enabled).padding(horizontal = FreetimeDesign.spacing.lg, vertical = FreetimeDesign.spacing.md),
            decorationBox = { inner -> Box { if (value.isEmpty() && placeholder.isNotEmpty()) BasicText(placeholder, style = FreetimeDesign.typography.bodyLarge.copy(color = FreetimeDesign.colors.contentMuted)); inner() } },
        )
    }
}

@Composable
fun FreetimeDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.freetimeWideGlass(LocalFreetimeShapes.current.dialog, interactive = false)
                .padding(FreetimeDesign.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.lg),
        ) {
            FreetimeGlassTitle(title)
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm, Alignment.End),
                content = actions,
            )
        }
    }
}

@Composable
fun FreetimeDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier.freetimeWideGlass(LocalFreetimeShapes.current.dialog, interactive = false)
                .padding(FreetimeDesign.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.lg),
        ) {
            BasicText(title, style = FreetimeDesign.typography.titleLarge.copy(color = FreetimeDesign.colors.contentStrong))
            if (!text.isNullOrBlank()) BasicText(text, style = FreetimeDesign.typography.bodyMedium.copy(color = FreetimeDesign.colors.contentMuted))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm, Alignment.End),
            ) {
                if (dismissText != null) FreetimeButton(dismissText, onDismissRequest)
                FreetimeButton(confirmText, onConfirm)
            }
        }
    }
}

@Composable
fun FreetimeSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.freetimeWideGlass(Capsule(), interactive = false)
            .padding(horizontal = FreetimeDesign.spacing.lg, vertical = FreetimeDesign.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm),
    ) {
        BasicText(message, Modifier.weight(1f), style = FreetimeDesign.typography.bodyMedium.copy(color = FreetimeDesign.colors.contentStrong))
        if (actionLabel != null && onAction != null) FreetimeButton(actionLabel, onAction)
    }
}

@Composable
fun FreetimeProgressIndicator(modifier: Modifier = Modifier, progress: Float? = null) {
    if (progress == null) {
        val borderColor = FreetimeDesign.colors.glassBorder
        val indicatorColor = FreetimeDesign.colors.contentStrong
        Canvas(modifier.size(28.dp)) {
            drawCircle(borderColor)
            drawCircle(indicatorColor, radius = size.minDimension * .22f)
        }
    } else {
        Box(modifier.fillMaxWidth().height(6.dp).background(FreetimeDesign.colors.glassBorder, RoundedCornerShape(50))) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(FreetimeDesign.colors.contentStrong, RoundedCornerShape(50)))
        }
    }
}

@Composable
fun FreetimeSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier, valueRange: ClosedFloatingPointRange<Float> = 0f..1f, enabled: Boolean = true) {
    val fraction = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
    Box(
        modifier = modifier.fillMaxWidth().height(FreetimeDesign.sizes.touchTarget).clickable(enabled = enabled) {
            val next = if (fraction < .5f) .75f else .25f
            onValueChange(valueRange.start + (valueRange.endInclusive - valueRange.start) * next)
        },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(FreetimeDesign.colors.glassBorder, RoundedCornerShape(50)))
        Box(Modifier.fillMaxWidth(fraction).height(6.dp).background(FreetimeDesign.colors.contentStrong, RoundedCornerShape(50)))
    }
}
