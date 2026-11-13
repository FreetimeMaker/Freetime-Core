package com.freetime.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.MessageDigest
import kotlinx.coroutines.flow.first

private val Context.notificationDataStore by preferencesDataStore(name = "freetime_notifications")

internal class NotificationStore(context: Context) {
    private val app = context.applicationContext
    private val dataStore = app.notificationDataStore

    suspend fun configuration(): NotificationConfiguration? {
        val preferences = dataStore.data.first()
        val encoded = preferences[configurationKey] ?: return null
        val config = NotificationConfigurationCodec.decode(encoded)
        val resourceName = preferences[iconResourceNameKey]
        require(!resourceName.isNullOrBlank()) {
            "Missing notification icon resource name; reinitialize notifications"
        }
        return config.copy(smallIcon = resolveIcon(resourceName))
    }

    suspend fun saveConfiguration(config: NotificationConfiguration) {
        val resourceName = try {
            requireIconType(config.smallIcon)
            app.resources.getResourceName(config.smallIcon)
        } catch (error: Resources.NotFoundException) {
            throw IllegalArgumentException("Notification icon resource does not exist", error)
        }
        val encoded = NotificationConfigurationCodec.encode(config)
        dataStore.edit {
            it[configurationKey] = encoded
            it[iconResourceNameKey] = resourceName
        }
    }

    suspend fun clearConfiguration() {
        dataStore.edit {
            it.remove(configurationKey)
            it.remove(iconResourceNameKey)
        }
    }

    // Resolve the persisted full resource name because numeric IDs can change after an app update.
    @SuppressLint("DiscouragedApi")
    private fun resolveIcon(resourceName: String): Int {
        val icon = app.resources.getIdentifier(resourceName, null, null)
        require(icon != 0) {
            "Notification icon resource cannot be resolved; reinitialize notifications"
        }
        try {
            requireIconType(icon)
        } catch (error: Resources.NotFoundException) {
            throw IllegalArgumentException("Notification icon resource does not exist", error)
        }
        return icon
    }

    private fun requireIconType(icon: Int) {
        val type = app.resources.getResourceTypeName(icon)
        require(type == "drawable" || type == "mipmap") {
            "Notification icon must be a drawable or mipmap resource"
        }
    }

    suspend fun isCategoryEnabled(
        category: NotificationCategory,
        config: NotificationConfiguration,
    ): Boolean = dataStore.data.first()[categoryKey(category)] ?: (category in config.enabledCategories)

    suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        dataStore.edit { it[categoryKey(category)] = enabled }
    }

    /** Null means never polled; a stored empty set is a successful empty baseline. */
    suspend fun seenIds(sourceId: String): Set<String>? =
        dataStore.data.first()[seenKey(sourceId)]?.toSet()

    suspend fun setSeenIds(sourceId: String, ids: Set<String>) {
        val snapshot = ids.take(500).toSet()
        dataStore.edit { it[seenKey(sourceId)] = snapshot }
    }

    private fun categoryKey(category: NotificationCategory) =
        booleanPreferencesKey("category_${category.name}")

    private fun seenKey(sourceId: String) = stringSetPreferencesKey(
        "seen_" + MessageDigest.getInstance("SHA-256")
            .digest(sourceId.toByteArray(Charsets.UTF_8))
            .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') },
    )

    private companion object {
        val configurationKey = stringPreferencesKey("configuration")
        val iconResourceNameKey = stringPreferencesKey("icon_resource_name")
    }
}
