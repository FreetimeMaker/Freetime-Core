package com.freetime.warn

import java.security.MessageDigest
import java.util.prefs.Preferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class DesktopWarningPreferences : WarningPreferences {
    private val preferences = Preferences.userRoot().node("com/freetime/warn")
    // Preferences limits key length; hashing also supports arbitrary warning IDs.
    private fun storageKey(key: String): String = MessageDigest.getInstance("SHA-256")
        .digest(key.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 255) }
    override fun getBoolean(key: String, default: Boolean) = preferences.getBoolean(storageKey(key), default)
    override fun getLong(key: String, default: Long) = preferences.getLong(storageKey(key), default)
    override fun putBoolean(key: String, value: Boolean) { preferences.putBoolean(storageKey(key), value); preferences.flush() }
    override fun putLong(key: String, value: Long) { preferences.putLong(storageKey(key), value); preferences.flush() }
    override fun remove(key: String) { preferences.remove(storageKey(key)); preferences.flush() }
}

@Composable
actual fun rememberWarningPreferences(): WarningPreferences = remember { DesktopWarningPreferences() }
