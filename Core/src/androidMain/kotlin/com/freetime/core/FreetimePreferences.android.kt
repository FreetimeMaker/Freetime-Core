package com.freetime.core

import android.content.Context
import android.content.SharedPreferences

actual class FreetimePreferences private constructor(
    private val preferences: SharedPreferences,
) {
    actual fun read(): FreetimePreferencesState = FreetimePreferencesState(
        themeMode = enumValueOrDefault(preferences.getString(KEY_THEME, null), FreetimeStoredThemeMode.AUTO_TIME),
        browserMode = enumValueOrDefault(preferences.getString(KEY_BROWSER, null), FreetimeStoredBrowserMode.EXTERNAL),
        darkHour = preferences.getInt(KEY_DARK_HOUR, 19).coerceIn(0, 23),
        lightHour = preferences.getInt(KEY_LIGHT_HOUR, 7).coerceIn(0, 23),
        reduceMotion = preferences.getBoolean(KEY_REDUCE_MOTION, false),
        reduceTransparency = preferences.getBoolean(KEY_REDUCE_TRANSPARENCY, false),
        highContrast = preferences.getBoolean(KEY_HIGH_CONTRAST, false),
        floatingBottomNavigationGlassEnabled = preferences.getBoolean(KEY_FLOATING_BOTTOM_NAVIGATION_GLASS, true),
    )

    actual fun update(transform: (FreetimePreferencesState) -> FreetimePreferencesState): FreetimePreferencesState {
        val state = transform(read())
        preferences.edit()
            .putString(KEY_THEME, state.themeMode.name)
            .putString(KEY_BROWSER, state.browserMode.name)
            .putInt(KEY_DARK_HOUR, state.darkHour.coerceIn(0, 23))
            .putInt(KEY_LIGHT_HOUR, state.lightHour.coerceIn(0, 23))
            .putBoolean(KEY_REDUCE_MOTION, state.reduceMotion)
            .putBoolean(KEY_REDUCE_TRANSPARENCY, state.reduceTransparency)
            .putBoolean(KEY_HIGH_CONTRAST, state.highContrast)
            .putBoolean(KEY_FLOATING_BOTTOM_NAVIGATION_GLASS, state.floatingBottomNavigationGlassEnabled)
            .apply()
        return state
    }

    actual fun reset() = preferences.edit().clear().apply()

    actual companion object {
        private const val FILE = "freetime_preferences"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_BROWSER = "browser_mode"
        private const val KEY_DARK_HOUR = "dark_hour"
        private const val KEY_LIGHT_HOUR = "light_hour"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_REDUCE_TRANSPARENCY = "reduce_transparency"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_FLOATING_BOTTOM_NAVIGATION_GLASS = "floating_bottom_navigation_glass_enabled"

        fun from(context: Context, name: String = FILE): FreetimePreferences =
            FreetimePreferences(context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE))

        private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == value } ?: default
    }
}
