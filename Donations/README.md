# Freetime Donations

Reusable Compose donation UI built on Freetime Design.

## Dependency

```kotlin
implementation("me.free-time:freetime-donations:<version>")
```

The module also uses the Core, Design and Browser modules.

## Donation targets

Two target types are available:

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

The SDK deliberately leaves actions to the host app. This allows an app to respect its own browser preference (for example in-app vs external) and decide whether a wallet click copies, displays or otherwise handles the address.

The screen uses Freetime Liquid Glass cards/buttons and Material theme colors so text remains readable in light and dark mode.
