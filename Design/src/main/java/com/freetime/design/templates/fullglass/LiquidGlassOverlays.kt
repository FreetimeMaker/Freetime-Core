@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * Material dialog behavior with caller-owned visibility and actions.
 * Separate windows cannot sample the page's window-local LayerBackdrop: overlay surfaces use an
 * opaque Material fallback, not cross-window blur. This applies to sheets, menus and tooltips too.
 * The caller must remove the dialog on dismissal and handle its action buttons explicitly.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidGlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(28.dp),
    properties: DialogProperties = DialogProperties(),
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest, modifier = modifier, properties = properties) {
        LiquidGlassOverlaySurface(shape = shape, contentPadding = PaddingValues(24.dp)) {
            Column(
                modifier = Modifier.heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.8f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    icon?.invoke()
                    if (title != null) {
                        ProvideTextStyle(MaterialTheme.typography.headlineSmall, title)
                    }
                    if (text != null) {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium, text)
                        }
                    }
                }
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

/**
 * Real modal sheet, retaining Material's scrim, insets, gestures and back handling.
 * [sheetState] is owned by the caller. For a content-driven close, hide the state before removing
 * this composable. The default Material drag handle remains in its native slot for accessibility;
 * only the content panel has an opaque overlay fallback (the surrounding sheet is transparent).
 */
@Composable
fun LiquidGlassModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    content: @Composable ColumnScope.() -> Unit,
) {
    // Also isolate the native handle and any content it may compose in the sheet window.
    CompositionLocalProvider(LocalPageBackdrop provides null) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
            modifier = modifier,
            shape = shape,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            properties = properties,
        ) {
            LiquidGlassOverlaySurface(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                contentPadding = contentPadding,
            ) {
                Column(content = content)
            }
        }
    }
}

/**
 * Anchor this beside its trigger in a Box, just like [DropdownMenu]. Material owns popup positioning,
 * keyboard navigation and scrolling; do not put a LazyColumn inside its scrollable content.
 * The popup uses an opaque fallback rather than sampling the anchor window's backdrop.
 */
@Composable
fun LiquidGlassDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = MenuDefaults.DefaultMenuProperties,
    shape: Shape = MenuDefaults.shape,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalPageBackdrop provides null) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            offset = offset,
            scrollState = scrollState,
            properties = properties,
            shape = shape,
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            LiquidGlassOverlaySurface(shape = shape, contentPadding = PaddingValues(0.dp)) {
                Column(content = content)
            }
        }
    }
}

/** Native menu item semantics, disabled styling, focus and ripple; clicking does not auto-dismiss. */
@Composable
fun LiquidGlassDropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    DropdownMenuItem(
        text = text,
        onClick = onClick,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        enabled = enabled,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}

/**
 * Intended for SnackbarHost's snackbar slot: the host owns timing and announcement semantics.
 * Uses inline glass and delegates action/dismiss callbacks to [snackbarData] through Material.
 */
@Composable
fun LiquidGlassSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = RoundedCornerShape(16.dp),
) {
    LiquidGlassSurface(modifier = modifier, shape = shape, contentPadding = PaddingValues(0.dp)) {
        Snackbar(
            snackbarData = snackbarData,
            actionOnNewLine = actionOnNewLine,
            shape = shape,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            actionColor = MaterialTheme.colorScheme.primary,
            actionContentColor = MaterialTheme.colorScheme.primary,
            dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Native long-press/hover tooltip behavior with caller-owned state and an opaque popup fallback. */
@Composable
fun LiquidGlassTooltip(
    state: TooltipState,
    tooltip: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberTooltipPositionProvider(
        TooltipAnchorPosition.Above,
    ),
    focusable: Boolean = false,
    enableUserInput: Boolean = true,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        state = state,
        modifier = modifier,
        positionProvider = positionProvider,
        focusable = focusable,
        enableUserInput = enableUserInput,
        tooltip = {
            LiquidGlassOverlaySurface(
                shape = TooltipDefaults.plainTooltipContainerShape,
                contentPadding = PaddingValues(0.dp),
            ) {
                PlainTooltip(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    content = tooltip,
                )
            }
        },
        content = content,
    )
}

/** Inline Material picker: state, date constraints and display mode remain caller-owned. */
@Composable
fun LiquidGlassDatePicker(
    state: DatePickerState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    colors: DatePickerColors = DatePickerDefaults.colors(),
    showModeToggle: Boolean = true,
) {
    LiquidGlassSurface(modifier = modifier, shape = shape, contentPadding = PaddingValues(0.dp)) {
        DatePicker(
            state = state,
            colors = colors.copy(containerColor = Color.Transparent),
            showModeToggle = showModeToggle,
        )
    }
}

/**
 * Inline Material range picker. Supply a bounded height via [modifier] for its scrolling calendar.
 * When hosted in LiquidGlassAlertDialog, inherits the safe opaque overlay fallback instead of blur.
 */
@Composable
fun LiquidGlassDateRangePicker(
    state: DateRangePickerState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    colors: DatePickerColors = DatePickerDefaults.colors(),
    showModeToggle: Boolean = true,
) {
    LiquidGlassSurface(modifier = modifier, shape = shape, contentPadding = PaddingValues(0.dp)) {
        DateRangePicker(
            state = state,
            colors = colors.copy(containerColor = Color.Transparent),
            showModeToggle = showModeToggle,
        )
    }
}

/**
 * Inline clock picker with native dial, hour/minute and AM/PM controls, not a decorative replica.
 * Only its outer container is transparent; Material selection/contrast colors remain intact.
 */
@Composable
fun LiquidGlassTimePicker(
    state: TimePickerState,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    colors: TimePickerColors = TimePickerDefaults.colors(),
    layoutType: TimePickerLayoutType = TimePickerDefaults.layoutType(),
) {
    LiquidGlassSurface(modifier = modifier, shape = shape, contentPadding = contentPadding) {
        TimePicker(
            state = state,
            colors = colors.copy(containerColor = Color.Transparent),
            layoutType = layoutType,
        )
    }
}
