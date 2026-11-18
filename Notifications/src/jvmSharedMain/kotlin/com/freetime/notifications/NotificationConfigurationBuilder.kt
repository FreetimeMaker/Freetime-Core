package com.freetime.notifications

class NotificationConfigurationBuilder {
    var smallIcon: Int = 0
    var checkIntervalMinutes: Long = 360
    var notifyOnFirstCheck: Boolean = false
    var maxNotificationsPerCheck: Int = 5
    private val sources = mutableListOf<NotificationSource>()
    private val enabled = NotificationCategory.entries.toMutableSet()
    private val channels = mutableMapOf<NotificationCategory, NotificationChannelOptions>()

    fun github(owner: String, repository: String, includePrereleases: Boolean = false) {
        sources += GitHubReleaseSource(owner, repository, includePrereleases)
    }

    fun fdroid(packageName: String, apiUrl: String? = null) {
        sources += FDroidVersionSource(packageName, apiUrl)
    }

    fun developerMessages(vararg categories: String) {
        sources += DeveloperBlogSource(categories.toSet())
    }

    fun source(source: NotificationSource) { sources += source }

    fun category(category: NotificationCategory, enabled: Boolean = true, options: NotificationChannelOptions? = null) {
        if (enabled) this.enabled += category else this.enabled -= category
        options?.let { channels[category] = it }
    }

    fun build() = NotificationConfiguration(
        smallIcon, sources.toList(), checkIntervalMinutes, enabled.toSet(), channels.toMap(),
        notifyOnFirstCheck, maxNotificationsPerCheck,
    )
}
