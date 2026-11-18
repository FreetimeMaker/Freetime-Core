package com.freetime.notifications

/** Shared checking and lifecycle surface; Android's Context-based facade remains available. */
interface NotificationService {
    suspend fun initialize(configuration: NotificationConfiguration)
    suspend fun checkNow(): NotificationCheckResult
    suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean)
    suspend fun isCategoryEnabled(category: NotificationCategory): Boolean
    fun hasPermission(): Boolean
    suspend fun stop()
}

internal interface NotificationStateStore {
    suspend fun configuration(): NotificationConfiguration?
    suspend fun saveConfiguration(config: NotificationConfiguration)
    suspend fun clearConfiguration()
    suspend fun isCategoryEnabled(category: NotificationCategory, config: NotificationConfiguration): Boolean
    suspend fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean)
    suspend fun seenIds(sourceId: String): Set<String>?
    suspend fun setSeenIds(sourceId: String, ids: Set<String>)
}

internal interface NotificationDelivery {
    fun hasPermission(): Boolean
    fun ensureChannels(configuration: NotificationConfiguration)
    fun post(configuration: NotificationConfiguration, category: NotificationCategory, sourceId: String, entry: NotificationEntry): Boolean
    fun canPost(category: NotificationCategory): Boolean
    fun cancelCategory(category: NotificationCategory)
}
