package com.freetime.notifications

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal object NotificationConfigurationCodec {
    private const val VERSION = 1

    fun encode(config: NotificationConfiguration): String = JSONObject().apply {
        put("version", VERSION)
        put("smallIcon", config.smallIcon)
        put("checkIntervalMinutes", config.checkIntervalMinutes)
        put("maxNotificationsPerCheck", config.maxNotificationsPerCheck)
        put("notifyOnFirstCheck", config.notifyOnFirstCheck)
        put("enabledCategories", JSONArray(config.enabledCategories.map { it.name }))
        put("channels", JSONObject().apply {
            config.channels.forEach { (category, options) ->
                put(category.name, JSONObject().apply {
                    put("name", options.name)
                    put("description", options.description)
                    put("importance", options.importance)
                })
            }
        })
        put("sources", JSONArray().apply {
            config.sources.forEach { source ->
                put(JSONObject().apply {
                    put("id", source.id)
                    put("category", source.category.name)
                    when (source) {
                        is GitHubReleaseSource -> {
                            put("type", "github")
                            put("owner", source.owner)
                            put("repository", source.repository)
                            put("includePrereleases", source.includePrereleases)
                        }
                        is FDroidVersionSource -> {
                            put("type", "fdroid")
                            put("packageName", source.packageName)
                            put("apiUrl", source.apiUrl ?: JSONObject.NULL)
                        }
                        is DeveloperBlogSource -> {
                            put("type", "blog")
                            put("categories", JSONArray(source.categories.toList()))
                            put("apiUrl", source.apiUrl)
                            put("blogUrl", source.blogUrl)
                        }
                    }
                })
            }
        })
    }.toString()

    /** Invalid persisted data is an error, not an absent configuration. */
    fun decode(encoded: String): NotificationConfiguration = try {
        val json = JSONObject(encoded)
        require(json.integer("version") == VERSION) { "Unsupported notification configuration version" }
        val sources = json.getJSONArray("sources")
        val channels = json.getJSONObject("channels")
        NotificationConfiguration(
            smallIcon = json.integer("smallIcon"),
            sources = (0 until sources.length()).map { decodeSource(sources.getJSONObject(it)) },
            checkIntervalMinutes = json.long("checkIntervalMinutes"),
            enabledCategories = json.getJSONArray("enabledCategories").strings()
                .map(::category).toSet(),
            channels = channels.keys().asSequence().associate { key ->
                category(key) to channels.getJSONObject(key).let {
                    NotificationChannelOptions(
                        name = it.string("name"),
                        description = it.string("description"),
                        importance = it.integer("importance"),
                    )
                }
            },
            notifyOnFirstCheck = json.boolean("notifyOnFirstCheck"),
            maxNotificationsPerCheck = json.integer("maxNotificationsPerCheck"),
        )
    } catch (error: JSONException) {
        throw IllegalArgumentException("Malformed notification configuration", error)
    }

    private fun decodeSource(json: JSONObject): NotificationSource {
        val id = json.string("id")
        val source = when (val type = json.string("type")) {
            "github" -> GitHubReleaseSource(
                owner = json.string("owner"),
                repository = json.string("repository"),
                includePrereleases = json.boolean("includePrereleases"),
                id = id,
            )
            "fdroid" -> FDroidVersionSource(
                packageName = json.string("packageName"),
                apiUrl = if (json.get("apiUrl") == JSONObject.NULL) null else json.string("apiUrl"),
                id = id,
            )
            "blog" -> DeveloperBlogSource(
                categories = json.getJSONArray("categories").strings().toSet(),
                apiUrl = json.string("apiUrl"),
                blogUrl = json.string("blogUrl"),
                id = id,
            )
            else -> throw IllegalArgumentException("Unknown notification source type: $type")
        }
        require(category(json.string("category")) == source.category) { "Source category does not match its type" }
        return source
    }

    private fun category(value: String): NotificationCategory = NotificationCategory.valueOf(value)

    private fun JSONObject.string(key: String): String {
        val value = get(key)
        require(value is String) { "$key must be a string" }
        return value
    }

    private fun JSONObject.boolean(key: String): Boolean {
        val value = get(key)
        require(value is Boolean) { "$key must be a boolean" }
        return value
    }

    private fun JSONObject.long(key: String): Long {
        val value = get(key)
        require(value is Int || value is Long) { "$key must be an integer" }
        return (value as Number).toLong()
    }

    private fun JSONObject.integer(key: String): Int {
        val value = long(key)
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "$key is outside the integer range" }
        return value.toInt()
    }

    private fun JSONArray.strings(): List<String> = (0 until length()).map {
        val value = get(it)
        require(value is String) { "Expected a string array" }
        value
    }
}
