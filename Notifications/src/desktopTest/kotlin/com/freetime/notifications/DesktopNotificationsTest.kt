package com.freetime.notifications

import java.nio.file.Files
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DesktopNotificationsTest {
    @Test fun storageSurvivesReopeningAndStopPreservesHistoryAndOverrides() = runBlocking {
        val directory = Files.createTempDirectory("notification-store-test")
        try {
            val config = NotificationConfiguration(sources = listOf(DeveloperBlogSource()))
            val store = DesktopNotificationStore(directory)
            assertNull(store.configuration())
            assertNull(store.seenIds("source"))
            store.saveConfiguration(config)
            store.setSeenIds("source", emptySet())
            store.setCategoryEnabled(NotificationCategory.DEVELOPER_MESSAGES, false)
            val reopened = DesktopNotificationStore(directory)
            assertEquals(config, reopened.configuration())
            assertEquals(emptySet<String>(), reopened.seenIds("source"))
            assertFalse(reopened.isCategoryEnabled(NotificationCategory.DEVELOPER_MESSAGES, config))
            reopened.setSeenIds("source", (1..600).map { it.toString() }.toSet())
            reopened.clearConfiguration()
            assertNull(store.configuration())
            assertEquals(500, store.seenIds("source")!!.size)
            assertFalse(store.isCategoryEnabled(NotificationCategory.DEVELOPER_MESSAGES, config))
        } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) } }
        }
    }

    @Test fun engineAcknowledgesOnlySuccessfulDeliveryAndPersistsBaseline() = runBlocking {
        val directory = Files.createTempDirectory("notification-engine-test")
        try {
            val store = DesktopNotificationStore(directory)
            store.saveConfiguration(NotificationConfiguration(sources = listOf(DeveloperBlogSource()), notifyOnFirstCheck = true))
            var permitted = false
            val delivery = object : NotificationDelivery {
                override fun hasPermission() = permitted
                override fun canPost(category: NotificationCategory) = permitted
                override fun ensureChannels(configuration: NotificationConfiguration) = Unit
                override fun cancelCategory(category: NotificationCategory) = Unit
                override fun post(configuration: NotificationConfiguration, category: NotificationCategory, sourceId: String, entry: NotificationEntry) = permitted
            }
            val fetcher = SourceFetcher(object : NotificationHttpClient {
                override suspend fun get(url: String) = if (url.endsWith("/posts")) {
                    """{"posts":[{"slug":"new","title":"News","releaseTimestamp":1,"categories":[]}]}"""
                } else """{"markdown":"Full message"}"""
            })
            val engine = NotificationEngine(store, delivery, "1.0", 1, NotificationRuntimeState(), fetcher)
            assertEquals(0, engine.check().postedNotifications)
            permitted = true
            assertEquals(1, engine.check().postedNotifications)
            assertEquals(0, engine.check().postedNotifications)
        } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.delete(it) } }
        }
    }
}
