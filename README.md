# Freetime Core

Shared Android libraries for Freetime Maker apps. Freetime Core keeps common design, browser and donation behavior reusable while every app remains independently installable and usable.

## Modules

| Artifact | Purpose |
| --- | --- |
| `freetime-core` | Common models, results and lightweight utilities |
| `freetime-design` | Material You and the current GeoWeather-style Liquid Glass system |
| `freetime-browser` | External/in-app URL routing |
| `freetime-donations` | Reusable donation models and Compose UI |

## JitPack

Releases are distributed through JitPack. Add the JitPack repository:

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}
```

Then add only the modules your app needs:

```kotlin
dependencies {
    implementation("com.github.FreetimeMaker.Freetime-Core:Core:<version>")
    implementation("com.github.FreetimeMaker.Freetime-Core:Design:<version>")
    implementation("com.github.FreetimeMaker.Freetime-Core:Browser:<version>")
    implementation("com.github.FreetimeMaker.Freetime-Core:Donations:<version>")
}
```

## Liquid Glass

The Design module follows GeoWeather's current Liquid Glass implementation. On Android 13+ it uses Kyant Backdrop and Shapes for backdrop sampling, vibrancy, blur, lens distortion, capsules and interactive spring scaling. Older Android versions receive a Material color-aware fallback.

Wrap the app content once so glass surfaces can sample a separate backdrop layer:

```kotlin
FreetimeTheme {
    FreetimeGlassRoot {
        // App UI
    }
}
```

Then use the reusable components or modifiers:

```kotlin
FreetimeGlassCard {
    FreetimeGlassButton("Continue", onClick = ::continueFlow)
}

Modifier.freetimeGlass()
Modifier.freetimeGlassCapsule()
```

The backdrop source is intentionally separated from the glass content to avoid RuntimeShader feedback loops seen on some Android GPU drivers.

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

Apps keep control over link handling and wallet-copy behavior.

## Android versions

`compileSdk` and `minSdk` are defined centrally in `gradle/libs.versions.toml`. The current minimum SDK is 24 and compile SDK is 37.

## Publishing

The library version is defined once in `gradle/libs.versions.toml`:

```toml
[versions]
freetime = "1.3.0"
```

Changing this value on `master` runs the build and verifies all Maven publications locally. The release job then creates the matching Git tag, for example `v1.3.0`. JitPack builds that tag using `jitpack.yml` and publishes the multi-module artifacts. No Maven Central credentials or signing secrets are required.

## Principles

1. Consuming apps continue to work independently.
2. No mandatory Freetime account or Luma Store dependency.
3. Dependencies remain open-source and F-Droid-friendly.
4. Shared infrastructure uses interfaces/callbacks instead of hard-coded backends.
5. Design follows Material color roles for readable light and dark themes.

## License

Freetime Core is licensed under the GNU General Public License v3.0 (GPL-3.0).
