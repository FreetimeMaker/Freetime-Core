# Freetime Core

Shared, open-source Android libraries for Freetime Maker apps. Freetime Core keeps common application infrastructure reusable while every consuming app remains independently installable and usable.

Current release line: **1.8.x**

## Modules

| Artifact | Purpose |
| --- | --- |
| `Core` | Common models, results and lightweight Android utilities |
| `Design` | Standalone Freetime UI system with Liquid Glass, theme tokens and reusable Compose controls |
| `Browser` | External/in-app URL routing |
| `Donations` | Reusable donation models and Compose UI based on Freetime Design |

## Dependency

Add the repository used by your release distribution, then include only the modules your app needs:

```kotlin
dependencies {
    implementation("com.github.FreetimeMaker.Freetime-Core:Core:1.8.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.8.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Browser:1.8.0")
    implementation("com.github.FreetimeMaker.Freetime-Core:Donations:1.8.0")
}
```

## Freetime Design

`Design` is its own Compose design system. It does not use Material 3 as its UI foundation. It is built from Compose UI/Foundation, Freetime theme tokens and Kyant Backdrop/Shapes.

```kotlin
FreetimeTheme {
    FreetimeGlassRoot {
        AppContent()
    }
}
```

The public design API includes Freetime palette, typography, shapes, spacing, sizing, motion and glass tokens plus reusable controls such as:

- `FreetimeButton`, `FreetimeIconButton`, `FreetimeCard`
- `FreetimeTopBar`, `FreetimeBottomBar`, `FreetimeAdaptiveBottomBar`
- `FreetimeTextField`, `FreetimeSwitch`, `FreetimeSlider`
- `FreetimeChip`, `FreetimeDialog`, `FreetimeSnackbar`, `FreetimeProgressIndicator`
- `FreetimeGlassPanel`, `FreetimeGlassAction` and Liquid Glass modifiers

The glass engine uses Kyant Backdrop where supported and a translucent Freetime fallback otherwise. Its backdrop is separated from foreground glass content to avoid rendering feedback loops.

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
freetime = "1.8.0"
```

Freetime Core follows semantic versioning: patch releases fix compatible behavior, minor releases add compatible public functionality, and major releases are reserved for breaking public API changes.

See [CHANGELOG.md](CHANGELOG.md) for the project history.

## Principles

1. Consuming apps continue to work independently.
2. No mandatory Freetime account or Luma Store dependency.
3. Dependencies remain open-source and F-Droid-friendly.
4. Shared infrastructure uses interfaces/callbacks instead of hard-coded backends.
5. Freetime Design owns its palette, typography, shapes, motion and components rather than depending on Material 3 UI components.

## License

Freetime Core is licensed under the GNU General Public License v3.0 (GPL-3.0).
