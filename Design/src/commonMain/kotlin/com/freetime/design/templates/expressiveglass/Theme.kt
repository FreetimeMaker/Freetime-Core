@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.freetime.design.templates.expressiveglass

import com.freetime.design.platformDynamicColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = (if (dynamicColor) platformDynamicColorScheme(darkTheme) else null)
        ?: if (darkTheme) AppDarkColorScheme else AppLightColorScheme

    MaterialExpressiveTheme(
        motionScheme = MotionScheme.expressive(),
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
