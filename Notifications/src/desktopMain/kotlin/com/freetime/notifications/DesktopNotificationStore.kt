package com.freetime.notifications

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.json.JSONArray
import org.json.JSONObject

/** One service owns a directory. Atomic replacement prevents partially written state after a crash. */
internal class DesktopNotificationStore(private val directory: Path) : NotificationStateStore {
    private val file = directory.resolve("notifications.json")

    private fun read(): JSONObject = if (Files.exists(file)) JSONObject(Files.readString(file)) else JSONObject()

    private fun edit(change: (JSONObject) -> Unit) {
        val state = read().apply(change)
        Files.createDirectories(directory)
        val temporary = Files.createTempFile(directory, "notifications-", ".tmp")
        try {
            Files.writeString(temporary, state.toString())
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }

    override suspend fun configuration(): NotificationConfiguration? = read().let {
        if (it.has("configuration")) NotificationConfigurationCodec.decode(it.getString("configuration")) else null
    }
    override suspend fun saveConfiguration(config: NotificationConfiguration) = edit {
        it.put("configuration", NotificationConfigurationCodec.encode(config))
    }
    override suspend fun clearConfiguration() = edit { it.remove("configuration") }
    override suspend fun isCategoryEnabled(category: NotificationCategory, config: NotificationConfiguration): Boolean =
        read().let { if (it.has("category_${category.name}")) it.getBoolean("category_${category.name}") else category in config.enabledCategories }
    override suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) = edit {
        it.put("category_${category.name}", enabled)
    }
    override suspend fun seenIds(sourceId: String): Set<String>? {
        val state = read()
        val key = "seen_$sourceId"
        if (!state.has(key)) return null
        val ids = state.getJSONArray(key)
        return (0 until ids.length()).map { ids.getString(it) }.toSet()
    }
    override suspend fun setSeenIds(sourceId: String, ids: Set<String>) = edit {
        it.put("seen_$sourceId", JSONArray(ids.take(500)))
    }
}
