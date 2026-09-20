package me.free_time.design

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class FreetimeGlassDepth { SUBTLE, STANDARD, ELEVATED }
enum class FreetimeHaptic { NONE, LIGHT, CONFIRM, WARNING }

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

fun Modifier.freetimeDepth(depth: FreetimeGlassDepth, interactive: Boolean = false): Modifier {
    val shape = when (depth) {
        FreetimeGlassDepth.SUBTLE -> RoundedCornerShape(FreetimeDesignTokens.radiusSubtle)
        FreetimeGlassDepth.STANDARD -> RoundedCornerShape(FreetimeDesignTokens.radiusStandard)
        FreetimeGlassDepth.ELEVATED -> RoundedCornerShape(FreetimeDesignTokens.radiusElevated)
    }
    return this.then(Modifier).let { base -> base }
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
            Text(title, style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, maxLines = 1)
            if (compact && !subtitle.isNullOrBlank()) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
fun FreetimeSwitchSetting(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, description: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onCheckedChange)
    }
}

@Composable
fun FreetimeChoiceSetting(title: String, selected: Boolean, onClick: () -> Unit) {
    FreetimeGlassAction(onClick, Modifier.fillMaxWidth(), haptic = FreetimeHaptic.LIGHT) {
        Text(title, Modifier.weight(1f))
        if (selected) Text("✓")
    }
}

@Composable
fun FreetimeGlassSearchField(value: String, onValueChange: (String) -> Unit, placeholder: String = "", modifier: Modifier = Modifier) {
    OutlinedTextField(value, onValueChange, modifier.fillMaxWidth().freetimeGlass(RoundedCornerShape(24.dp), true), placeholder = { Text(placeholder) }, singleLine = true)
}

@Composable
fun FreetimeGlassSnackbar(message: String, modifier: Modifier = Modifier, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    Row(modifier.freetimeGlassCapsule(false).padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(message, Modifier.weight(1f))
        if (actionLabel != null && onAction != null) TextButton(onClick = onAction) { Text(actionLabel) }
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
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
        }
    }
}
