# Freetime Browser

Common URL routing for Freetime Android apps without forcing a particular navigation stack or browser implementation.

## Dependency

Version **3.2.0** is prepared but not yet uploaded; this dependency becomes available after publication.

```kotlin
implementation("com.freetime:Browser:3.2.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Public packages require no consumer credentials. The [public publishing flow](../README.md#publishing) requests public visibility for new packages without an initial dashboard step; existing private version conflicts are not automatically made public.

## External browser

```kotlin
FreetimeBrowser.openExternal(context, "https://example.org")
```

This starts an Android `ACTION_VIEW` intent.

## In-app browser

The host app can own its WebView or Custom Tabs implementation:

```kotlin
FreetimeBrowser.open(
    context = context,
    url = url,
    options = BrowserOptions(mode = BrowserMode.IN_APP),
    openInApp = { requestedUrl ->
        openMyWebView(requestedUrl)
        true
    },
)
```

Return `true` when the app handled the URL. Returning `false` allows the external-browser fallback when `allowExternalFallback` is enabled.

To prevent fallback:

```kotlin
BrowserOptions(
    mode = BrowserMode.IN_APP,
    allowExternalFallback = false,
)
```

The module also provides `FreetimeBrowserScreen` for apps that want the reusable Compose browser UI.

Browser remains independent from the Design module; its UI uses Material 3 directly.
