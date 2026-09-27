# Freetime Core

Reusable, open-source Android libraries for Freetime Maker apps.

**Current version: 3.0.0**

Freetime Core keeps shared Android functionality in small modules that apps can adopt independently. Version 3.x keeps **Material 3 Expressive + Material You** as the UI foundation and restricts Liquid Glass to the dedicated floating bottom navigation API.

## Highlights

- **Material 3 Expressive** as the UI foundation
- **Material You** dynamic colors on Android 12+
- automatic light mode from **07:00** and dark mode from **19:00**
- one **Liquid Glass** switch dedicated to the floating bottom navigation
- reusable browser, donation and warning modules
- no mandatory Freetime account, Luma Store installation or proprietary backend
- F-Droid-friendly, open-source dependencies
- minimum Android SDK **24**, compile SDK **37**

## Modules

| Module | Purpose |
| --- | --- |
| `Core` | Shared models, results, SDK metadata and lightweight Android helpers |
| `Design` | Material 3 Expressive / Material You theme helpers plus floating bottom-navigation Liquid Glass |
| `Browser` | External and in-app URL routing plus reusable browser UI |
| `Donations` | Donation targets, wallet helpers and Compose donation UI |
| `FreetimeWarn` | Reusable acknowledgement/warning flow for Compose apps |
| `Sample` | Example application demonstrating the libraries together |

## Installation

Freetime Core is published as a JitPack multi-module project.

Add JitPack:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add only the modules your app needs:

```kotlin
dependencies {
    implementation("com.github.FreetimeMaker.Freetime-Core:Core:3.0.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Design:3.0.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Browser:3.0.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Donations:3.0.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:FreetimeWarn:3.0.0")
}
```

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

The project currently pins `androidx.compose.material3:material3:1.5.0-alpha29` for the current Material 3 Expressive APIs.

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
freetime = "3.0.0"
```

The root Gradle build applies that value to all published library modules. `FreetimeCore.SDK_VERSION` uses the same release number, and the GitHub Actions release workflow creates `v3.0.0` when the version does not already have a tag.

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
