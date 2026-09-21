package com.freetime.design

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

enum class FreetimeNavigationLayout { BOTTOM, RAIL }

@Composable
fun FreetimeAdaptiveNavigation(
    destinations: List<FreetimeNavigationDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    railBreakpoint: Dp = 720.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        if (maxWidth >= railBreakpoint) {
            Row(Modifier.fillMaxSize()) {
                FreetimeNavigationRail(
                    destinations = destinations,
                    selectedIndex = selectedIndex,
                    onDestinationSelected = onDestinationSelected,
                )
                Box(Modifier.weight(1f).fillMaxHeight(), content = content)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth(), content = content)
                FreetimeBottomBar(
                    destinations = destinations,
                    selectedIndex = selectedIndex,
                    onDestinationSelected = onDestinationSelected,
                )
            }
        }
    }
}

@Composable
fun FreetimeNavigationRail(
    destinations: List<FreetimeNavigationDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (destinations.isEmpty()) return
    val safeIndex = selectedIndex.coerceIn(destinations.indices)
    Column(
        modifier
            .padding(12.dp)
            .width(88.dp)
            .fillMaxHeight()
            .freetimeWideGlass(interactive = false)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        destinations.forEachIndexed { index, destination ->
            val selected = index == safeIndex
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(if (selected) Modifier.freetimeSelectedGlassCapsule() else Modifier)
                    .clickable(role = Role.Tab) { onDestinationSelected(index) }
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FreetimeIconButton(
                    icon = destination.icon,
                    contentDescription = destination.label,
                    onClick = { onDestinationSelected(index) },
                    tint = if (selected) FreetimeDesign.palette.primary else Color.Unspecified,
                )
                BasicText(
                    destination.label,
                    style = FreetimeDesign.typography.labelSmall.copy(
                        color = if (selected) FreetimeDesign.colors.contentStrong else FreetimeDesign.colors.contentMuted
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

data class FreetimeMenuItem(
    val label: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun FreetimePopupMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<FreetimeMenuItem>,
    modifier: Modifier = Modifier,
) {
    if (!expanded) return
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier
                .widthIn(min = 180.dp, max = 300.dp)
                .freetimeWideGlass(interactive = false)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) {
                            item.onClick()
                            onDismissRequest()
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (item.icon != null) {
                        FreetimeIconButton(
                            icon = item.icon,
                            contentDescription = null,
                            onClick = {
                                item.onClick()
                                onDismissRequest()
                            },
                            enabled = item.enabled,
                        )
                    }
                    BasicText(
                        item.label,
                        style = FreetimeDesign.typography.bodyMedium.copy(
                            color = if (item.enabled) FreetimeDesign.colors.contentStrong else FreetimeDesign.colors.contentMuted
                        ),
                    )
                }
            }
        }
    }
}
