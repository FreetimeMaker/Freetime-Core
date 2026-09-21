# Freetime Browser

Common URL routing for Freetime Android apps without forcing a particular WebView implementation or UI design system.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Browser:1.9.0")
```

## External browser

```kotlin
FreetimeBrowser.openExternal(context, "https://example.org")
```

This starts an Android `ACTION_VIEW` intent.

## In-app browser

The host app owns its WebView or Custom Tabs implementation:

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

The Browser module remains independent from Freetime Design and Material 3.
