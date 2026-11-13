# Notifications

Local Android notifications for **app updates**, **GitHub release-note changelogs** and **developer blog messages**. Module `:Notifications` uses namespace `com.freetime.notifications` and exposes a dependency on `:Core`; it uses AndroidX WorkManager, Preferences DataStore and coroutines. Minimum Android SDK is 24.

This is fully local polling: the device fetches public metadata and posts Android notifications. No Firebase, UnifiedPush, push registration, Freetime account or notification backend is required. The optional blog source reads an existing public content API, not a push backend. There is no immediate push-delivery guarantee. GitLab, RSS and arbitrary reminders are not supported.

## Installation and publication status

Notifications is introduced in **3.2.0**. This release is prepared but **not yet uploaded**; the Maven dependency becomes available after actual publication.

Add `https://repo.free-time.me/maven/` to your app's `settings.gradle.kts` as shown in the root [repository setup](../README.md#installation). Public consumption requires no credentials. After publication, add to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.freetime:Notifications:3.2.0")
}
```

For local integration before publication, or when working in this repository, use this alternative instead:

```kotlin
dependencies {
    implementation(project(":Notifications"))
}
```

For another checkout, first include the local `Notifications` and `Core` projects in that build. Notifications was not part of the five-module 3.1.0 release.

## Configure from an application-owned coroutine

`initialize` is suspending: it persists configuration, creates channels, schedules periodic work and requests a best-effort initial check. Call it from an application-owned scope, not `runBlocking` or blocking work on the main thread. The implementation moves its storage and scheduling work to `Dispatchers.IO`.

The following example assumes the host app defines the referenced drawable and localized string resources. Replace the repository with the repository for **this installed app**. The two update sources below are alternatives; enable both only if both describe the same app and version scheme.

```kotlin
import android.app.Application
import com.freetime.notifications.FreetimeNotifications
import com.freetime.notifications.NotificationCategory
import com.freetime.notifications.NotificationChannelOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            FreetimeNotifications.initialize(this@MyApplication) {
                smallIcon = R.drawable.ic_stat_notification
                checkIntervalMinutes = 360 // Six hours; minimum is 15 minutes.
                notifyOnFirstCheck = false
                maxNotificationsPerCheck = 5

                github(owner = "your-owner", repository = "your-app")
                // Alternative: fdroid(packageName = packageName)
                developerMessages("Android", "Releases")

                category(
                    NotificationCategory.UPDATES,
                    options = NotificationChannelOptions(
                        name = getString(R.string.channel_updates),
                        description = getString(R.string.channel_updates_description),
                        importance = 3,
                    ),
                )
                category(
                    NotificationCategory.CHANGELOGS,
                    options = NotificationChannelOptions(
                        name = getString(R.string.channel_changelogs),
                    ),
                )
                category(
                    NotificationCategory.DEVELOPER_MESSAGES,
                    options = NotificationChannelOptions(
                        name = getString(R.string.channel_developer_messages),
                    ),
                )
            }
        }
    }
}
```

Register your application class in the host manifest. Handle initialization errors through the app's normal coroutine error handling. The host must supply a valid app-owned drawable or mipmap small icon; use a notification-appropriate monochrome status icon, not a library resource. Its resource name is persisted and resolved against the current installation, rather than reusing a potentially stale numeric resource ID after an app update.

The DSL supports `github(owner, repository, includePrereleases = false)`, `fdroid(packageName, apiUrl = null)`, `developerMessages(vararg categories)`, `source(NotificationSource)` and `category(category, enabled = true, options = null)`. You can also pass a `NotificationConfiguration` directly to `initialize(context, configuration)`. `NotificationSource` is sealed to the three supported source models, not an arbitrary fetcher extension point.

All three categories default to enabled. Configuration allows up to 50 sources with unique, nonblank IDs (at most 1,024 characters each), and `maxNotificationsPerCheck` accepts 1–50, defaulting to 5 across all sources and categories in a check.

## Sources and version rules

### GitHub releases

`GitHubReleaseSource` fetches the public endpoint `https://api.github.com/repos/{owner}/{repository}/releases?per_page=100`. Drafts are excluded; prereleases are excluded unless `includePrereleases` is true. Releases are sorted newest first when all publication timestamps are available; otherwise API ordering is retained.

Configure only a repository whose releases represent the installed app. The library compares release tags against the host's installed `versionName`; it does not verify that a repository belongs to that app. Numeric/semver-style versions with an optional `v` or `V` prefix are supported, including prerelease ordering; build metadata does not affect ordering. Equal or older versions do not alert. Unknown/unorderable tags, or an unorderable installed version name, do not become update alerts.

A newer GitHub release can produce an `UPDATES` notification and, independently, a `CHANGELOGS` notification **only if its release body is nonblank**. These categories have separate channels and deduplication state. Changelogs are not notifications for arbitrary edits to release notes.

### F-Droid versions

`fdroid(packageName)` uses `https://f-droid.org/api/v1/packages/{packageName}`. Use the package name of the installed host app: comparison is against that app's installed **long version code**, not a separate package queried from the device.

The parser selects `suggestedVersionCode` when present (from matching app metadata or the response root); otherwise it selects the highest version code. A declared suggested version missing from the package list is a source failure, not a fallback to an unrelated release. Only a selected code greater than the installed code produces an update alert. F-Droid does **not** produce changelog notifications.

For a custom repository, `fdroid(packageName, apiUrl = "https://…")` accepts an HTTPS per-package API, raw `index-v1.json`, or `index-v1.jar` containing `index-v1.json`. Responses are bounded to 16 MiB; archive inflation is also bounded to 16 MiB, including ignored entries. Large indexes may exceed these limits. ZIP/JAR signatures are **not validated**: metadata is informational only, not a trusted installation source. There is no APK download or installation. F-Droid notification links currently open `https://f-droid.org/packages/{packageName}/`, including for custom metadata URLs.

### Developer blog messages

`developerMessages()` reads all posts from `https://api.free-time.me/v2/blog/posts`, the public API actually used by `https://blog.free-time.me`. It does not use an invented RSS endpoint or require a tracking token.

`developerMessages("Android", "Releases")` requires **all** requested categories to occur in a post's categories, matched case-insensitively (AND, not OR). An empty filter accepts all categories. Posts are ordered by `releaseTimestamp`, with up to 100 matching posts retained. The stable entry ID is the post slug. Full Markdown text is fetched from `/v2/blog/posts/{encodedSlug}` only for posts about to be delivered, not for every historical entry. If that request fails, the headline and an open-post prompt are delivered instead, and the failure is included in the check result. The link is `https://blog.free-time.me/blog/?post={encodedSlug}`, with the slug URL-encoded as a query value.

For explicit source configuration, use `source(DeveloperBlogSource(categories = setOf("Android")))`. The model also exposes HTTPS `apiUrl`, `blogUrl` and an optional explicit `id`; a custom endpoint must follow the same blog JSON schema, not an arbitrary feed format.

## Permission and channels

The module manifest declares `INTERNET`, `ACCESS_NETWORK_STATE` and `POST_NOTIFICATIONS`. It never opens a permission prompt. On Android 13+, the **host Activity** requests `POST_NOTIFICATIONS`, preferably after explaining the feature and receiving a user action. For example, using AndroidX Activity Result APIs:

```kotlin
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.freetime.notifications.FreetimeNotifications

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) FreetimeNotifications.requestCheck(this)
    }

    // Call from the host's notification opt-in button, not from Application.
    fun enableNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !FreetimeNotifications.hasPermission(this)
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else if (FreetimeNotifications.hasPermission(this)) {
            FreetimeNotifications.requestCheck(this)
        }
    }
}
```

`hasPermission(context)` checks runtime permission and app-wide notification enablement; it does not mean every channel is enabled. If the user disabled notifications in system settings, respect that choice and provide an appropriate settings route rather than repeatedly prompting.

On Android 8+, each category has a stable channel:

| Category             | Channel ID                                  |
| -------------------- | ------------------------------------------- |
| `UPDATES`            | `freetime.notifications.updates`            |
| `CHANGELOGS`         | `freetime.notifications.changelogs`         |
| `DEVELOPER_MESSAGES` | `freetime.notifications.developer_messages` |

Supply localized `NotificationChannelOptions` through `category(...)`. Importance accepts 1 (minimum), 2 (low), 3 (default) or 4 (high). Android channel settings and user choices take precedence: importance cannot be changed programmatically once the channel exists. Reinitializing is not a way to override a user's channel settings. Built-in fallback channel labels and the changelog title prefix are English.

Notifications open the entry's valid HTTPS URL using `ACTION_VIEW`, or fall back to launching the app if no valid link is available.

## Polling and immediate checks

Periodic work defaults to **360 minutes (six hours)**; the minimum accepted interval is **15 minutes**. WorkManager requires connectivity and controls actual execution: Doze, battery restrictions and other OS scheduling can delay checks. This is not a precise timer or immediate push.

For an immediate foreground HTTP check, call the suspending API from a coroutine, after initialization has completed:

```kotlin
val result = FreetimeNotifications.checkNow(context)
// result.checkedSources: successfully fetched sources
// result.postedNotifications: notifications actually posted
// result.failures: map from source ID to failure message
// result.permissionGranted: runtime/app-wide permission status
```

`checkNow` performs network work on `Dispatchers.IO` rather than merely enqueueing a worker. It shares a mutex with background checks, so it can wait for an active check. Without saved configuration it returns zero counts and an empty failure map. Source fetch/parse failures are returned per source while other sources continue; storage/configuration failures can still throw, and cancellation propagates.

`FreetimeNotifications.requestCheck(context)` is non-suspending and enqueues a **best-effort** one-time WorkManager check with a connectivity constraint. Existing one-time check work is replaced, so a newer explicit request supersedes the previous one. `initialize` requests this kind of check; it does not await a foreground HTTP result.

WorkManager persists scheduled work across process death and reboot, and the worker reconstructs sources from saved configuration. No custom boot receiver is needed. The host must meet normal WorkManager initialization requirements; if it disables the default initializer, it must provide its custom WorkManager configuration/initialization before using these APIs.

## Persistent state and user controls

Preferences DataStore stores source configuration, channel options, category overrides and per-source, per-category seen IDs. State survives process restarts. The first **successful** check establishes a silent baseline for each enabled source/category unless `notifyOnFirstCheck = true`; even then, release alerts must be newer than the installed app. A successful empty feed also establishes a baseline; a failed fetch does not.

Deduplication retains up to **500 IDs per source/category**, prioritizing the current feed window. GitHub and blog windows are at most 100 entries; F-Droid selects one version. This is bounded polling history, not a complete archive: posts missed outside the window may never be seen, and sufficiently old IDs evicted from history can be considered unseen if they return. Text edits alone do not create a new notification. Entries that cannot be posted because of permission/channel restrictions, or the per-check posting limit, remain pending rather than being marked delivered (apart from the intentional silent initial baseline).

Use these suspending APIs from a coroutine for app-level toggles:

```kotlin
FreetimeNotifications.setCategoryEnabled(
    context, NotificationCategory.DEVELOPER_MESSAGES, enabled = false,
)
val enabled = FreetimeNotifications.isCategoryEnabled(
    context, NotificationCategory.DEVELOPER_MESSAGES,
)
```

A user override persists across later `initialize` calls and takes precedence over DSL category defaults. Disabling a category also cancels its currently posted notifications; scheduling is updated based on enabled sources/categories. An app toggle cannot bypass Android permission or channel settings.

`FreetimeNotifications.stop(context)` clears the stored configuration and cancels periodic and queued one-time work. It invalidates active checks and prevents them from posting or committing stale state without waiting behind network I/O. It **does not clear seen IDs or category overrides**. Reinitialize to configure/start polling again; it is not a reset of notification history or user preferences.

## Networking, privacy and failures

Requests and redirects must remain HTTPS, with no embedded URL credentials; at most five redirects are followed. No API key, authentication header or blog tracking token is required or configured. Public services still receive ordinary HTTP requests and the device's network IP; this is polling, not anonymous/offline operation.

GitHub's unauthenticated public API rate limit is shared by clients using the same public IP. Frequent polling or many apps behind one NAT can exhaust it. Increase the interval and inspect `NotificationCheckResult.failures`; HTTP rate limits, missing endpoints, malformed schemas and response-size limits are source failures, not proof that no new content exists. The background worker logs failures with hashed source identifiers, rather than URL-bearing source IDs, and retries within its bounded retry policy. Avoid aggressive foreground retry loops.
