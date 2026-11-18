# Freetime Browser

Common URL routing for Freetime Android and desktop JVM apps without forcing a particular navigation stack or browser implementation.

## Dependency

Version **4.0.1** is staged locally; this dependency becomes available once the release is published and anonymously verified.

```kotlin
implementation("com.freetime:Browser:4.0.1")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Anonymous access and complete KMP metadata must be verified before release. See the [publishing requirements](../README.md#publishing); the historical AAR-only public uploader is not suitable for KMP.

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

Browser uses Material 3 exported by `:Design`.

## Android and desktop KMP

Shared models, routing, and Compose chrome live in `commonMain`. Android retains
`openExternal(context, url)` and `open(context, url, options, openInApp)` and the
embedded WebView (including opt-in JavaScript). Desktop uses `java.awt.Desktop`
to launch the system browser; no embedded desktop HTML/JavaScript engine is bundled.
`FreetimeBrowserScreen` explicitly offers an “Open in browser” action on desktop,
validates the URL, and displays launch failures instead of silently doing nothing.
The screen action is an explicit external launch, not an automatic routing fallback.
`FreetimeBrowser.open` still respects `allowExternalFallback` for host-driven IN_APP routing.

For shared Compose callbacks on either platform:

```kotlin
val launcher = rememberBrowserLauncher()
// Inside an event callback; inspect the Result to present errors:
launcher.openExternal("https://example.org")
```

Non-Compose desktop code can call `FreetimeBrowser.openExternal(url)` directly.
Android requires a context-bound launcher or the preserved Context overload;
context-free object calls return a descriptive failure rather than retaining a global Context.
Host allowlists require exact, case-insensitive host matches (not suffix matches).
