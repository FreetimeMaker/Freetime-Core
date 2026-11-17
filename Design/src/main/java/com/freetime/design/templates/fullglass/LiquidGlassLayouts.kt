package com.freetime.design.templates.fullglass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Layout adapter, not a viewport-sized optical wrapper. Native Scaffold owns measurement and inset
 * handling; supply glass bars, snackbars and FABs through its slots under [LiquidGlassRoot]. Apply
 * and consume the supplied padding in the body just as with Material Scaffold.
 * These layouts stay inline: the cross-window [LiquidGlassOverlaySurface] fallback is not needed.
 */
@Composable
fun LiquidGlassScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = floatingActionButtonPosition,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        contentWindowInsets = contentWindowInsets,
        content = content,
    )
}

/**
 * Native persistent-sheet layout adapter with an optical wrapper only around the inline sheet.
 * The caller owns [scaffoldState]; native anchors, nested scrolling and peek-height padding remain
 * intact. The native handle slot retains its expand/collapse actions and accessibility semantics;
 * callers can supply a glass handle surface there as well.
 * Unlike a modal sheet this shares [LiquidGlassRoot]'s window and must not isolate its backdrop.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassBottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    sheetContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    sheetPeekHeight: Dp = BottomSheetDefaults.SheetPeekHeight,
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    sheetShape: Shape = BottomSheetDefaults.ExpandedShape,
    sheetSwipeEnabled: Boolean = true,
    sheetDragHandle: (@Composable () -> Unit)? = { BottomSheetDefaults.DragHandle() },
    topBar: (@Composable () -> Unit)? = null,
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    content: @Composable (PaddingValues) -> Unit,
) {
    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetMaxWidth = sheetMaxWidth,
        sheetShape = sheetShape,
        sheetContainerColor = Color.Transparent,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 0.dp,
        sheetDragHandle = sheetDragHandle,
        sheetSwipeEnabled = sheetSwipeEnabled,
        topBar = topBar,
        snackbarHost = snackbarHost,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        sheetContent = {
            LiquidGlassSurface(Modifier.fillMaxWidth(), sheetShape, PaddingValues(0.dp)) {
                Column(content = sheetContent)
            }
        },
        content = content,
    )
}

/**
 * Layout adapter retaining native drag, direction and dismissal semantics with caller-owned state
 * and background. Only the moving content is optically wrapped; the background is left untouched.
 * [onDismiss] must perform the caller's action (and reset state if the item remains in composition).
 */
@Composable
fun LiquidGlassSwipeToDismissBox(
    state: SwipeToDismissBoxState,
    onDismiss: (SwipeToDismissBoxValue) -> Unit,
    backgroundContent: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    gesturesEnabled: Boolean = true,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable RowScope.() -> Unit,
) {
    SwipeToDismissBox(
        state = state,
        onDismiss = onDismiss,
        backgroundContent = backgroundContent,
        modifier = modifier,
        enableDismissFromStartToEnd = enableDismissFromStartToEnd,
        enableDismissFromEndToStart = enableDismissFromEndToStart,
        gesturesEnabled = gesturesEnabled,
    ) {
        LiquidGlassSurface(Modifier.fillMaxWidth(), shape, contentPadding) {
            Row(content = content)
        }
    }
}

/**
 * Native nested-scroll layout adapter, not a glass wrapper around the scrolling viewport. Native
 * IndicatorBox owns indicator translation and clipping; only its surface is glass. The default
 * indicator is a determinate Material ring while pulling and indeterminate while refreshing.
 * Supply [indicatorContent] for other visuals without replacing the native gesture machinery.
 * [state], [isRefreshing] and [onRefresh] are all caller-owned; use a scrollable body.
 */
@Composable
fun LiquidGlassPullToRefreshBox(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    threshold: Dp = PullToRefreshDefaults.PositionalThreshold,
    contentAlignment: Alignment = Alignment.TopStart,
    indicatorContent: @Composable BoxScope.() -> Unit = {
        if (isRefreshing) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            CircularProgressIndicator(
                progress = { state.distanceFraction.coerceIn(0f, 1f) },
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        }
    },
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        enabled = enabled,
        threshold = threshold,
        contentAlignment = contentAlignment,
        indicator = {
            PullToRefreshDefaults.IndicatorBox(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.Transparent,
                elevation = 0.dp,
            ) {
                LiquidGlassSurface(
                    Modifier.fillMaxSize(),
                    PullToRefreshDefaults.indicatorShape,
                    PaddingValues(0.dp),
                ) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
                        content = indicatorContent,
                    )
                }
            }
        },
        content = content,
    )
}
