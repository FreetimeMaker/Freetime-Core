package com.freetime.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FreetimeCoreAndroidTest {
    @Test
    fun instrumentationUsesConfiguredTestPackage() {
        val context = InstrumentationRegistry.getInstrumentation().context
        assertEquals("com.freetime.core.test", context.packageName)
    }

    @Test
    fun preferencesPersistAcrossInstancesAndResetToDefaults() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val name = "freetime_core_preferences_test"
        val preferences = FreetimePreferences.from(context, name)
        preferences.reset()
        try {
            val expected = FreetimePreferencesState(
                themeMode = FreetimeStoredThemeMode.DARK,
                browserMode = FreetimeStoredBrowserMode.IN_APP,
                darkHour = 20,
                lightHour = 8,
                reduceMotion = true,
                reduceTransparency = true,
                highContrast = true,
                floatingBottomNavigationGlassEnabled = false,
            )
            preferences.update { expected }
            assertEquals(expected, FreetimePreferences.from(context, name).read())

            preferences.reset()
            assertEquals(FreetimePreferencesState(), FreetimePreferences.from(context, name).read())
        } finally {
            preferences.reset()
        }
    }
}
