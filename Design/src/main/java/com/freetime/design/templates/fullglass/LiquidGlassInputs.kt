@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@file:Suppress("DEPRECATION")

package com.freetime.design.templates.fullglass

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimeInputDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Slot-label overload; the existing String-label overload is unchanged. State and IME actions are
 * caller-owned. Only container fills become transparent; Material indicators, error and disabled
 * foregrounds stay visible. [modifier] belongs to the glass, [controlModifier] to the native field.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    controlModifier: Modifier = Modifier.fillMaxWidth(),
    shape: Shape = RoundedCornerShape(28.dp),
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        TextField(
            value = value, onValueChange = onValueChange, modifier = controlModifier,
            enabled = enabled, readOnly = readOnly, textStyle = textStyle, label = label,
            placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            prefix = prefix, suffix = suffix, supportingText = supportingText, isError = isError,
            visualTransformation = visualTransformation, keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions, singleLine = singleLine, maxLines = maxLines,
            minLines = minLines, interactionSource = interactionSource, shape = shape,
            colors = transparentInputContainers(TextFieldDefaults.colors()),
        )
    }
}

/** Native outlined field: its outline, including focus/error paint and label cutout, is retained. */
@Composable
fun LiquidGlassOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    controlModifier: Modifier = Modifier.fillMaxWidth(),
    shape: Shape = RoundedCornerShape(28.dp),
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange, modifier = controlModifier,
            enabled = enabled, readOnly = readOnly, textStyle = textStyle, label = label,
            placeholder = placeholder, leadingIcon = leadingIcon, trailingIcon = trailingIcon,
            prefix = prefix, suffix = suffix, supportingText = supportingText, isError = isError,
            visualTransformation = visualTransformation, keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions, singleLine = singleLine, maxLines = maxLines,
            minLines = minLines, interactionSource = interactionSource, shape = shape,
            colors = transparentInputContainers(OutlinedTextFieldDefaults.colors()),
        )
    }
}

/**
 * Native in-composition expanding SearchBar, not a text-field imitation or a dialog. The caller
 * owns [state] and [expanded], handles [onExpandedChange], and decides whether [onSearch] collapses
 * results. Material owns focus, IME Search, back handling and expansion animation. Leave room in
 * the parent for expansion; glass cannot escape parent constraints or sample another window.
 *
 * Uses the compatibility expanded API deliberately: it keeps results in this window. Its native
 * state-based InputField supports [readOnly] but has no isError or custom keyboard-actions/options
 * parameters. Use the general text-field wrappers when those controls are needed.
 */
@Composable
fun LiquidGlassSearchBar(
    state: TextFieldState,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    controlModifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = if (expanded) RectangleShape else SearchBarDefaults.inputFieldShape
    val inputColors = transparentInputContainers(SearchBarDefaults.inputFieldColors())
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    state = state, onSearch = onSearch, expanded = expanded,
                    onExpandedChange = onExpandedChange, modifier = inputModifier,
                    enabled = enabled, readOnly = readOnly, placeholder = placeholder,
                    leadingIcon = leadingIcon, trailingIcon = trailingIcon,
                    interactionSource = interactionSource, colors = inputColors,
                )
            },
            expanded = expanded, onExpandedChange = onExpandedChange, modifier = controlModifier,
            shape = shape,
            colors = SearchBarDefaults.colors(containerColor = Color.Transparent,
                inputFieldColors = inputColors),
            tonalElevation = 0.dp, shadowElevation = 0.dp, windowInsets = windowInsets,
            content = content,
        )
    }
}

/**
 * Native docked results panel in the same composition. State/expansion are caller-owned, and
 * [onSearch] does not implicitly collapse it. The same native InputField API limits as
 * [LiquidGlassSearchBar] apply. Parent constraints must accommodate the expanded results panel.
 */
@Composable
fun LiquidGlassDockedSearchBar(
    state: TextFieldState,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    controlModifier: Modifier = Modifier,
    inputModifier: Modifier = Modifier,
    shape: Shape = SearchBarDefaults.dockedShape,
    content: @Composable ColumnScope.() -> Unit,
) {
    val inputColors = transparentInputContainers(SearchBarDefaults.inputFieldColors())
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        DockedSearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    state = state, onSearch = onSearch, expanded = expanded,
                    onExpandedChange = onExpandedChange, modifier = inputModifier,
                    enabled = enabled, readOnly = readOnly, placeholder = placeholder,
                    leadingIcon = leadingIcon, trailingIcon = trailingIcon,
                    interactionSource = interactionSource, colors = inputColors,
                )
            },
            expanded = expanded, onExpandedChange = onExpandedChange, modifier = controlModifier,
            shape = shape,
            colors = SearchBarDefaults.colors(containerColor = Color.Transparent,
                inputFieldColors = inputColors),
            tonalElevation = 0.dp, shadowElevation = 0.dp, content = content,
        )
    }
}

/** Native non-interactive ListItem slots and typography; add interaction on [controlModifier]. */
@Composable
fun LiquidGlassListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        ListItem(
            headlineContent = headlineContent, modifier = controlModifier,
            overlineContent = overlineContent, supportingContent = supportingContent,
            leadingContent = leadingContent, trailingContent = trailingContent,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            tonalElevation = 0.dp, shadowElevation = 0.dp,
        )
    }
}

/** Native dot/count geometry. Callers supply accessible badge text/description as appropriate. */
@Composable
fun LiquidGlassBadge(
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    controlModifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit)? = null,
) {
    LiquidGlassSurface(modifier, RoundedCornerShape(50), PaddingValues(0.dp)) {
        Badge(modifier = controlModifier, containerColor = Color.Transparent,
            contentColor = contentColor, content = content)
    }
}

/** No enclosing clipped surface: native badge placement may extend beyond the anchor's bounds. */
@Composable
fun LiquidGlassBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    BadgedBox(badge = badge, modifier = modifier, content = content)
}

/** Zero-padding rectangular glass strip with a real visible native divider; default height is 1dp. */
@Composable
fun LiquidGlassHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) {
    LiquidGlassSurface(modifier, RectangleShape, PaddingValues(0.dp)) {
        HorizontalDivider(thickness = thickness, color = color)
    }
}

/** Default width is 1dp. Constrain height in [modifier] (or use a parent with intrinsic height). */
@Composable
fun LiquidGlassVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) {
    LiquidGlassSurface(modifier, RectangleShape, PaddingValues(0.dp)) {
        VerticalDivider(thickness = thickness, color = color)
    }
}

/**
 * Native time editor with caller-owned [state]. Material3 1.5 uses TimeInputDefaults, not the hidden
 * TimePickerColors overload. Selected AM/PM fill and all foregrounds remain Material; only the
 * unselected period fill and field containers are transparent. Native TimeInput exposes no enabled,
 * readOnly, isError or keyboard configuration parameters; this wrapper does not simulate them.
 */
@Composable
fun LiquidGlassTimeInput(
    state: TimePickerState,
    modifier: Modifier = Modifier,
    controlModifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val defaults = TimeInputDefaults.colors()
    LiquidGlassSurface(modifier, shape, contentPadding) {
        TimeInput(
            state = state, modifier = controlModifier,
            colors = TimeInputDefaults.colors(
                containerColor = Color.Transparent,
                periodSelectorContainerColor = Color.Transparent,
                timeTextFieldColors = transparentInputContainers(defaults.timeTextFieldColors),
            ),
        )
    }
}

private fun transparentInputContainers(colors: TextFieldColors): TextFieldColors = colors.copy(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
)
