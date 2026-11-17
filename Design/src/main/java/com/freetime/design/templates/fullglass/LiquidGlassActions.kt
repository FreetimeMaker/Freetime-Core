package com.freetime.design.templates.fullglass

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Native Material click, focus, ripple and disabled behavior over a glass-filled container. */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        Button(onClick, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface, disabledContainerColor = Color.Transparent),
            elevation = null, content = content)
    }
}

@Composable
fun LiquidGlassElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, ButtonDefaults.elevatedShape, PaddingValues(0.dp)) {
        ElevatedButton(onClick, enabled = enabled,
            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), elevation = null, content = content)
    }
}

@Composable
fun LiquidGlassFilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, ButtonDefaults.filledTonalShape, PaddingValues(0.dp)) {
        FilledTonalButton(onClick, enabled = enabled,
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), elevation = null, content = content)
    }
}

@Composable
fun LiquidGlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, ButtonDefaults.outlinedShape, PaddingValues(0.dp)) {
        OutlinedButton(onClick, enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent), content = content)
    }
}

@Composable
fun LiquidGlassTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, ButtonDefaults.textShape, PaddingValues(0.dp)) {
        TextButton(onClick, enabled = enabled, content = content)
    }
}

/** Name the icon with its contentDescription; only the native icon button is interactive. */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        IconButton(onClick = onClick, enabled = enabled, content = content)
    }
}

@Composable
fun LiquidGlassIconToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, CircleShape, PaddingValues(0.dp)) {
        IconToggleButton(checked, onCheckedChange, enabled = enabled, content = content)
    }
}

@Composable
fun LiquidGlassFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, RoundedCornerShape(16.dp), PaddingValues(0.dp)) {
        FloatingActionButton(onClick, containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp,
                pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp),
            content = content)
    }
}

@Composable
fun LiquidGlassSmallFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, RoundedCornerShape(12.dp), PaddingValues(0.dp)) {
        SmallFloatingActionButton(onClick, containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp,
                pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp),
            content = content)
    }
}

@Composable
fun LiquidGlassLargeFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    LiquidGlassSurface(modifier, RoundedCornerShape(28.dp), PaddingValues(0.dp)) {
        LargeFloatingActionButton(onClick, containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp,
                pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp),
            content = content)
    }
}

@Composable
fun LiquidGlassExtendedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, RoundedCornerShape(16.dp), PaddingValues(0.dp)) {
        ExtendedFloatingActionButton(onClick, containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp,
                pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp),
            content = content)
    }
}

/** Optional click uses the native Card overload; no second overlapping click target is installed. */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = CardDefaults.shape,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LiquidGlassSurface(modifier, shape, PaddingValues(0.dp)) {
        val colors = CardDefaults.cardColors(containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent)
        val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp,
            focusedElevation = 0.dp, hoveredElevation = 0.dp, draggedElevation = 0.dp, disabledElevation = 0.dp)
        if (onClick == null) {
            Card(shape = shape, colors = colors, elevation = elevation, border = border, content = content)
        } else {
            Card(onClick = onClick, enabled = enabled, shape = shape, colors = colors,
                elevation = elevation, border = border, content = content)
        }
    }
}

/** Elevated and outlined variants keep glass rather than adding an opaque Material card fill. */
@Composable
fun LiquidGlassElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) = LiquidGlassCard(modifier, onClick, enabled, content = content)

@Composable
fun LiquidGlassOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) = LiquidGlassCard(modifier, onClick, enabled,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), content = content)
