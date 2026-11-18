@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.gestures.TargetedFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupMenuState
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Adapters target Compose Multiplatform Material 3 1.10.0-alpha05:
 * ButtonGroup.kt, SplitButton.kt, ToggleButton.kt, WavyProgressIndicator.kt,
 * FloatingToolbar.kt and carousel/Carousel.kt (commonMain/androidx/compose/material3).
 * These adapters add only a glass parent, not replacement state or gesture handling.
 */

/**
 * Native expressive group DSL over glass. Supply the native overflow indicator using its menu
 * state; the native group handles overflow. The DSL is deliberately not a composable RowScope.
 * Items retain their own Material paint; custom items can choose transparent native colors and
 * share an interaction source with ButtonGroupScope.animateWidth for native press expansion.
 * [verticalAlignment] aligns the group in its available height; this Material version does not
 * expose per-item vertical alignment. [modifier], [shape] and [contentPadding] configure the parent.
 */
@Composable
fun LiquidGlassButtonGroup(
    overflowIndicator: @Composable (ButtonGroupMenuState) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    expandedRatio: Float = ButtonGroupDefaults.ExpandedRatio,
    horizontalArrangement: Arrangement.Horizontal = ButtonGroupDefaults.HorizontalArrangement,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: ButtonGroupScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        ButtonGroup(
            overflowIndicator = overflowIndicator,
            expandedRatio = expandedRatio,
            horizontalArrangement = horizontalArrangement,
            modifier = Modifier.wrapContentHeight(verticalAlignment),
            content = content,
        )
    }
}

/**
 * Native split layout over a glass parent. Supply real leading and trailing controls (for example
 * SplitButtonDefaults.LeadingButton/TrailingButton); they own clicks, checked state and semantics.
 * The layout has no container colors: child controls keep their own paint unless the caller
 * explicitly supplies transparent colors. This does not install another click target.
 * [modifier], [shape] and [contentPadding] configure the glass parent only.
 */
@Composable
fun LiquidGlassSplitButton(
    leadingButton: @Composable () -> Unit,
    trailingButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    spacing: Dp = SplitButtonDefaults.Spacing,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        SplitButtonLayout(
            leadingButton = leadingButton,
            trailingButton = trailingButton,
            spacing = spacing,
        )
    }
}

/**
 * Native determinate wavy indicator over glass; pass null [progress] for native indeterminate
 * animation. Native indicator/track paint and accessibility semantics are retained. No gestures
 * are added. [modifier], [shape] and [contentPadding] configure the glass parent only.
 */
@Composable
fun LiquidGlassLinearWavyProgressIndicator(
    progress: (() -> Float)? = null,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(12.dp),
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        if (progress == null) {
            LinearWavyProgressIndicator(color = color, trackColor = trackColor)
        } else {
            LinearWavyProgressIndicator(progress = progress, color = color, trackColor = trackColor)
        }
    }
}

/**
 * Native circular wavy indicator, retaining its own indicator/track paint and semantics.
 * Null [progress] selects the native indeterminate animation; no progress state is simulated.
 * [modifier], [shape] and [contentPadding] configure the glass parent only.
 */
@Composable
fun LiquidGlassCircularWavyProgressIndicator(
    progress: (() -> Float)? = null,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(12.dp),
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        if (progress == null) {
            CircularWavyProgressIndicator(color = color, trackColor = trackColor)
        } else {
            CircularWavyProgressIndicator(progress = progress, color = color, trackColor = trackColor)
        }
    }
}

/**
 * Native multi-browse carousel with caller-owned [state] and native item scope (including masking
 * modifiers). The carousel has no container paint; items retain whatever paint the caller supplies.
 * Give [carouselModifier] a bounded height appropriate to the items. [modifier], [shape] and
 * [contentPadding] configure the glass parent; [carouselContentPadding] configures the viewport.
 */
@Composable
fun LiquidGlassHorizontalMultiBrowseCarousel(
    state: CarouselState,
    preferredItemWidth: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    carouselModifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(state),
    userScrollEnabled: Boolean = true,
    carouselContentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        HorizontalMultiBrowseCarousel(
            state = state,
            preferredItemWidth = preferredItemWidth,
            modifier = carouselModifier,
            itemSpacing = itemSpacing,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = carouselContentPadding,
            content = content,
        )
    }
}

/**
 * Native uncontained carousel with caller-owned [state], scrolling and scoped item masking.
 * "Uncontained" describes native item layout; the outer glass [shape] still clips its viewport.
 * Items retain their own paint. Give [carouselModifier] a bounded height appropriate to the items.
 * [contentPadding] pads the glass parent, while [carouselContentPadding] pads the native viewport.
 */
@Composable
fun LiquidGlassHorizontalUncontainedCarousel(
    state: CarouselState,
    itemWidth: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    carouselModifier: Modifier = Modifier,
    itemSpacing: Dp = 0.dp,
    flingBehavior: TargetedFlingBehavior = CarouselDefaults.noSnapFlingBehavior(),
    userScrollEnabled: Boolean = true,
    carouselContentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable CarouselItemScope.(itemIndex: Int) -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        HorizontalUncontainedCarousel(
            state = state,
            itemWidth = itemWidth,
            modifier = carouselModifier,
            itemSpacing = itemSpacing,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            contentPadding = carouselContentPadding,
            content = content,
        )
    }
}

/**
 * Native expanding vertical toolbar with a transparent container and no native shadow over glass.
 * Caller owns [expanded] and child interactions; native accessibility expansion behavior remains.
 * Slots retain their own paint. The stationary glass parent does not follow a native scroll offset,
 * so this adapter intentionally does not accept scrollBehavior. [modifier], [shape] and
 * [contentPadding] configure the glass parent; [toolbarContentPadding] pads the native toolbar.
 */
@Composable
fun LiquidGlassVerticalFloatingToolbar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingToolbarDefaults.ContainerShape,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    toolbarContentPadding: PaddingValues = FloatingToolbarDefaults.ContentPadding,
    leadingContent: @Composable (ColumnScope.() -> Unit)? = null,
    trailingContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        VerticalFloatingToolbar(
            expanded = expanded,
            colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = Color.Transparent,
                toolbarContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = shape,
            contentPadding = toolbarContentPadding,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            expandedShadowElevation = 0.dp,
            collapsedShadowElevation = 0.dp,
            content = content,
        )
    }
}

/**
 * Native ToggleButton with caller-owned checked state, native semantics, ripple and shape animation.
 * All native container states are transparent, with no native elevation. Native foreground paint
 * remains; checked content uses primary and unchecked content uses onSurface. Provide an icon or
 * label that also communicates selection without relying on color alone. [shape] clips the glass
 * parent, not the native animated button shape; [contentPadding] pads that parent only.
 */
@Composable
fun LiquidGlassToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        ToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = ToggleButtonDefaults.toggleButtonColors(
                containerColor = Color.Transparent,
                checkedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                checkedContentColor = MaterialTheme.colorScheme.primary,
            ),
            elevation = null,
            interactionSource = interactionSource,
            content = {
                if (icon != null) {
                    icon()
                    Spacer(Modifier.width(8.dp))
                }
                content()
            },
        )
    }
}
