package com.freetime.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule

/**
 * Freetime-native screen layout that keeps top, content and floating bottom
 * surfaces independent from Material Scaffold.
 */
@Composable
fun FreetimeScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(Modifier.fillMaxSize()) {
            if (topBar != null) Box(Modifier.fillMaxWidth()) { topBar() }
            Box(Modifier.weight(1f).fillMaxWidth(), content = content)
            if (bottomBar != null) Box(Modifier.fillMaxWidth()) { bottomBar() }
        }
        if (floatingActionButton != null) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = if (bottomBar == null) 24.dp else 100.dp)
            ) { floatingActionButton() }
        }
    }
}

@Composable
fun FreetimeFloatingActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FreetimeDesign.palette.primary,
) {
    FreetimeIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier.size(58.dp),
        tint = tint,
    )
}

@Composable
fun FreetimeExtendedFloatingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = FreetimeDesign.palette.primary,
) {
    FreetimeButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = icon,
        tint = tint,
    )
}

@Composable
fun FreetimeTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FreetimeDesign.palette.primary,
) {
    if (tabs.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(tabs.indices)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .freetimeGlassCapsule(interactive = false)
            .padding(4.dp)
    ) {
        val slotWidth = maxWidth / tabs.size
        Box(
            Modifier
                .width(slotWidth)
                .height(44.dp)
                .offset(x = slotWidth * safeIndex)
                .freetimeSelectedGlassCapsule(tint = tint)
        )
        Row(Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, label ->
                val selected = index == safeIndex
                val color by animateColorAsState(
                    if (selected) FreetimeDesign.colors.contentStrong else FreetimeDesign.colors.contentMuted,
                    label = "freetime-tab-$index",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable(role = Role.Tab) { onTabSelected(index) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(label, style = FreetimeDesign.typography.labelMedium.copy(color = color), maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun FreetimeBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return
    Box(
        Modifier
            .fillMaxSize()
            .clickable(onClick = onDismissRequest),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .padding(12.dp)
                .freetimeWideGlass(interactive = false)
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
