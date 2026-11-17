@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Signatures verified against the published material3-android 1.5.0-rc01 sources.
// Selection, navigation, nested-scroll connections and whole-surface visibility belong to callers.
// Insets are consumed by the native component, not again by the glass surface.

@Composable
fun LiquidGlassTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
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
fun LiquidGlassCenterAlignedTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        CenterAlignedTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
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
fun LiquidGlassMediumTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    collapsedHeight: Dp = TopAppBarDefaults.MediumAppBarCollapsedHeight,
    expandedHeight: Dp = TopAppBarDefaults.MediumAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        MediumTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
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
fun LiquidGlassLargeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    collapsedHeight: Dp = TopAppBarDefaults.LargeAppBarCollapsedHeight,
    expandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    shape: Shape = RectangleShape,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        LargeTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
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
fun LiquidGlassBottomAppBar(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    contentPadding: PaddingValues = BottomAppBarDefaults.ContentPadding,
    windowInsets: WindowInsets = BottomAppBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        BottomAppBar(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            contentPadding = contentPadding,
            windowInsets = windowInsets,
            content = content,
        )
    }
}

@Composable
fun LiquidGlassNavigationBar(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            windowInsets = windowInsets,
            content = content,
        )
    }
}

@Composable
fun LiquidGlassShortNavigationBar(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    windowInsets: WindowInsets = ShortNavigationBarDefaults.windowInsets,
    arrangement: ShortNavigationBarArrangement = ShortNavigationBarDefaults.arrangement,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        ShortNavigationBar(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            windowInsets = windowInsets,
            arrangement = arrangement,
            content = content,
        )
    }
}

@Composable
fun LiquidGlassNavigationRail(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    windowInsets: WindowInsets = NavigationRailDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        NavigationRail(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            header = header,
            windowInsets = windowInsets,
            content = content,
        )
    }
}

/** Place in a caller-owned ModalNavigationDrawer; pass its state for predictive back support. */
@Composable
fun LiquidGlassNavigationDrawerSheet(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    shape: Shape = DrawerDefaults.shape,
    windowInsets: WindowInsets = DrawerDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        // This is a real modal sheet, not a replacement for the caller's drawer host or state.
        ModalDrawerSheet(
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

@Composable
fun LiquidGlassPrimaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    tabs: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            tabs = tabs,
        )
    }
}

@Composable
fun LiquidGlassSecondaryTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    tabs: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            tabs = tabs,
        )
    }
}

/** Apply hide/scroll positioning to [modifier] so the glass moves with the entire toolbar. */
@Composable
fun LiquidGlassHorizontalFloatingToolbar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    contentPadding: PaddingValues = FloatingToolbarDefaults.ContentPadding,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        HorizontalFloatingToolbar(
            expanded = expanded,
            shape = shape,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = Color.Transparent,
                toolbarContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            contentPadding = contentPadding,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            expandedShadowElevation = 0.dp,
            collapsedShadowElevation = 0.dp,
            content = content,
        )
    }
}

/** A null [progress] uses the native indeterminate indicator; otherwise progress stays caller-owned. */
@Composable
fun LiquidGlassLoadingIndicator(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(8.dp),
    color: Color = LoadingIndicatorDefaults.indicatorColor,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        if (progress == null) {
            LoadingIndicator(color = color)
        } else {
            LoadingIndicator(progress = progress, color = color)
        }
    }
}
