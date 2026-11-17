@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.MultiChoiceSegmentedButtonRowScope
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SingleChoiceSegmentedButtonRowScope
import androidx.compose.material3.Slider
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material checkbox in a glass container; the checkbox itself retains Material paint and behavior.
 * The caller owns [checked]. Use [controlModifier] for an accessible name and [modifier] for the
 * container. A null callback makes this a passive indicator, for use in a separately toggleable row.
 */
@Composable
fun LiquidGlassCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = controlModifier,
            enabled = enabled, interactionSource = interactionSource)
    }
}

/**
 * Material tri-state checkbox in glass, not a glass-painted checkmark. The caller owns [state] and
 * decides its next state in [onClick]; no state cycle is invented. Supply an accessible name through
 * [controlModifier]. A null callback supports a separately toggleable parent.
 */
@Composable
fun LiquidGlassTriStateCheckbox(
    state: ToggleableState,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        TriStateCheckbox(state = state, onClick = onClick, modifier = controlModifier,
            enabled = enabled, interactionSource = interactionSource)
    }
}

/**
 * Material radio indicator in a glass container. The caller owns [selected] and should put related
 * options in a selectable group. Name the control using [controlModifier]; null [onClick] delegates
 * interaction to a separately selectable parent. The radio's selection paint remains visible.
 */
@Composable
fun LiquidGlassRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        RadioButton(selected = selected, onClick = onClick, modifier = controlModifier,
            enabled = enabled, interactionSource = interactionSource)
    }
}

/**
 * Material switch over glass. Its actual thumb and track keep Material's contrasting selected and
 * disabled colors; only the surrounding container is glass. The caller owns [checked], supplies an
 * accessible name via [controlModifier], and may use a null callback with a toggleable parent.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp),
    interactionSource: MutableInteractionSource? = null,
    thumbContent: (@Composable () -> Unit)? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = controlModifier,
            enabled = enabled, interactionSource = interactionSource, thumbContent = thumbContent)
    }
}

/**
 * State-hoisted Material slider in a glass container, with the real Material thumb, active track,
 * inactive track and ticks intact (not a refractive track). [valueRange] must cover caller values;
 * [steps] is the number of interior discrete values, or zero for continuous input. Name the slider
 * through [controlModifier]. [onValueChangeFinished] does not itself persist the value.
 */
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        Slider(value = value, onValueChange = onValueChange, modifier = controlModifier,
            enabled = enabled, valueRange = valueRange, steps = steps,
            onValueChangeFinished = onValueChangeFinished,
                        interactionSource = interactionSource ?: remember { MutableInteractionSource() })
    }
}

/**
 * Two-thumb Material range slider in glass; its actual tracks and thumbs retain Material paint and
 * range semantics. The caller owns [value], must provide a covering [valueRange], and handles any
 * persistence in its callbacks. [steps] counts interior stops; zero means continuous input.
 * Use [controlModifier] for control sizing and accessible context.
 */
@Composable
fun LiquidGlassRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp),
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        RangeSlider(value = value, onValueChange = onValueChange, modifier = controlModifier,
            enabled = enabled, valueRange = valueRange, steps = steps,
            onValueChangeFinished = onValueChangeFinished)
    }
}

/**
 * Material assist chip with a transparent container over glass. Material owns click, focus and
 * disabled semantics; [label] supplies its accessible text. The surrounding padding is not clickable.
 */
@Composable
fun LiquidGlassAssistChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        AssistChip(onClick = onClick, label = label, modifier = controlModifier, enabled = enabled,
            leadingIcon = leadingIcon, trailingIcon = trailingIcon, shape = shape,
            colors = AssistChipDefaults.assistChipColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), interactionSource = interactionSource)
    }
}

/**
 * State-hoisted Material filter chip over glass. Only the unselected container is transparent;
 * Material's selected fill remains visible. [onClick] requests a change, never mutates [selected].
 * Give [label] meaningful text and use [leadingIcon] for an optional selection icon.
 */
@Composable
fun LiquidGlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        FilterChip(selected = selected, onClick = onClick, label = label, modifier = controlModifier,
            enabled = enabled, leadingIcon = leadingIcon, trailingIcon = trailingIcon, shape = shape,
            colors = FilterChipDefaults.filterChipColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), interactionSource = interactionSource)
    }
}

/**
 * Material input chip over glass with caller-owned selection. Unselected fill is transparent and
 * selected fill stays Material. [trailingIcon] is a visual slot, NOT an automatic remove action;
 * removal must be implemented and accessibly labeled by the caller. [label] names the chip.
 */
@Composable
fun LiquidGlassInputChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    avatar: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        InputChip(selected = selected, onClick = onClick, label = label, modifier = controlModifier,
            enabled = enabled, leadingIcon = leadingIcon, avatar = avatar, trailingIcon = trailingIcon,
            shape = shape, colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), interactionSource = interactionSource)
    }
}

/**
 * Material suggestion chip with transparent fill over glass. [label] names the real Material click
 * target; glass padding is decoration, not an additional target. No suggestion acceptance is stored.
 */
@Composable
fun LiquidGlassSuggestionChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    interactionSource: MutableInteractionSource? = null,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        SuggestionChip(onClick = onClick, label = label, modifier = controlModifier, enabled = enabled,
            icon = icon, shape = shape,
            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), interactionSource = interactionSource)
    }
}

/**
 * Glass container around Material's single-choice segmented row, including its selectable-group
 * semantics. Supply Material SegmentedButton children in [content], hoisting their selection and
 * enabled state. This does not enforce exclusivity or repaint child fills; Material defaults keep
 * inactive fills transparent and active indicators visible. Match child border widths to [space].
 */
@Composable
fun LiquidGlassSingleChoiceSegmentedButtonRow(
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    space: Dp = SegmentedButtonDefaults.BorderWidth,
    content: @Composable SingleChoiceSegmentedButtonRowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        SingleChoiceSegmentedButtonRow(modifier = controlModifier, space = space, content = content)
    }
}

/**
 * Glass container around Material's multi-choice segmented row. Supply Material SegmentedButton
 * children in [content]; the caller owns every child's checked/enabled state and callbacks. Child
 * paint is unchanged, retaining visible selection. Match child border widths to [space].
 */
@Composable
fun LiquidGlassMultiChoiceSegmentedButtonRow(
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    space: Dp = SegmentedButtonDefaults.BorderWidth,
    content: @Composable MultiChoiceSegmentedButtonRowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        MultiChoiceSegmentedButtonRow(modifier = controlModifier, space = space, content = content)
    }
}

/**
 * Material linear progress in a glass container, not a glass-painted track. Null [progress] uses
 * Material's indeterminate animation; otherwise the caller supplies a fraction provider (0..1).
 * Both the active indicator and inactive track retain Material colors and progress semantics.
 * [controlModifier] sizes/names the indicator; [modifier] sizes the outer glass container.
 */
@Composable
fun LiquidGlassLinearProgressIndicator(
    progress: (() -> Float)? = null,
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(12.dp),
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        if (progress == null) {
            LinearProgressIndicator(modifier = controlModifier)
        } else {
            LinearProgressIndicator(progress = progress, modifier = controlModifier)
        }
    }
}

/**
 * Material circular progress in a glass container; the arc and track use Material paint, not glass.
 * Null [progress] means indeterminate; a non-null provider supplies caller-owned progress (0..1).
 * Material supplies progress semantics. Use [controlModifier] for its accessible name and size.
 */
@Composable
fun LiquidGlassCircularProgressIndicator(
    progress: (() -> Float)? = null,
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(12.dp),
) {
    LiquidGlassSurface(modifier, shape, contentPadding) {
        if (progress == null) {
            CircularProgressIndicator(modifier = controlModifier)
        } else {
            CircularProgressIndicator(progress = progress, modifier = controlModifier)
        }
    }
}
