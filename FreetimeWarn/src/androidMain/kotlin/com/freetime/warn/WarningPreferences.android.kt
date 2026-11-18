package com.freetime.warn

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidWarningPreferences(context: Context) : WarningPreferences {
    private val preferences = context.applicationContext.getSharedPreferences("freetime_warn", Context.MODE_PRIVATE)
    override fun getBoolean(key: String, default: Boolean) = preferences.getBoolean(key, default)
    override fun getLong(key: String, default: Long) = preferences.getLong(key, default)
    override fun putBoolean(key: String, value: Boolean) { check(preferences.edit().putBoolean(key, value).commit()) { "Could not persist warning acknowledgement" } }
    override fun putLong(key: String, value: Long) { check(preferences.edit().putLong(key, value).commit()) { "Could not persist warning acknowledgement" } }
    override fun remove(key: String) { check(preferences.edit().remove(key).commit()) { "Could not reset warning acknowledgement" } }
}

@Composable
actual fun rememberWarningPreferences(): WarningPreferences {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidWarningPreferences(context) }
}

@Composable
fun rememberFreetimeWarnState(context: Context, appName: String, versionCode: Long, warningId: String = "android-distribution-notice", frequency: FreetimeWarnFrequency = FreetimeWarnFrequency.ONCE_PER_VERSION): FreetimeWarnState {
    val preferences = remember(context.applicationContext) { AndroidWarningPreferences(context.applicationContext) }
    return rememberFreetimeWarnState(preferences, appName, versionCode, warningId, frequency)
}
