package com.freetime.design.templates.fullglass

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Native icon variants with glass replacing container paint; name the icon in [content]. */
@Composable
fun LiquidGlassFilledIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        FilledIconButton(onClick, enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface, disabledContainerColor = Color.Transparent),
            content = content)
    }
}

@Composable
fun LiquidGlassFilledTonalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        FilledTonalIconButton(onClick, enabled = enabled,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface, disabledContainerColor = Color.Transparent),
            content = content)
    }
}

@Composable
fun LiquidGlassOutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        OutlinedIconButton(onClick, enabled = enabled, content = content)
    }
}

/** Checked content keeps a distinct native role color and toggle semantics, not an opaque fill. */
@Composable
fun LiquidGlassFilledIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        FilledIconToggleButton(checked, onCheckedChange, enabled = enabled,
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = Color.Transparent, checkedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface,
                checkedContentColor = MaterialTheme.colorScheme.primary), content = content)
    }
}

@Composable
fun LiquidGlassFilledTonalIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        FilledTonalIconToggleButton(checked, onCheckedChange, enabled = enabled,
            colors = IconButtonDefaults.filledTonalIconToggleButtonColors(
                containerColor = Color.Transparent, checkedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface,
                checkedContentColor = MaterialTheme.colorScheme.primary), content = content)
    }
}

@Composable
fun LiquidGlassOutlinedIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        OutlinedIconToggleButton(checked, onCheckedChange, enabled = enabled,
            colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                containerColor = Color.Transparent, checkedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurface,
                checkedContentColor = MaterialTheme.colorScheme.primary), content = content)
    }
}
