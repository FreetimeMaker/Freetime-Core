# Freetime Donations

Reusable Compose donation UI built with Material 3 and the Design module's optional Liquid Glass effect.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Donations:2.0.0")
```

The module uses Core, Design and Browser.

## Donation targets

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

## Screen

```kotlin
FreetimeDonationScreen(
    targets = targets,
    onLinkClick = { target ->
        FreetimeBrowser.openExternal(context, target.url)
    },
    onWalletClick = { target ->
        copyToClipboard(target.address)
    },
)
```

The host app owns all actions. The screen uses Material 3 components; cards and action surfaces follow the global `LocalLiquidGlassEnabled` value when hosted under `AppTheme` or `ProvideLiquidGlass`.
