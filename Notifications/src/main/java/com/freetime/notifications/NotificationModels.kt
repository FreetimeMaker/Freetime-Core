package com.freetime.notifications

import java.net.URI

/** Each category has its own Android channel and persisted app-level preference. */
enum class NotificationCategory {
    UPDATES, CHANGELOGS, DEVELOPER_MESSAGES,
}

data class NotificationChannelOptions(
    val name: String,
    val description: String = "",
    val importance: Int = 3,
) {
    init {
        require(name.isNotBlank())
        require(importance in 1..4)
    }
}

sealed interface NotificationSource {
    val id: String
    val category: NotificationCategory
}

data class GitHubReleaseSource(
    val owner: String,
    val repository: String,
    val includePrereleases: Boolean = false,
    override val id: String = "github:$owner/$repository",
) : NotificationSource {
    override val category = NotificationCategory.UPDATES
    init {
        require(owner.matches(Regex("[A-Za-z0-9_.-]+")))
        require(repository.matches(Regex("[A-Za-z0-9_.-]+")))
    }
}

/** Null apiUrl uses F-Droid's public per-package API. Custom URLs may serve index-v1 JSON. */
data class FDroidVersionSource(
    val packageName: String,
    val apiUrl: String? = null,
    override val id: String = "fdroid:${apiUrl.orEmpty()}:$packageName",
) : NotificationSource {
    override val category = NotificationCategory.UPDATES
    init {
        require(packageName.matches(Regex("[A-Za-z0-9_]+(?:\\.[A-Za-z0-9_]+)+")))
        apiUrl?.let(::requireHttpsUrl)
    }
}

/** Public API actually used by blog.free-time.me; categories are matched case-insensitively. */
data class DeveloperBlogSource(
    val categories: Set<String> = emptySet(),
    val apiUrl: String = "https://api.free-time.me/v2/blog/posts",
    val blogUrl: String = "https://blog.free-time.me",
    override val id: String = "developer-blog:$apiUrl:${categories.sorted().joinToString(",")}",
) : NotificationSource {
    override val category = NotificationCategory.DEVELOPER_MESSAGES
    init {
        requireHttpsUrl(apiUrl)
        requireHttpsUrl(blogUrl)
    }
}

/** A stable remote ID is required; text edits alone do not create new notifications. */
data class NotificationEntry(
    val id: String,
    val title: String,
    val body: String = "",
    val url: String? = null,
    val versionName: String? = null,
    val versionCode: Long? = null,
)

data class NotificationConfiguration(
    val smallIcon: Int,
    val sources: List<NotificationSource> = emptyList(),
    val checkIntervalMinutes: Long = 360,
    val enabledCategories: Set<NotificationCategory> = NotificationCategory.entries.toSet(),
    val channels: Map<NotificationCategory, NotificationChannelOptions> = emptyMap(),
    val notifyOnFirstCheck: Boolean = false,
    val maxNotificationsPerCheck: Int = 5,
) {
    init {
        require(smallIcon != 0) { "Supply the app's notification small-icon resource" }
        require(checkIntervalMinutes >= 15) { "WorkManager requires at least 15 minutes" }
        require(maxNotificationsPerCheck in 1..50)
        require(sources.size <= 50)
        require(sources.all { it.id.isNotBlank() && it.id.length <= 1024 })
        require(sources.map { it.id }.distinct().size == sources.size) { "Source IDs must be unique" }
    }
}

data class NotificationCheckResult(
    val checkedSources: Int,
    val postedNotifications: Int,
    val failures: Map<String, String>,
    val permissionGranted: Boolean,
)

internal fun requireHttpsUrl(value: String) {
    val uri = URI(value)
    require(uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank() && uri.userInfo == null) {
        "Use an HTTPS URL without embedded credentials"
    }
}
