# Freetime Core

Shared, open-source Android libraries for Freetime Maker apps. Freetime Core keeps common application infrastructure reusable while every consuming app remains independently installable and usable.

Current release line: **1.11.x**

## Modules

| Artifact | Purpose |
| --- | --- |
| `Core` | Common models, results and lightweight Android utilities |
| `Design` | Material 3 Expressive / Material You theme helpers and reusable Liquid Glass |
| `Browser` | External/in-app URL routing |
| `Donations` | Reusable donation models and Material 3 Compose UI |

## Dependency

Add the repository used by your release distribution, then include only the modules your app needs:

```kotlin
dependencies {
    implementation("com.github.FreetimeMaker.Freetime-Core:Core:1.11.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.11.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Browser:1.11.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Donations:1.11.0")
}
```

## Design

The Design module no longer ships a parallel Freetime-prefixed component system. Use Material 3 Expressive directly.

```kotlin
AppTheme(
    liquidGlassEnabled = true,
) {
    LiquidGlassRoot {
        AppContent()
    }
}
```

`AppTheme` installs `MaterialExpressiveTheme` with expressive motion, enables Material You dynamic colors on Android 12+, defaults to light from 07:00 and dark from 19:00, and provides one global Liquid Glass flag.

Liquid Glass is exposed as reusable effect primitives:

- `Modifier.liquidGlass()`
- `Modifier.liquidGlassCapsule()`
- `Modifier.liquidGlassCircle()`
- `LiquidGlassContainer`
- `LiquidGlassIconButton`

The glass renderer follows the same shared-setting architecture and core optical recipe as SimpMusic. Turning Liquid Glass off globally automatically changes every glass surface to a Material 3 fallback without requiring per-screen branching.

## Browser

```kotlin
FreetimeBrowser.openExternal(context, "https://example.org")
```

For app-owned WebView or custom-tab behavior, use `BrowserMode.IN_APP` and provide an `openInApp` callback.

## Donations

```kotlin
val targets = listOf(
    DonationTarget.Link("OpenCollective", "https://opencollective.com/example"),
    DonationTarget.Wallet("Bitcoin", "BTC", "wallet-address"),
)
```

Apps retain control over link handling and wallet behavior.

## Android versions

SDK and dependency versions are centralized in `gradle/libs.versions.toml`. The current minimum SDK is 24 and compile SDK is 37.

## Versioning and releases

The library version is defined once in `gradle/libs.versions.toml`:

```toml
[versions]
freetime = "1.11.0"
```

Freetime Core follows semantic versioning. Removing the old Freetime-prefixed Design API is a breaking API change and should be released on a major version before consumers migrate.

See [CHANGELOG.md](CHANGELOG.md) for the project history.

## Principles

1. Consuming apps continue to work independently.
2. No mandatory Freetime account or Luma Store dependency.
3. Dependencies remain open-source and F-Droid-friendly.
4. Shared infrastructure uses interfaces/callbacks instead of hard-coded backends.
5. Material 3 Expressive is the UI foundation; Liquid Glass remains a focused reusable effect layer rather than a second component framework.

## License

Freetime Core is licensed under the GNU General Public License v3.0 (GPL-3.0).
