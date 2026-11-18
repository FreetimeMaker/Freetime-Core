@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider

// Native signatures verified against the published material3-android 1.5.0-rc01 sources.
// Hosts, state, selection callbacks and nested-scroll connections remain caller-owned.

@Composable
fun LiquidGlassMediumFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    collapsedHeight: Dp = TopAppBarDefaults.MediumAppBarCollapsedHeight,
    expandedHeight: Dp = if (subtitle != null) {
        TopAppBarDefaults.MediumFlexibleAppBarWithSubtitleExpandedHeight
    } else {
        TopAppBarDefaults.MediumFlexibleAppBarWithoutSubtitleExpandedHeight
    },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        MediumFlexibleTopAppBar(
            title = title,
            subtitle = subtitle,
            navigationIcon = navigationIcon,
            actions = actions,
            titleHorizontalAlignment = titleHorizontalAlignment,
            collapsedHeight = collapsedHeight,
            expandedHeight = expandedHeight,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
fun LiquidGlassLargeFlexibleTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: (@Composable () -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    titleHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
    collapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    expandedHeight: Dp = if (subtitle != null) {
        TopAppBarDefaults.LargeFlexibleAppBarWithSubtitleExpandedHeight
    } else {
        TopAppBarDefaults.LargeFlexibleAppBarWithoutSubtitleExpandedHeight
    },
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        LargeFlexibleTopAppBar(
            title = title,
            subtitle = subtitle,
            navigationIcon = navigationIcon,
            actions = actions,
            titleHorizontalAlignment = titleHorizontalAlignment,
            collapsedHeight = collapsedHeight,
            expandedHeight = expandedHeight,
            windowInsets = windowInsets,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
fun LiquidGlassPrimaryScrollableTabRow(
    selectedTabIndex: Int,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentColor: Color = TabRowDefaults.primaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    minTabWidth: Dp = TabRowDefaults.ScrollableTabRowMinTabWidth,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        TabRowDefaults.PrimaryIndicator(
            Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
            width = Dp.Unspecified,
        )
    },
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            scrollState = scrollState,
            containerColor = Color.Transparent,
            contentColor = contentColor,
            edgePadding = edgePadding,
            minTabWidth = minTabWidth,
            indicator = indicator,
            divider = divider,
            tabs = tabs,
        )
    }
}

@Composable
fun LiquidGlassSecondaryScrollableTabRow(
    selectedTabIndex: Int,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentColor: Color = TabRowDefaults.secondaryContentColor,
    edgePadding: Dp = TabRowDefaults.ScrollableTabRowEdgeStartPadding,
    minTabWidth: Dp = TabRowDefaults.ScrollableTabRowMinTabWidth,
    indicator: @Composable TabIndicatorScope.() -> Unit = {
        TabRowDefaults.SecondaryIndicator(
            Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false),
        )
    },
    divider: @Composable () -> Unit = { HorizontalDivider() },
    tabs: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            scrollState = scrollState,
            containerColor = Color.Transparent,
            contentColor = contentColor,
            edgePadding = edgePadding,
            minTabWidth = minTabWidth,
            indicator = indicator,
            divider = divider,
            tabs = tabs,
        )
    }
}

@Composable
fun LiquidGlassWideNavigationRail(
    state: WideNavigationRailState,
    modifier: Modifier = Modifier,
    shape: Shape = WideNavigationRailDefaults.shape,
    header: (@Composable () -> Unit)? = null,
    windowInsets: WindowInsets = WideNavigationRailDefaults.windowInsets,
    arrangement: Arrangement.Vertical = WideNavigationRailDefaults.arrangement,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        WideNavigationRail(
            state = state,
            shape = shape,
            colors = WideNavigationRailDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            header = header,
            windowInsets = windowInsets,
            arrangement = arrangement,
            content = content,
        )
    }
}

/** Sheet slot for a caller-owned PermanentNavigationDrawer. */
@Composable
fun LiquidGlassPermanentDrawerSheet(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    windowInsets: WindowInsets = DrawerDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        PermanentDrawerSheet(
            drawerShape = shape,
            drawerContainerColor = Color.Transparent,
            drawerContentColor = MaterialTheme.colorScheme.onSurface,
            drawerTonalElevation = 0.dp,
            windowInsets = windowInsets,
            content = content,
        )
    }
}

/** Pass the host DismissibleNavigationDrawer's state to retain native back handling. */
@Composable
fun LiquidGlassDismissibleDrawerSheet(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    windowInsets: WindowInsets = DrawerDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        DismissibleDrawerSheet(
            drawerState = drawerState,
            drawerShape = shape,
            drawerContainerColor = Color.Transparent,
            drawerContentColor = MaterialTheme.colorScheme.onSurface,
            drawerTonalElevation = 0.dp,
            windowInsets = windowInsets,
            content = content,
        )
    }
}

/** Native popup and interaction behavior; no page backdrop is sampled across windows. */
@Composable
fun LiquidGlassRichTooltip(
    state: TooltipState,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(
        TooltipAnchorPosition.Above,
    ),
    onDismissRequest: (() -> Unit)? = null,
    focusable: Boolean = false,
    enableUserInput: Boolean = true,
    maxWidth: Dp = TooltipDefaults.richTooltipMaxWidth,
    shape: Shape = TooltipDefaults.richTooltipContainerShape,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        state = state,
        modifier = modifier,
        positionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        focusable = focusable,
        enableUserInput = enableUserInput,
        hasAction = action != null,
        tooltip = {
            // No caret: the opaque fallback and native tooltip share one clipped outline.
            LiquidGlassOverlaySurface(shape = shape, contentPadding = PaddingValues(0.dp)) {
                RichTooltip(
                    title = title,
                    action = action,
                    maxWidth = maxWidth,
                    shape = shape,
                    colors = TooltipDefaults.richTooltipColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    text = text,
                )
            }
        },
        content = content,
    )
}
