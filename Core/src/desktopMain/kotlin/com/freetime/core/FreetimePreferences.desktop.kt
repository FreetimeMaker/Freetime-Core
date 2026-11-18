package com.freetime.core

import java.util.prefs.Preferences

actual class FreetimePreferences private constructor(
    private val preferences: Preferences,
) {
    actual fun read(): FreetimePreferencesState = FreetimePreferencesState(
        themeMode = enumValueOrDefault(preferences.get(KEY_THEME, null), FreetimeStoredThemeMode.AUTO_TIME),
        browserMode = enumValueOrDefault(preferences.get(KEY_BROWSER, null), FreetimeStoredBrowserMode.EXTERNAL),
        darkHour = preferences.getInt(KEY_DARK_HOUR, 19).coerceIn(0, 23),
        lightHour = preferences.getInt(KEY_LIGHT_HOUR, 7).coerceIn(0, 23),
        reduceMotion = preferences.getBoolean(KEY_REDUCE_MOTION, false),
        reduceTransparency = preferences.getBoolean(KEY_REDUCE_TRANSPARENCY, false),
        highContrast = preferences.getBoolean(KEY_HIGH_CONTRAST, false),
        floatingBottomNavigationGlassEnabled = preferences.getBoolean(KEY_FLOATING_BOTTOM_NAVIGATION_GLASS, true),
    )

    actual fun update(transform: (FreetimePreferencesState) -> FreetimePreferencesState): FreetimePreferencesState {
        val state = transform(read())
        preferences.put(KEY_THEME, state.themeMode.name)
        preferences.put(KEY_BROWSER, state.browserMode.name)
        preferences.putInt(KEY_DARK_HOUR, state.darkHour.coerceIn(0, 23))
        preferences.putInt(KEY_LIGHT_HOUR, state.lightHour.coerceIn(0, 23))
        preferences.putBoolean(KEY_REDUCE_MOTION, state.reduceMotion)
        preferences.putBoolean(KEY_REDUCE_TRANSPARENCY, state.reduceTransparency)
        preferences.putBoolean(KEY_HIGH_CONTRAST, state.highContrast)
        preferences.putBoolean(KEY_FLOATING_BOTTOM_NAVIGATION_GLASS, state.floatingBottomNavigationGlassEnabled)
        preferences.flush()
        return state
    }

    actual fun reset() { preferences.clear(); preferences.flush() }

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

        fun from(name: String = FILE): FreetimePreferences =
            FreetimePreferences(Preferences.userRoot().node("com/freetime/" + name))

        private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == value } ?: default
    }
}
