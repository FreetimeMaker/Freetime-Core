package com.freetime.notifications

import org.junit.Assert.*
import org.junit.Test

class NotificationPolicyTest {
    @Test fun versionsRespectNumericSegmentsAndPrereleases() {
        assertTrue(compareReleaseVersions("v1.10.0", "1.9.9")!! > 0)
        assertEquals(0, compareReleaseVersions("1.2", "1.2.0+build"))
        assertTrue(compareReleaseVersions("2.0.0-beta.10", "2.0.0-beta.2")!! > 0)
        assertTrue(compareReleaseVersions("2.0.0", "2.0.0-rc.1")!! > 0)
        assertTrue(compareReleaseVersions("2.0.0-rc.1", "2.0.0")!! < 0)
        assertNull(compareReleaseVersions("nightly", "1.0.0"))
        assertNull(compareReleaseVersions("1.0.0", "unknown"))
    }

    @Test fun numericVersionCodesTakePrecedenceAndSupportLongs() {
        assertTrue(shouldNotifyRelease(NotificationEntry("a", "Release", versionCode = 5_000_000_000L), "99.0", 2_000_000_000))
        assertFalse(shouldNotifyRelease(NotificationEntry("a", "Release", versionName = "99.0", versionCode = 10), "1.0", 11))
        assertFalse(shouldNotifyRelease(NotificationEntry("a", "Release", versionName = "nightly"), "1.0", 1))
    }

    @Test fun firstCheckBaselinesWithoutAlertingByDefault() {
        val entries = listOf(NotificationEntry("1", "New post"))
        val plan = planNotificationBatch(entries, null, false, NotificationCategory.DEVELOPER_MESSAGES, "1.0", 1)
        assertEquals(setOf("1"), plan.seen)
        assertTrue(plan.pending.isEmpty())
        assertEquals(entries, planNotificationBatch(entries, null, true, NotificationCategory.DEVELOPER_MESSAGES, "1.0", 1).pending)
    }

    @Test fun successfulEmptyBaselineStillRecognizesFirstNewPost() {
        val entries = listOf(NotificationEntry("1", "New post"))
        assertEquals(entries, planNotificationBatch(entries, emptySet(), false, NotificationCategory.DEVELOPER_MESSAGES, "1.0", 1).pending)
    }

    @Test fun deduplicationDoesNotMarkUnpostedNotificationsAsDelivered() {
        val entries = listOf(NotificationEntry("2", "New post"), NotificationEntry("1", "Old post"))
        val plan = planNotificationBatch(entries, setOf("1"), false, NotificationCategory.DEVELOPER_MESSAGES, "1.0", 1)
        assertEquals(listOf(entries.first()), plan.pending)
        assertEquals(setOf("1"), plan.seen)
    }

    @Test fun installedAndOlderVersionsAreNotUpdateAlerts() {
        val entries = listOf(
            NotificationEntry("old", "Old", versionName = "0.9"),
            NotificationEntry("same", "Installed", versionName = "1.0"),
            NotificationEntry("new", "New", "Release notes", versionName = "1.1"),
        )
        val plan = planNotificationBatch(entries, emptySet(), true, NotificationCategory.UPDATES, "1.0", 1)
        assertEquals(setOf("old", "same"), plan.seen)
        assertEquals(listOf(entries.last()), plan.pending)
    }

    @Test fun changelogsRequireReleaseNotesAndANewerVersion() {
        val entries = listOf(
            NotificationEntry("empty", "Empty notes", versionName = "2.0"),
            NotificationEntry("notes", "Notes", "Changes", versionName = "2.0"),
        )
        val plan = planNotificationBatch(entries, emptySet(), true, NotificationCategory.CHANGELOGS, "1.0", 1)
        assertEquals(setOf("empty"), plan.seen)
        assertEquals(listOf(entries.last()), plan.pending)
    }

    @Test fun stableKeysSeparateSourcesEntriesAndCategories() {
        val first = notificationKey(NotificationCategory.UPDATES, "a:b", "c")
        assertEquals(first, notificationKey(NotificationCategory.UPDATES, "a:b", "c"))
        assertNotEquals(first, notificationKey(NotificationCategory.UPDATES, "a", "b:c"))
        assertNotEquals(first, notificationKey(NotificationCategory.CHANGELOGS, "a:b", "c"))
    }
}
