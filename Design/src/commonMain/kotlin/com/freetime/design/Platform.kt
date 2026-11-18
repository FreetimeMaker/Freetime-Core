package com.freetime.design

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/** Returns null when system wallpaper colors are unavailable; callers retain their own palette. */
@Composable
internal expect fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme?

internal expect fun platformCurrentHour(): Int
internal expect fun platformDateLabel(millis: Long): String
internal expect fun platformTimeLabel(hour: Int, minute: Int): String
