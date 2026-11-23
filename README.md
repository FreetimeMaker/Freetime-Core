# Freetime Core

Reusable, open-source Kotlin Multiplatform libraries for Android and desktop for Freetime Maker apps.

**Only supported version: v4.0.1**

Versions below **v4.0.1** are available only through [JitPack](https://jitpack.io/) and are no longer supported.

Freetime Core keeps shared Android and desktop functionality in small modules that apps can adopt independently. Version **4.0.1** is the breaking KMP successor to Android-only **3.2.0**, not a new 1.x release. It keeps **Material 3 Expressive + Material You** as the default UI foundation. Default glass remains navigation-only; imported full-glass templates are separate, explicit opt-in surfaces.

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
    implementation("com.freetime:Core:4.0.1")
    implementation("com.freetime:Design:4.0.1")
    implementation("com.freetime:Browser:4.0.1")
    implementation("com.freetime:Donations:4.0.1")
    implementation("com.freetime:FreetimeWarn:4.0.1")
    implementation("com.freetime:Notifications:4.0.1")
}
```

The attempted **4.0.0** upload on this repository is incomplete: only the POMs and root JARs are anonymously retrievable, so **4.0.0 must not be consumed**. It has no `.module` metadata, no sources, no AAR and no desktop JAR, and no `v4.0.0` tag exists. New builds are staged locally only; remote publishing is disabled.

### Migrating from Android-only artifacts

Follow [MIGRATION-4.0.md](MIGRATION-4.0.md) for API and platform differences.

### Dependency changes

Replace historical JitPack dependencies with `com.freetime:<Module>:4.0.1` after publication. Existing `com.freetime` consumers keep the same case-sensitive root artifact IDs as 3.2.0 and update the version; no module or root artifact rename is required. The former generated target coordinates `Core-android` and `Core-desktop` are gone: the platform artifacts now ride the root coordinate as the `-android` and `-desktop` classifiers, so consumers never reference them directly. Keep Gradle Module Metadata enabled so Gradle can resolve the appropriate variant and dependencies.

## Project layout and verification

Root settings map `:Core`, `:Design`, `:Browser`, `:Donations`, `:FreetimeWarn`, `:Notifications` and `:Sample` to their existing unsuffixed directories. The Android sample application is `:Sample-android` at `Sample/androidApp`; `:Sample` contains shared UI and the desktop entry point.

```sh
./gradlew desktopTest :Sample:desktopJar :Sample-android:assembleDebug :Design:checkTemplates
```

Shared APIs target Android and desktop; Android system integrations such as WorkManager, notification permission and Material You remain platform-specific.

### 4.0.1 validation milestone

`stagePublications` stages **six unified publications totalling 48 files**: each library contributes `POM`, `.module`, main JAR, sources JAR, `-android.aar`, `-android-sources.jar`, `-desktop.jar` and `-desktop-sources.jar` under one coordinate. Every `.module` component reports group `com.freetime`, version `4.0.1`, contains no `available-at` redirect and no `-kmp` reference. `verify_gemfury_release.py 4.0.1 --staging build/public-maven --local-only` validates all 48 files, and a scratch Gradle consumer resolving `com.freetime:Core:4.0.1` from that staged repository receives `Core-4.0.1-desktop.jar` plus `kotlin-stdlib`.

The complete CI command set (`:Design:checkTemplates`, five `testAndroidHostTest`, `:Sample-android:assembleDebug`/`assembleRelease` and `stagePublications`) and the Python publishing test suite pass. Desktop GUI smoke tests and device execution have not been performed; the Core device-test APK has not been compiled. These results do not establish GUI/device runtime behavior.

## Publishing

Each library is staged under **one Maven coordinate**. Gradle's native KMP publications (root, Android, desktop) are first staged privately to `build/native-maven`; `.github/scripts/consolidate_kmp_publications.py` then validates them — SemVer, variant roles, sizes and every checksum — and inlines the platform artifacts into `build/unified-inputs` as classifier artifacts of the root coordinate, rewriting cross-module dependencies from `Core-android`/`Core-desktop` to `Core`. The `unified` Gradle publication attaches those validated files plus a POM that keeps the native dependency block and the `published-with-gradle-metadata` marker. Invalid input aborts before any output is written.

Stage publications locally without credentials (safe; no remote upload):

```sh
./gradlew stagePublications -PreleaseVersion=4.0.1
```

`stagePublications` writes all six unified libraries to `build/public-maven`. CI validates that local repository with `verify_gemfury_release.py --local-only`. After successful tests and local validation on `master`, CI creates the version tag if it does not already exist. CI does not upload Maven packages; [FreetimeMaker/Repoitories](https://github.com/FreetimeMaker/Repoitories) builds from tags and manages the public Maven repository separately.

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

The project currently pins `androidx.compose.material3:material3:1.5.0-rc01` for the current Material 3 Expressive APIs.

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

## Versioning

The intended 4.0.1 shared release version lives in `gradle/libs.versions.toml`:

```toml
[versions]
freetime = "4.0.1"
```

Both Android and desktop `FreetimeCore.SDK_VERSION` actuals are **4.0.1**. The Android sample uses version code **6** and derives its version name from `project.version`. See [CHANGELOG.md](CHANGELOG.md) for release history.

## Project principles

1. Apps remain independently installable and usable.
2. No mandatory Freetime account, Luma Store dependency or hard-coded backend.
3. Dependencies should remain open-source and F-Droid-friendly.
4. Shared functionality should stay modular and host-controlled.
5. Material 3 Expressive is the default component system; default navigation glass and explicitly opted-in full-glass templates remain separate.
6. Breaking API changes use semantic-versioning major releases.

## License

Freetime Core is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
