# Freetime Core

Reusable, open-source Kotlin Multiplatform libraries for Android and desktop for Freetime Maker apps.

Versions below **v4.0.1** are available only through [JitPack](https://jitpack.io/) and are no longer supported.

## This Repo is sponsored by

[![Sponsor](https://readme.cash/i/i2o7ay35ms.svg)](https://readme.cash/c/i2o7ay35ms)

## Highlights

- **Material 3 Expressive** as the UI foundation
- **Material You** dynamic colors on Android 12+
- app-owned classic Material 3 and Expressive theme templates without Liquid Glass
- automatic light mode from **07:00** and dark mode from **19:00**
- one **Liquid Glass** switch dedicated to the floating bottom navigation
- reusable browser, donation and warning modules
- local polling for app updates, GitHub changelogs and developer blog messages with the new [Notifications module](Notifications/README.md)
- no mandatory Freetime account, Luma Store installation or proprietary backend
- F-Droid-friendly, open-source dependencies
- minimum Android SDK **24**, compile SDK **37**

## Modules

| Module           | Purpose                                                                                                                    |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------- |
| `Core`           | Shared models, results, SDK metadata and lightweight Android helpers                                                       |
| `Design`         | Material 3 Expressive / Material You theme helpers plus floating bottom-navigation Liquid Glass                            |
| `Browser`        | External and in-app URL routing plus reusable browser UI                                                                   |
| `Donations`      | Donation targets, wallet helpers and Compose donation UI                                                                   |
| `FreetimeWarn`   | Reusable acknowledgement/warning flow for Compose apps                                                                     |
| `Notifications`  | Local GitHub/F-Droid update detection, GitHub release notes and developer blog messages ([guide](Notifications/README.md)) |
| `Sample`         | Shared sample UI and desktop entry point                                                                                   |
| `Sample-android` | Android sample application host in `Sample/androidApp`                                                                     |

## Android and desktop capability matrix

Only **Android and desktop JVM** are supported. There are no supported iOS or web targets. “Supported” here describes implemented APIs, not a claim that every OS has passed runtime testing.

| Capability                   | Android                                                           | Desktop JVM                                                                   |
| ---------------------------- | ----------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| Core models and preferences  | SharedPreferences; `appName(context)` helper                      | Java Preferences; no Android Context helper                                   |
| Design themes and Compose UI | Material You on Android 12+, regular schemes otherwise            | Regular light/dark schemes; no Material You                                   |
| Browser screen               | Embedded WebView, opt-in JavaScript                               | External browser action only; no embedded HTML engine                         |
| URL routing                  | Context-bound intents or remembered launcher                      | AWT `Desktop.browse`; host IN_APP callback or permitted external fallback     |
| Donations                    | Clipboard and wallet intents                                      | AWT clipboard and registered wallet URI handlers                              |
| Warning acknowledgements     | SharedPreferences                                                 | Java Preferences                                                              |
| Notifications                | WorkManager, DataStore, native channels; host requests permission | In-process polling, atomic JSON storage, AWT tray; no OS background scheduler |

Desktop browser/wallet/clipboard/tray integrations depend on a graphical session and OS support. Handle returned failures. QR encoding is supplied by the host on both platforms. Notifications uses JVM-shared Java/`org.json` code, not portable Native/JS common code.

## Installation

KMP libraries use the Maven group `com.freetime` and case-sensitive artifact IDs matching the library module names. Every library is a **single Maven coordinate** that carries the common metadata JAR, the Android AAR and the desktop JAR as classifiers; there are no separate `Core-android` or `Core-desktop` packages to consume. Add the root dependency and let Gradle select Android or desktop through Gradle Module Metadata.

Add the Maven repository in your app's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.free-time.me/maven/")
    }
}
```

Then add only the modules your app needs:

```kotlin
dependencies {
    implementation("com.freetime:Core:4.0.2")
    implementation("com.freetime:Design:4.0.2")
    implementation("com.freetime:Browser:4.0.2")
    implementation("com.freetime:Donations:4.0.2")
    implementation("com.freetime:FreetimeWarn:4.0.2")
    implementation("com.freetime:Notifications:4.0.2")
}
```

## Project layout and verification

Root settings map `:Core`, `:Design`, `:Browser`, `:Donations`, `:FreetimeWarn`, `:Notifications` and `:Sample` to their existing unsuffixed directories. The Android sample application is `:Sample-android` at `Sample/androidApp`; `:Sample` contains shared UI and the desktop entry point.

```sh
./gradlew desktopTest :Sample:desktopJar :Sample-android:assembleDebug :Design:checkTemplates
```

Shared APIs target Android and desktop; Android system integrations such as WorkManager, notification permission and Material You remain platform-specific.

## Material 3 Expressive + Material You

The Design module no longer provides a second component framework. Use AndroidX Material 3 components directly and let `AppTheme` provide the shared Expressive theme.

```kotlin
AppTheme(
    themeMode = ThemeMode.AUTO_TIME,
    floatingBottomNavigationGlassEnabled = true,
) {
    AppContent()
}
```

`AppTheme` provides:

- `MaterialExpressiveTheme`
- `MotionScheme.expressive()`
- Material You dynamic colors on Android 12+
- regular Material color schemes on older Android versions
- `SYSTEM`, `LIGHT`, `DARK`, `OLED` and `AUTO_TIME` modes
- automatic light/dark switching at 07:00 and 19:00 by default
- the global Liquid Glass state

## App-owned theme templates

Version 4.0.0 includes classic Material 3 and Expressive templates in the Design
library under `com.freetime.design.templates.material3` and
`com.freetime.design.templates.expressive`. Each variant separates colors,
typography, shapes and theme composition, with light/dark previews and a separate
Freetime adapter that disables navigation glass. Material You is opt-in.

For custom design values, copy the desired variant into your app and adapt its
tokens. See the [template guide](Design/src/commonMain/kotlin/com/freetime/design/templates/README.md).
The existing root `AppTheme` behavior is unchanged.

## Liquid Glass

In the default `AppTheme` path, Liquid Glass is reserved for the floating bottom navigation. Optional imported `templates.material3glass`, `templates.expressiveglass` and `templates.fullglass` APIs are a separate explicit choice; see [Design](Design/README.md).

Wrap the app only to provide the backdrop used by the floating bottom navigation:

```kotlin
FloatingBottomNavigationGlassRoot(
    source = {
        AppBackground()
    },
) {
    AppContent()
}
```

Use the dedicated floating bar:

```kotlin
FloatingBottomNavigationGlassRoot(
    source = { AppBackground() },
) {
    FloatingBottomNavigationBar(
        items = tabs,
        selectedItemIndex = selectedIndex,
        onItemSelected = { selectedIndex = it },
        searchItem = searchItem,
        onSearchSelected = ::openSearch,
    )
}
```

Its layout mirrors SimpMusic's Android bar: 64dp capsule, 56dp sliding frosted selection pill, up-to-96dp tabs, 6dp inset, 12dp gap and a separate 56dp circular search button. For the default navigation-only design, keep cards, buttons, text fields, dialogs, top bars and content on Material 3 Expressive. Optional full-glass templates do not change that default or automatically restyle existing components.

The navigation-only setting is provided through `LocalFloatingBottomNavigationGlassEnabled`. When disabled, the floating bottom navigation automatically uses the Material fallback.

Apps that already own their Material theme can provide only the glass setting:

```kotlin
ProvideFloatingBottomNavigationGlass(enabled = settings.floatingBottomNavigationGlassEnabled) {
    AppContent()
}
```

## Browser

Open a URL externally:

```kotlin
FreetimeBrowser.openExternal(
    context,
    "https://example.org",
)
```

For app-owned browsing, use `BrowserMode.IN_APP` with the routing options or the reusable browser screen.

## Notifications

The new [`:Notifications` module](Notifications/README.md) (`com.freetime.notifications`, depending on `:Core`) polls public sources locally for GitHub/F-Droid versions newer than the installed app, GitHub release-note changelogs and developer blog messages. Configure only update repositories/packages for the same installed app. F-Droid metadata does not produce changelog alerts or download/install APKs.

No Firebase, UnifiedPush or notification backend is required. The blog source uses `https://api.free-time.me/v2/blog/posts`, the public API used by `blog.free-time.me`; filters require all requested categories, case-insensitively, without a tracking token. On Android, WorkManager polling defaults to six hours with a 15-minute minimum, not an immediate push guarantee. A suspending `checkNow` performs a foreground HTTP check; `requestCheck` only queues best-effort work.

On Android, configuration and bounded per-source/per-category deduplication persist in DataStore. Desktop instead uses atomic JSON storage and in-process coroutine polling; it needs explicit startup/resume and cannot poll after exit. The first successful check is a silent baseline by default. The host supplies its notification icon, localized channel options and Android 13+ permission request. See the [module guide](Notifications/README.md) for the initialization DSL, application-owned coroutine setup, version rules, persistent toggles and networking limits. Use the local project dependency above until publication is available.

## Donations

Donation targets remain host-controlled:

```kotlin
val targets = listOf(
    DonationTarget.Link(
        label = "OpenCollective",
        url = "https://opencollective.com/example",
    ),
    DonationTarget.Wallet(
        label = "Bitcoin",
        currency = "BTC",
        address = "wallet-address",
    ),
)
```

The host decides how links, wallets, clipboard actions and QR payloads are handled.

## FreetimeWarn

Create a warning state with a required application name:

```kotlin
val warning = rememberFreetimeWarnState(
    context = context,
    appName = "GeoWeather",
    versionCode = BuildConfig.VERSION_CODE.toLong(),
)

FreetimeWarn(
    state = warning,
    onLearnMore = {
        // Open your current information page.
    },
)
```

Supported frequencies are `ONCE`, `ONCE_PER_VERSION` and `ALWAYS`.

## Project principles

1. Apps remain independently installable and usable.
2. No mandatory Freetime account, Luma Store dependency or hard-coded backend.
3. Dependencies should remain open-source and F-Droid-friendly.
4. Shared functionality should stay modular and host-controlled.
5. Material 3 Expressive is the default component system; default navigation glass and explicitly opted-in full-glass templates remain separate.
6. Breaking API changes use semantic-versioning major releases.

## License

Freetime Core is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
