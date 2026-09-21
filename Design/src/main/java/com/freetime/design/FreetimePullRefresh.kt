package com.freetime.design

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Stable
class FreetimePullRefreshState internal constructor(
    val refreshing: Boolean,
    private val onRefresh: () -> Unit,
) {
    var pullDistance by mutableFloatStateOf(0f)
        internal set

    internal fun release(thresholdPx: Float) {
        if (!refreshing && pullDistance >= thresholdPx) onRefresh()
        pullDistance = 0f
    }
}

@Composable
fun rememberFreetimePullRefreshState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
): FreetimePullRefreshState {
    val currentRefresh by rememberUpdatedState(onRefresh)
    return remember(refreshing) { FreetimePullRefreshState(refreshing) { currentRefresh() } }
}

@Composable
fun FreetimePullRefresh(
    state: FreetimePullRefreshState,
    modifier: Modifier = Modifier,
    threshold: androidx.compose.ui.unit.Dp = 72.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    Box(
        modifier
            .fillMaxSize()
            .pointerInput(state, thresholdPx) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, amount ->
                        if (amount > 0f && !state.refreshing) {
                            state.pullDistance = (state.pullDistance + amount * .5f).coerceAtMost(thresholdPx * 1.5f)
                        }
                    },
                    onDragEnd = { state.release(thresholdPx) },
                    onDragCancel = { state.pullDistance = 0f },
                )
            }
    ) {
        content()
        FreetimeGlassPullRefreshIndicator(
            refreshing = state.refreshing || state.pullDistance > 8f,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp),
        )
    }
}
