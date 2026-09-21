# Freetime Donations

Reusable Compose donation UI built on Freetime Design.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Donations:1.7.0")
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

The host app owns the actions, so it can respect its browser preference and decide whether a wallet click copies, displays or otherwise handles the address.

The screen uses Freetime Design and Liquid Glass for its UI. Readable light/dark colors now come from Freetime's own palette and typography rather than Material 3 theme colors.
