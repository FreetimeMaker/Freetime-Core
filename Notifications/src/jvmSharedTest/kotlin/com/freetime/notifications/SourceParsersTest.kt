package com.freetime.notifications

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SourceParsersTest {
    private val fdroid = FDroidVersionSource("com.example.app")

    @Test fun githubFiltersAndSortsWithoutTruncatingChangelog() {
        val json = """[
            {"id":10,"tag_name":"v1","name":"First","draft":false,"prerelease":false,"body":"Full\nchangelog","html_url":"https://github.com/o/r/releases/tag/v1","published_at":"2025-01-01T00:00:00Z"},
            {"id":11,"tag_name":"v2-beta","draft":false,"prerelease":true,"body":null,"html_url":"https://github.com/o/r/releases/tag/v2","published_at":"2025-02-01T00:00:00Z"},
            {"id":12,"tag_name":"draft","draft":true,"prerelease":false},
            {"id":13,"tag_name":"v3","draft":false,"prerelease":false,"body":"New","html_url":"https://github.com/o/r/releases/tag/v3","published_at":"2025-03-01T00:00:00Z"}
        ]"""
        val source = GitHubReleaseSource("o", "r")
        val entries = SourceParsers.parse(source, json)
        assertEquals(listOf("13", "10"), entries.map { it.id })
        assertEquals("Full\nchangelog", entries.last().body)
        assertEquals("v1", entries.last().versionName)
        assertEquals(listOf("13", "11", "10"), SourceParsers.parse(source.copy(includePrereleases = true), json).map { it.id })
    }

    @Test fun githubWithoutDatesPreservesApiOrder() {
        val rows = """[{"id":2,"tag_name":"v2","draft":false,"prerelease":false,"html_url":"https://github.com/o/r/releases/2"},
            {"id":1,"tag_name":"v1","draft":false,"prerelease":false,"html_url":"https://github.com/o/r/releases/1"}]"""
        assertEquals(listOf("2", "1"), SourceParsers.parse(GitHubReleaseSource("o", "r"), rows).map { it.id })
    }

    @Test fun fdroidUsesLongCodesAndSuggestedInsteadOfNewest() {
        val versions = """[{"versionCode":5000000000,"versionName":"5"},{"versionCode":6000000000,"versionName":"6"}]"""
        assertEquals(6000000000L, SourceParsers.parse(fdroid, """{"packages":$versions}""").single().versionCode)
        val selected = SourceParsers.parse(fdroid, """{"suggestedVersionCode":"5000000000","packages":$versions}""").single()
        assertEquals(5000000000L, selected.versionCode)
        assertEquals("5", selected.versionName)
        assertEquals("com.example.app:5000000000", selected.id)
        assertEquals(selected, SourceParsers.parse(fdroid.copy(apiUrl = "https://example.org/index-v1.json"),
            """{"suggestedVersionCode":5000000000,"packages":{"com.example.app":$versions}}""").single())
    }

    @Test fun fdroidDistinguishesEmptyMissingAndMissingSuggested() {
        assertTrue(SourceParsers.parse(fdroid, """{"packages":[]}""").isEmpty())
        assertTrue(SourceParsers.parse(fdroid, """{"packages":{"com.example.app":[]}}""").isEmpty())
        rejects { SourceParsers.parse(fdroid, """{"packages":{}}""") }
        rejects { SourceParsers.parse(fdroid, """{"suggestedVersionCode":5,"packages":[]}""") }
        rejects { SourceParsers.parse(fdroid, """{"suggestedVersionCode":"bad","packages":[]}""") }
        rejects { SourceParsers.parse(fdroid, """{"packages":[{"versionCode":1.5,"versionName":"bad"}]}""") }
    }

    @Test fun blogMatchesAllCategoriesAndEncodesSlug() {
        val json = """{"posts":[
            {"slug":"older","title":"Old","releaseTimestamp":1700000000,"categories":["Android","Release"]},
            {"slug":"not-all","title":"Excluded","releaseTimestamp":1800000000,"categories":["Android"]},
            {"slug":"new & news/é","title":"New","releaseTimestamp":1750000000,"categories":["ANDROID","release"]},
            {"slug":"uncategorized","title":"General","releaseTimestamp":1600000000,"categories":[]}
        ]}"""
        val source = DeveloperBlogSource(categories = setOf("android", "RELEASE"))
        val entries = SourceParsers.parse(source, json)
        assertEquals(listOf("new & news/é", "older"), entries.map { it.id })
        assertEquals("https://blog.free-time.me/blog/?post=new+%26+news%2F%C3%A9", entries.first().url)
        assertEquals("ANDROID, release", entries.first().body)
        assertEquals("", SourceParsers.parse(source.copy(categories = emptySet()), json).last().body)
    }

    @Test fun blogMessageLoadsBodyOnlyForTheSelectedPost() = runBlocking {
        val urls = mutableListOf<String>()
        val fetcher = SourceFetcher(object : NotificationHttpClient {
            override suspend fun get(url: String): String {
                urls += url
                return """{"title":"Message","markdown":"Important developer message"}"""
            }
        })
        val entry = NotificationEntry("new & news", "Headline", url = "https://blog.free-time.me/blog/?post=new")
        val message = fetcher.blogMessage(DeveloperBlogSource(), entry)
        assertEquals(listOf("https://api.free-time.me/v2/blog/posts/new%20%26%20news"), urls)
        assertEquals("Important developer message", message.body)
        assertEquals(entry.id, message.id)
        assertEquals(entry.url, message.url)
        rejects { SourceParsers.blogMessage(entry, """{"markdown":null}""") }
    }

    @Test fun blogCapsAtOneHundred() {
        val posts = (0..100).joinToString(",") {
            """{"slug":"$it","title":"Post $it","releaseTimestamp":$it,"categories":[]}"""
        }
        val entries = SourceParsers.parse(DeveloperBlogSource(), """{"posts":[$posts]}""")
        assertEquals(100, entries.size)
        assertEquals("100", entries.first().id)
    }

    @Test fun malformedPayloadsFailRatherThanLookEmpty() {
        rejects { SourceParsers.parse(fdroid, "not json") }
        rejects { SourceParsers.parse(fdroid, "{}") }
        rejects { SourceParsers.parse(DeveloperBlogSource(), """{"posts":[{"slug":"x"}]}""") }
        rejects { SourceParsers.parse(GitHubReleaseSource("o", "r"), "{}") }
    }

    @Test fun fetcherUsesOneListRequestAndSanitizesFailures() = runBlocking {
        val urls = mutableListOf<String>()
        val fetcher = SourceFetcher(object : NotificationHttpClient {
            override suspend fun get(url: String): String {
                urls += url
                return """{"posts":[]}"""
            }
        })
        assertTrue(fetcher.fetch(DeveloperBlogSource()).isEmpty())
        assertEquals(listOf("https://api.free-time.me/v2/blog/posts"), urls)
        val failed = SourceFetcher(object : NotificationHttpClient {
            override suspend fun get(url: String): String = throw IOException(url)
        })
        try {
            failed.fetch(DeveloperBlogSource(apiUrl = "https://example.org/?secret=hidden"))
            fail("Expected failure")
        } catch (error: IOException) {
            assertFalse(error.message.orEmpty().contains("hidden"))
            assertNull(error.cause)
        }
    }

    @Test fun fetcherRethrowsCancellation() = runBlocking {
        val cancellation = CancellationException("Cancelled")
        val fetcher = SourceFetcher(object : NotificationHttpClient {
            override suspend fun get(url: String): String = throw cancellation
        })
        try {
            fetcher.fetch(fdroid)
            fail("Expected cancellation")
        } catch (error: CancellationException) {
            assertSame(cancellation, error)
        }
    }

    @Test fun customJarExtractsExactMetadataEntry() {
        val json = """{"apps":[{"packageName":"com.example.app","suggestedVersionCode":"5000000000"}],"packages":{"com.example.app":[{"versionCode":5000000000,"versionName":"stable"},{"versionCode":6000000000,"versionName":"experimental"}]}}"""
        val archive = archive("META-INF/MANIFEST.MF" to "Manifest-Version: 1.0\n", "index-v1.json" to json)
        val decoded = decodeNotificationResponse(archive, "application/java-archive")
        assertEquals(json, decoded)
        val source = fdroid.copy(apiUrl = "https://example.org/repo/index-v1.jar")
        assertEquals(5000000000L, SourceParsers.parse(source, decoded).single().versionCode)
        assertEquals("stable", SourceParsers.parse(source, decoded).single().versionName)
    }

    @Test fun archiveRequiresExactTargetAndBoundsAllInflation() {
        rejects { decodeNotificationResponse(archive("nested/index-v1.json" to "{}")) }
        rejects { decodeNotificationResponse(archive("INDEX-V1.JSON" to "{}")) }
        rejects { decodeNotificationResponse(archive("index-v1.json" to "x".repeat(65)), maxDecompressedBytes = 64) }
        rejects { decodeNotificationResponse(archive("ignored" to "x".repeat(65), "index-v1.json" to "{}"), maxDecompressedBytes = 64) }
        rejects { decodeNotificationResponse(archive("ignored" to "x".repeat(63), "index-v1.json" to "{}"), maxDecompressedBytes = 64) }
        assertEquals("{}", decodeNotificationResponse(archive("index-v1.json" to "{}"), maxDecompressedBytes = 2))
    }

    @Test fun plainJsonPreservesCharsetSupport() {
        val json = """{"title":"café"}"""
        assertEquals(json, decodeNotificationResponse(json.toByteArray(Charsets.UTF_8)))
        val latin = java.nio.charset.Charset.forName("ISO-8859-1")
        assertEquals(json, decodeNotificationResponse(json.toByteArray(latin), "application/json; charset=\"ISO-8859-1\""))
    }

    @Test fun fdroidRejectsWrongPackageAndMissingAppSuggestionTarget() {
        rejects { SourceParsers.parse(fdroid, """{"packageName":"com.other.app","packages":[]}""") }
        assertTrue(SourceParsers.parse(fdroid, """{"packageName":"com.example.app","packages":[]}""").isEmpty())
        val json = """{"apps":[{"packageName":"com.other.app","suggestedVersionCode":"9"},{"packageName":"com.example.app","suggestedVersionCode":"5"}],"packages":{"com.example.app":[{"versionCode":6,"versionName":"experimental"}]}}"""
        rejects { SourceParsers.parse(fdroid, json) }
        rejects { SourceParsers.parse(fdroid, json.replace("\"5\"", "\"invalid\"")) }
        val available = json.replace("\"versionCode\":6", "\"versionCode\":5")
        assertEquals(5L, SourceParsers.parse(fdroid, available).single().versionCode)
        assertEquals(5L, SourceParsers.parse(fdroid, available.replace("\"packages\":", "\"suggestedVersionCode\":6,\"packages\":")).single().versionCode)
    }

    private fun archive(vararg entries: Pair<String, String>): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(output).use { zip ->
            entries.forEach { (name, text) ->
                zip.putNextEntry(java.util.zip.ZipEntry(name))
                zip.write(text.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return output.toByteArray()
    }

    private fun rejects(block: () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
            return
        }
        fail("Expected malformed data to fail")
    }
}
