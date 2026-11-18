package com.freetime.core

enum class FreetimeStoredThemeMode { SYSTEM, LIGHT, DARK, OLED, AUTO_TIME }
enum class FreetimeStoredBrowserMode { EXTERNAL, IN_APP }

data class FreetimePreferencesState(
    val themeMode: FreetimeStoredThemeMode = FreetimeStoredThemeMode.AUTO_TIME,
    val browserMode: FreetimeStoredBrowserMode = FreetimeStoredBrowserMode.EXTERNAL,
    val darkHour: Int = 19,
    val lightHour: Int = 7,
    val reduceMotion: Boolean = false,
    val reduceTransparency: Boolean = false,
    val highContrast: Boolean = false,
    val floatingBottomNavigationGlassEnabled: Boolean = true,
)

expect class FreetimePreferences {
    fun read(): FreetimePreferencesState
    fun update(transform: (FreetimePreferencesState) -> FreetimePreferencesState): FreetimePreferencesState
    fun reset()
    companion object
}
