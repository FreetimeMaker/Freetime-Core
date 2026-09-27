# Freetime Donations

Reusable Compose donation UI built with Material 3 Expressive-compatible components.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Donations:3.0.0")
```

The module uses Core and Browser and no longer depends on Design.

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

The host app owns all actions. Donation cards and buttons use normal Material 3 styling. Liquid Glass is reserved for the app's floating bottom navigation.
