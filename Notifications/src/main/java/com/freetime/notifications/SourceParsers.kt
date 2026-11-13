package com.freetime.notifications

import java.net.URLEncoder
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONObject

internal object SourceParsers {
    fun parse(source: NotificationSource, json: String): List<NotificationEntry> = when (source) {
        is GitHubReleaseSource -> github(source, JSONArray(json))
        is FDroidVersionSource -> fdroid(source, JSONObject(json))
        is DeveloperBlogSource -> blog(source, JSONObject(json))
    }

    fun blogMessage(entry: NotificationEntry, json: String): NotificationEntry {
        val post = JSONObject(json)
        val markdown = post.get("markdown")
        require(markdown is String) { "Missing blog message text" }
        val title = if (post.isNull("title")) entry.title else requiredText(post, "title")
        return entry.copy(title = title, body = markdown.take(8_000))
    }

    private fun requiredText(row: JSONObject, key: String): String =
        row.getString(key).also { require(it.isNotBlank()) { "Missing $key" } }

    private fun longValue(row: JSONObject, key: String): Long {
        val value = row.get(key)
        require(value is Number || value is String) { "Invalid $key" }
        return value.toString().toLongOrNull() ?: error("Invalid $key")
    }

    private fun timestamp(value: String): Long {
        // java.time requires API 26 or app-level desugaring; keep the SDK usable on API 24.
        for (pattern in listOf("yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")) {
            val format = SimpleDateFormat(pattern, Locale.ROOT).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val position = ParsePosition(0)
            val date = format.parse(value, position)
            if (date != null && position.index == value.length) return date.time
        }
        throw IllegalArgumentException("Invalid release timestamp")
    }

    private fun github(source: GitHubReleaseSource, rows: JSONArray): List<NotificationEntry> {
        val releases = (0 until rows.length()).map { rows.getJSONObject(it) }
            .filter { !it.getBoolean("draft") && (source.includePrereleases || !it.getBoolean("prerelease")) }
        val dated = releases.map { row ->
            val date = if (row.isNull("published_at")) null else timestamp(row.getString("published_at"))
            row to date
        }
        // If dates are incomplete, preserve the API's ordering rather than inventing chronology.
        val ordered = if (dated.all { it.second != null }) dated.sortedByDescending { it.second } else dated
        return ordered.take(100).map { (row, _) ->
            val tag = requiredText(row, "tag_name")
            NotificationEntry(
                id = longValue(row, "id").toString(),
                title = if (row.isNull("name")) tag else row.optString("name").takeIf { it.isNotBlank() } ?: tag,
                body = if (row.isNull("body")) "" else row.getString("body"),
                url = requiredText(row, "html_url").also(::requireHttpsUrl),
                versionName = tag,
            )
        }
    }

    private fun fdroid(source: FDroidVersionSource, root: JSONObject): List<NotificationEntry> {
        if (root.has("packageName")) {
            require(root.getString("packageName") == source.packageName) { "Package name does not match source" }
        }
        val packages = root.get("packages")
        val versions = when (packages) {
            is JSONArray -> packages
            is JSONObject -> {
                require(packages.has(source.packageName)) { "Package missing from index" }
                packages.getJSONArray(source.packageName)
            }
            else -> error("Invalid packages")
        }
        val app = if (root.has("apps")) {
            val apps = root.getJSONArray("apps")
            (0 until apps.length()).map { apps.getJSONObject(it) }
                .firstOrNull { it.getString("packageName") == source.packageName }
        } else null
        val suggestionOwner = when {
            app != null && app.has("suggestedVersionCode") && !app.isNull("suggestedVersionCode") -> app
            root.has("suggestedVersionCode") && !root.isNull("suggestedVersionCode") -> root
            else -> null
        }
        val suggested = suggestionOwner?.let {
            longValue(it, "suggestedVersionCode").also { code -> require(code > 0) }
        }
        val rows = (0 until versions.length()).map { versions.getJSONObject(it) }
            .map { it to longValue(it, "versionCode").also { code -> require(code > 0) } }
        val selected = if (suggested != null) {
            rows.firstOrNull { it.second == suggested } ?: error("Suggested version missing from packages")
        } else rows.maxByOrNull { it.second } ?: return emptyList()
        val (row, code) = selected
        return listOf(NotificationEntry(
            id = "${source.packageName}:$code",
            title = source.packageName,
            url = "https://f-droid.org/packages/${source.packageName}/",
            versionName = requiredText(row, "versionName"),
            versionCode = code,
        ))
    }

    private fun blog(source: DeveloperBlogSource, root: JSONObject): List<NotificationEntry> {
        val posts = root.getJSONArray("posts")
        return (0 until posts.length()).map { posts.getJSONObject(it) }.map { row ->
            val categories = row.getJSONArray("categories").let { values ->
                (0 until values.length()).map { values.getString(it) }
            }
            Triple(row, categories, longValue(row, "releaseTimestamp"))
        }.filter { (_, categories, _) ->
            source.categories.all { requested -> categories.any { it.equals(requested, ignoreCase = true) } }
        }.sortedByDescending { it.third }.take(100).map { (row, categories, _) ->
            val slug = requiredText(row, "slug")
            NotificationEntry(
                id = slug,
                title = requiredText(row, "title"),
                body = categories.joinToString(", "),
                url = "${source.blogUrl.trimEnd('/')}/blog/?post=${URLEncoder.encode(slug, "UTF-8")}",
            )
        }
    }
}
