package com.freetime.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FreetimeText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = FreetimeDesign.typography.bodyLarge,
    color: Color = FreetimeDesign.colors.contentStrong,
    maxLines: Int = Int.MAX_VALUE,
) = BasicText(text, modifier, style.copy(color = color), maxLines = maxLines)

@Composable
fun FreetimeScreen(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        topBar?.invoke()
        Box(Modifier.weight(1f).fillMaxWidth(), content = content)
        bottomBar?.invoke()
    }
}

@Composable
fun FreetimeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.xs)) {
            FreetimeText(title, style = FreetimeDesign.typography.titleMedium)
            if (!subtitle.isNullOrBlank()) FreetimeText(subtitle, style = FreetimeDesign.typography.bodySmall, color = FreetimeDesign.colors.contentMuted)
        }
        trailing?.invoke()
    }
}

@Composable
fun FreetimeListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier.fillMaxWidth().defaultMinSize(minHeight = FreetimeDesign.sizes.touchTarget)
    Row(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(horizontal = FreetimeDesign.spacing.md, vertical = FreetimeDesign.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md),
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.xs)) {
            FreetimeText(title)
            if (!subtitle.isNullOrBlank()) FreetimeText(subtitle, style = FreetimeDesign.typography.bodySmall, color = FreetimeDesign.colors.contentMuted)
        }
        trailing?.invoke()
    }
}

@Composable
fun FreetimeOptionGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.xs)) {
        options.forEach { (value, label) ->
            FreetimeChoiceSetting(label, selected == value) { onSelect(value) }
        }
    }
}

@Composable
fun FreetimeStatusBanner(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    FreetimeGlassPanel(modifier.fillMaxWidth(), depth = FreetimeGlassDepth.SUBTLE) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.sm)) {
            FreetimeText(message, Modifier.weight(1f), style = FreetimeDesign.typography.bodyMedium)
            if (actionLabel != null && onAction != null) FreetimeButton(actionLabel, onAction)
        }
    }
}

@Composable
fun FreetimeEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(FreetimeDesign.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md),
    ) {
        icon?.invoke()
        FreetimeText(title, style = FreetimeDesign.typography.titleLarge)
        if (!message.isNullOrBlank()) FreetimeText(message, style = FreetimeDesign.typography.bodyMedium, color = FreetimeDesign.colors.contentMuted)
        if (actionLabel != null && onAction != null) FreetimeButton(actionLabel, onAction)
    }
}

@Composable
fun FreetimeLoadingState(modifier: Modifier = Modifier, message: String? = null) {
    Column(
        modifier.fillMaxWidth().padding(FreetimeDesign.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md),
    ) {
        FreetimeProgressIndicator()
        if (!message.isNullOrBlank()) FreetimeText(message, style = FreetimeDesign.typography.bodyMedium, color = FreetimeDesign.colors.contentMuted)
    }
}

@Composable
fun FreetimeErrorState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
) = FreetimeEmptyState(title, modifier, message, retryLabel, onRetry)

@Composable
fun FreetimeDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(FreetimeDesign.colors.glassBorder))
}

@Composable
fun FreetimeBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier.freetimeGlassCapsule(interactive = false)
            .padding(horizontal = FreetimeDesign.spacing.sm, vertical = FreetimeDesign.spacing.xs),
        contentAlignment = Alignment.Center,
    ) {
        FreetimeText(text, style = FreetimeDesign.typography.labelSmall)
    }
}

@Composable
fun FreetimeInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    FreetimeCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(FreetimeDesign.spacing.md)) {
            FreetimeSectionHeader(title, subtitle = subtitle)
            content()
        }
    }
}
