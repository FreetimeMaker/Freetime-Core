package com.freetime.design

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun platformDynamicColorScheme(darkTheme: Boolean): ColorScheme? {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else null
}

internal actual fun platformCurrentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

internal actual fun platformDateLabel(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

internal actual fun platformTimeLabel(hour: Int, minute: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
