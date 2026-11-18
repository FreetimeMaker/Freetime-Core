package com.freetime.design

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? = null

internal actual fun platformCurrentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

internal actual fun platformDateLabel(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

internal actual fun platformTimeLabel(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
