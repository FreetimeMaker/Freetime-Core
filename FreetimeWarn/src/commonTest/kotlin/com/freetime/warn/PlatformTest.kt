package com.freetime.warn
import kotlin.test.*
class WarningTest {
    private class MemoryPreferences : WarningPreferences {
        val data = mutableMapOf<String, Any>()
        override fun getBoolean(key: String, default: Boolean) = data[key] as? Boolean ?: default
        override fun getLong(key: String, default: Long) = data[key] as? Long ?: default
        override fun putBoolean(key: String, value: Boolean) { data[key] = value }
        override fun putLong(key: String, value: Long) { data[key] = value }
        override fun remove(key: String) { data.remove(key) }
    }
    @Test fun frequencyAndReset() {
        val store = MemoryPreferences()
        fun state(version: Long = 1, frequency: FreetimeWarnFrequency = FreetimeWarnFrequency.ONCE_PER_VERSION) =
            FreetimeWarnState(store, "App", version, "notice", frequency)
        val first = state()
        assertTrue(first.visible)
        first.dismiss()
        assertTrue(state().visible)
        first.acknowledge()
        assertFalse(state().visible)
        assertTrue(state(2).visible)
        first.reset()
        assertTrue(state().visible)
        state(frequency = FreetimeWarnFrequency.ONCE).acknowledge()
        assertFalse(state(2, FreetimeWarnFrequency.ONCE).visible)
        assertTrue(state(frequency = FreetimeWarnFrequency.ALWAYS).visible)
    }
    @Test fun failedWriteKeepsWarningVisible() {
        val broken = object : WarningPreferences {
            override fun getBoolean(key: String, default: Boolean) = default
            override fun getLong(key: String, default: Long) = default
            override fun putBoolean(key: String, value: Boolean): Unit = error("storage failure")
            override fun putLong(key: String, value: Long): Unit = error("storage failure")
            override fun remove(key: String): Unit = error("storage failure")
        }
        val state = FreetimeWarnState(broken, "App", 1, "notice", FreetimeWarnFrequency.ONCE)
        assertFails { state.acknowledge() }
        assertTrue(state.visible)
    }
}
