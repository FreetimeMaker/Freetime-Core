# Freetime Core

Reusable Android building blocks for Freetime Maker apps. The SDK is modular, open-source friendly and does **not** require Luma Store, an account, or a proprietary runtime.

## Modules

- **Core** — common models, results and lightweight utilities.
- **Design** — Material You theme plus reusable Liquid Glass Compose components.
- **Updater** — source-agnostic update models and update checking. Apps decide where version data comes from.
- **Browser** — consistent external/in-app URL routing without forcing WebView on every app.
- **Donations** — reusable donation models and a Liquid Glass Compose donation screen for links and wallet addresses.

## Local Gradle usage

When this repository is included as a composite build or modules are copied into a workspace:

```kotlin
dependencies {
    implementation(project(":Core"))
    implementation(project(":Design"))
    implementation(project(":Updater"))
    implementation(project(":Browser"))
    implementation(project(":Donations"))
}
```

Only include modules the app actually needs.

## Design

```kotlin
FreetimeTheme {
    FreetimeGlassCard {
        FreetimeGlassButton("Continue", onClick = ::continueFlow)
    }
}
```

The theme follows system dark mode by default and supports Android dynamic color. Components use Material color roles, so foreground text automatically remains readable in light and dark themes.

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

The updater deliberately does not install APKs or require Luma Store.

## Browser

```kotlin
FreetimeBrowser.openExternal(context, "https://example.org")
```

For an app-owned WebView/custom tab flow, choose `BrowserMode.IN_APP` and provide an `openInApp` callback.

## Donations

```kotlin
val targets = listOf(
    DonationTarget.Link("OpenCollective", "https://opencollective.com/example"),
    DonationTarget.Wallet("Bitcoin", "BTC", "wallet-address")
)
```

Apps control link handling and wallet-copy behavior themselves.

## Principles

1. Every consuming app must continue to work independently.
2. No mandatory account or store dependency.
3. Keep dependencies small and F-Droid-friendly.
4. Prefer interfaces/callbacks over hard-coded Freetime backend dependencies.
5. Shared UI uses Material color roles for light/dark accessibility.

## License

Add the repository license before publishing binary artifacts to a Maven repository.
