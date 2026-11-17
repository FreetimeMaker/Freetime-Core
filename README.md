# Freetime Core

Reusable, open-source Android libraries for Freetime Maker apps.

**Current version: 3.2.0**

Release 3.2.0 is prepared but **not yet uploaded**. The Maven dependencies below become available after publication.

Freetime Core keeps shared Android functionality in small modules that apps can adopt independently. Version 3.x keeps **Material 3 Expressive + Material You** as the UI foundation and restricts Liquid Glass to the dedicated floating bottom navigation API.

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

| Module          | Purpose                                                                                                                                       |
| --------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `Core`          | Shared models, results, SDK metadata and lightweight Android helpers                                                                          |
| `Design`        | Material 3 Expressive / Material You theme helpers plus floating bottom-navigation Liquid Glass                                               |
| `Browser`       | External and in-app URL routing plus reusable browser UI                                                                                      |
| `Donations`     | Donation targets, wallet helpers and Compose donation UI                                                                                      |
| `FreetimeWarn`  | Reusable acknowledgement/warning flow for Compose apps                                                                                        |
| `Notifications` | Local GitHub/F-Droid update detection, GitHub release notes and developer blog messages ([guide](Notifications/README.md) |
| `Sample`        | Example application demonstrating the libraries together                                                                                      |

## Installation

Freetime Core libraries are published to Gemfury (fury.io) under the fixed Maven group `com.freetime`.

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
    implementation("com.freetime:Core:3.2.0")
    implementation("com.freetime:Design:3.2.0")
    implementation("com.freetime:Browser:3.2.0")
    implementation("com.freetime:Donations:3.2.0")
    implementation("com.freetime:FreetimeWarn:3.2.0")
    implementation("com.freetime:Notifications:3.2.0")
}
```

Public consumption requires no credentials. The public upload flow requests public visibility for new packages; no initial dashboard step is required. For private packages, configure a read-only token through local Gradle properties or your CI secret store, never a publishing deploy token. Do not embed tokens or other secrets in apps or tracked files.

### Notifications: 3.2.0 integration

Notifications is introduced in **3.2.0**, pending actual publication. After publication, use `implementation("com.freetime:Notifications:3.2.0")` with the repository settings above and follow the [module integration guide](Notifications/README.md).

For local integration before publication, or when working within this repository, use the project dependency instead:

```kotlin
dependencies {
    implementation(project(":Notifications"))
}
```

For another checkout, include the local `Notifications` and `Core` projects in the consuming build first. The local project dependency is an alternative to the Maven dependency, not an additional requirement.

### Migrating from JitPack

Historical 3.1.0 migration example: replace `com.github.FreetimeMaker.Freetime-Core:<module>:3.1.0` with `com.freetime:<module>:3.1.0` and add the Gemfury repository above. Artifact IDs remain case-sensitive and unchanged: `Core`, `Design`, `Browser`, `Donations` and `FreetimeWarn`. The version remains `3.1.0`; this migration changes the repository and Maven group, not the library APIs. Remove `https://jitpack.io` only if no other dependencies need it. For the prepared 3.2.0 release, use the six `com.freetime` dependencies above after publication; 3.2.0 is not a JitPack release.

## Publishing

Public dependencies are consumed from `https://repo.free-time.me/maven/` with the stable group `com.freetime`. Uploads use the `freetimemaker` account only at the push endpoint; consumers do not need an account-specific URL.

### Credentials and commands

For local publishing, put only the deploy token in your **user** `~/.gradle/gradle.properties`, never in tracked project files:

```properties
furyToken=YOUR_DEPLOY_TOKEN
```

In CI, configure only the `FURY_TOKEN` secret. `FURY_USERNAME` / `furyUsername` is not needed for the public path. Never embed publishing tokens in apps.

From the repository root, run:

```sh
sh gradlew publishPublic
```

Publication is configured for six modules: `Core`, `Design`, `Browser`, `Donations`, `FreetimeWarn` and `Notifications`. Release 3.2.0 is prepared but has not yet been uploaded or verified remotely.

This stages the configured release AARs and their POMs through a file Maven repository in `build/public-maven`, then runs `.github/scripts/publish_public_maven.py VERSION`. The default version comes from the shared version catalog, for example `3.2.0`; an explicit `-PreleaseVersion` overrides it consistently for staging and uploads. Artifact versions are never randomized.

`sh gradlew publish` **only stages locally**; it does not upload remotely. To validate the staged archives without credentials or uploads:

```sh
sh gradlew publish
python3 .github/scripts/publish_public_maven.py 3.2.0 --dry-run
```

### Public upload format and limits

The Python publisher embeds `META-INF/maven/com.freetime/<Module>/pom.xml` and `pom.properties` in each AAR and uploads it using `POST https://push.fury.io/uploads?as=freetimemaker` with multipart fields `public=true` and `file`. The public publication exposes **only AARs and POMs**. Gradle `.module` metadata is disabled; POM metadata supplies dependency information.

Sources and Javadoc archives are still built and staged locally, but are not advertised or downloadable from the public repository. Classifier multipart uploads were refused with HTTP 409 for the same version. Native Maven PUT returned HTTP 200 without making the artifacts available, while metadata finalization conflicted; it is not the public publication path.

New packages require no manual initial dashboard visibility step. An existing private duplicate version instead causes a conflict: uploading does not automatically change an old package’s visibility. A retry skips HTTP 409 only when the anonymously accessible public AAR is identical by archive entry contents; a conflicting or private artifact fails rather than being treated as success.

### Anonymous verification and release status

CI stages and publicly uploads the release, then checks anonymous downloads of all six configured modules’ **POMs and AARs at the exact group, artifact and version**, before creating the release tag. It does not require sources, Javadoc or `.module` files. No publishing credentials are passed to the check. Run it locally after publishing:

```sh
python3 .github/scripts/verify_public_maven.py 3.2.0
```

Historically, the five `com.freetime` modules at version `3.1.0` were uploaded through this public path, and anonymous downloads of every AAR and POM were verified at the custom repository URL. No release tag is created by the local publishing command.

Existing tagged versions are not automatically republished; normally, bump the shared version for the next release to run the publication-and-tag flow. The next minor release, 3.2.0, adds Notifications as the sixth module; its public availability remains pending publication.

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

Version 3.1.0 includes classic Material 3 and Expressive templates in the Design
library under `com.freetime.design.templates.material3` and
`com.freetime.design.templates.expressive`. Each variant separates colors,
typography, shapes and theme composition, with light/dark previews and a separate
Freetime adapter that disables navigation glass. Material You is opt-in.

For custom design values, copy the desired variant into your app and adapt its
tokens. See the [template guide](Design/src/main/java/com/freetime/design/templates/README.md).
The existing root `AppTheme` behavior is unchanged.

## Liquid Glass

Liquid Glass is reserved for the floating bottom navigation. All other UI uses normal Material 3 Expressive surfaces.

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

Its layout mirrors SimpMusic's Android bar: 64dp capsule, 56dp sliding frosted selection pill, up-to-96dp tabs, 6dp inset, 12dp gap and a separate 56dp circular search button. Do not apply Liquid Glass to cards, buttons, text fields, dialogs, top bars or content surfaces. Those should keep the normal Material 3 Expressive appearance.

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

No Firebase, UnifiedPush or notification backend is required. The blog source uses `https://api.free-time.me/v2/blog/posts`, the public API used by `blog.free-time.me`; filters require all requested categories, case-insensitively, without a tracking token. WorkManager polling defaults to six hours with a 15-minute minimum, not an immediate push guarantee. A suspending `checkNow` performs a foreground HTTP check; `requestCheck` only queues best-effort work.

Configuration and bounded per-source/per-category deduplication persist in DataStore. The first successful check is a silent baseline by default. The host supplies its notification icon, localized channel options and Android 13+ permission request. See the [module guide](Notifications/README.md) for the initialization DSL, application-owned coroutine setup, version rules, persistent toggles and networking limits. Use the local project dependency above until publication is available.

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

## Migrating to 3.x

Version 3.0.0 is a breaking release for apps that used the generic Liquid Glass APIs from 2.0.0.

Remove usages of:

- `Modifier.liquidGlass()`
- `Modifier.liquidGlassCapsule()`
- `Modifier.liquidGlassCircle()`
- `LiquidGlassContainer`
- `LiquidGlassIconButton`
- the generic `LiquidGlassRoot`
- `LocalLiquidGlassEnabled` / `ProvideLiquidGlass`

Use `FloatingBottomNavigationGlassRoot` together with `FloatingBottomNavigationBar` instead. Normal cards, buttons, text fields, dialogs and top bars should use Material 3 Expressive directly.

Apps migrating from 1.x should also replace the removed Freetime-prefixed Design component wrappers with Material 3 equivalents.

## Versioning

The shared release version lives in `gradle/libs.versions.toml`:

```toml
[versions]
freetime = "3.2.0"
```

The root Gradle build applies that value to all published library modules. `FreetimeCore.SDK_VERSION` uses the same release number, and the GitHub Actions release workflow creates `v3.2.0` after publication and verification when the version does not already have a tag.

See [CHANGELOG.md](CHANGELOG.md) for release history.

## Project principles

1. Apps remain independently installable and usable.
2. No mandatory Freetime account, Luma Store dependency or hard-coded backend.
3. Dependencies should remain open-source and F-Droid-friendly.
4. Shared functionality should stay modular and host-controlled.
5. Material 3 Expressive is the component system; Liquid Glass is reserved for the floating bottom navigation.
6. Breaking API changes use semantic-versioning major releases.

## License

Freetime Core is licensed under the **GNU General Public License v3.0 (GPL-3.0)**.
