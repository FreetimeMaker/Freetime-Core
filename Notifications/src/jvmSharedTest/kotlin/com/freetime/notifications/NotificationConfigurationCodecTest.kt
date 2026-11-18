package com.freetime.notifications

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class NotificationConfigurationCodecTest {
    private val configuration = NotificationConfiguration(
        smallIcon = 123,
        sources = listOf(
            GitHubReleaseSource("FreeTime", "Core", includePrereleases = true, id = "github-custom"),
            FDroidVersionSource("com.example.app", id = "fdroid-public"),
            FDroidVersionSource("com.example.app", "https://example.org/index-v1.json", "fdroid-custom"),
            DeveloperBlogSource(
                categories = setOf("Android", "Release", "News / é"),
                apiUrl = "https://example.org/posts",
                blogUrl = "https://example.org/blog",
                id = "blog-custom",
            ),
        ),
        checkIntervalMinutes = 45,
        enabledCategories = setOf(NotificationCategory.UPDATES, NotificationCategory.CHANGELOGS),
        channels = mapOf(
            NotificationCategory.UPDATES to NotificationChannelOptions("Updates", "New versions", 4),
            NotificationCategory.CHANGELOGS to NotificationChannelOptions("Changelogs", "Release notes", 2),
            NotificationCategory.DEVELOPER_MESSAGES to NotificationChannelOptions("Developer news", "Posts", 1),
        ),
        notifyOnFirstCheck = true,
        maxNotificationsPerCheck = 17,
    )

    @Test fun roundTripPreservesAllSourcesAndOptions() {
        assertEquals(configuration, roundTrip(configuration))
    }

    @Test fun roundTripPreservesDefaultsAndEmptyCollections() {
        val defaults = NotificationConfiguration(smallIcon = 1)
        assertEquals(defaults, roundTrip(defaults))
        val empty = defaults.copy(enabledCategories = emptySet(), checkIntervalMinutes = 15)
        assertEquals(empty, roundTrip(empty))
        val defaultSources = defaults.copy(sources = listOf(
            GitHubReleaseSource("owner", "repository"),
            FDroidVersionSource("com.example.app"),
            DeveloperBlogSource(),
        ))
        assertEquals(defaultSources, roundTrip(defaultSources))
    }

    @Test fun nullableFdroidApiUrlRemainsNull() {
        val sources = roundTrip(configuration).sources.filterIsInstance<FDroidVersionSource>()
        assertNull(sources[0].apiUrl)
        assertEquals("https://example.org/index-v1.json", sources[1].apiUrl)
    }

    @Test fun explicitIdsAreIndependentOfSourceDetails() {
        val first = GitHubReleaseSource("owner", "repo", id = "first / : é")
        val second = first.copy(id = "second")
        val config = NotificationConfiguration(1, sources = listOf(first, second))
        assertEquals(listOf(first, second), roundTrip(config).sources)
    }

    @Test fun unknownAndMissingVersionsFail() {
        for (version in listOf(-1, 0, 2, Int.MAX_VALUE)) {
            rejects { decode(json().put("version", version)) }
        }
        rejects { decode(json().apply { remove("version") }) }
        rejects { decode(json().put("version", "1")) }
    }

    @Test fun unknownSourceTypesFail() {
        rejects { decode(json().apply { getJSONArray("sources").getJSONObject(0).put("type", "rss") }) }
    }

    @Test fun unknownCategoriesFailEverywhere() {
        rejects { decode(json().put("enabledCategories", JSONArray().put("UNKNOWN"))) }
        rejects { decode(json().apply {
            getJSONObject("channels").put("UNKNOWN", JSONObject().put("name", "Unknown"))
        }) }
        rejects { decode(json().apply {
            getJSONArray("sources").getJSONObject(0).put("category", "UNKNOWN")
        }) }
    }

    @Test fun mismatchedSourceCategoryFails() {
        rejects { decode(json().apply {
            getJSONArray("sources").getJSONObject(0).put("category", "CHANGELOGS")
        }) }
    }

    @Test fun duplicateIdsAreRejectedByModelAndDecoder() {
        val source = GitHubReleaseSource("owner", "repo", id = "duplicate")
        rejects { NotificationConfiguration(1, sources = listOf(source, source.copy(repository = "other"))) }
        rejects { decode(json().apply {
            val sources = getJSONArray("sources")
            sources.getJSONObject(1).put("id", sources.getJSONObject(0).getString("id"))
        }) }
    }

    @Test fun intervalsBelowFifteenAreRejectedByModelAndDecoder() {
        rejects { NotificationConfiguration(1, checkIntervalMinutes = 14) }
        rejects { decode(json().put("checkIntervalMinutes", 14)) }
        assertEquals(15L, roundTrip(configuration.copy(checkIntervalMinutes = 15)).checkIntervalMinutes)
    }

    @Test fun malformedOrWronglyTypedDataFailsExplicitly() {
        rejects { NotificationConfigurationCodec.decode("not json") }
        rejects { NotificationConfigurationCodec.decode("{}") }
        rejects { decode(json().put("notifyOnFirstCheck", "true")) }
        rejects { decode(json().put("smallIcon", 1.5)) }
        rejects { decode(json().put("smallIcon", Long.MAX_VALUE)) }
        rejects { decode(json().put("enabledCategories", JSONArray().put(123))) }
        rejects { decode(json().apply { getJSONArray("sources").getJSONObject(1).remove("apiUrl") }) }
    }

    private fun roundTrip(config: NotificationConfiguration) =
        NotificationConfigurationCodec.decode(NotificationConfigurationCodec.encode(config))

    private fun json() = JSONObject(NotificationConfigurationCodec.encode(configuration))

    private fun decode(json: JSONObject) = NotificationConfigurationCodec.decode(json.toString())

    private fun rejects(block: () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Invalid persisted configuration must be reported rather than treated as absent.
        }
    }
}
