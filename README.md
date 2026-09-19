# Freetime Core

Shared Android libraries for Freetime Maker apps. Freetime Core keeps common design, update, browser and donation behavior reusable while every app remains independently installable and usable.

## Modules

| Artifact | Purpose |
| --- | --- |
| `freetime-core` | Common models, results and lightweight utilities |
| `freetime-design` | Material You and the current GeoWeather-style Liquid Glass system |
| `freetime-updater` | Source-agnostic update checking |
| `freetime-browser` | External/in-app URL routing |
| `freetime-donations` | Reusable donation models and Compose UI |

## Maven Central

Releases use the verified `me.free-time` namespace:

```kotlin
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("me.free-time:freetime-core:<version>")
    implementation("me.free-time:freetime-design:<version>")
    implementation("me.free-time:freetime-updater:<version>")
    implementation("me.free-time:freetime-browser:<version>")
    implementation("me.free-time:freetime-donations:<version>")
}
```

Only add the modules an app needs.

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

## Updater

```kotlin
val updater = FreetimeUpdater(
    UpdateSource { packageName ->
        // Fetch from All API, Luma Store, GitHub, F-Droid, etc.
        AppVersion("2.0.0", 20, downloadUrl = "...")
    }
)

val result = updater.check(
    packageName = context.packageName,
    current = AppVersion(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toLong())
)
```

The updater does not require Luma Store and does not install APKs itself.

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

The **Maven Central** GitHub Actions workflow creates release AARs, sources, Javadocs, signatures and checksums, then uploads one bundle through the Central Publisher API using `USER_MANAGED`. A successful workflow stages and validates the deployment; it does **not** automatically publish it.

Required repository secrets:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY`
- `SIGNING_PASSWORD`

## Principles

1. Consuming apps continue to work independently.
2. No mandatory Freetime account or Luma Store dependency.
3. Dependencies remain open-source and F-Droid-friendly.
4. Shared infrastructure uses interfaces/callbacks instead of hard-coded backends.
5. Design follows Material color roles for readable light and dark themes.

## License

Freetime Core is licensed under the GNU General Public License v3.0 (GPL-3.0).
