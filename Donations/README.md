# Freetime Donations

Reusable Compose donation UI built with Material 3 Expressive-compatible components.

## Dependency

Version **3.2.0** is prepared but not yet uploaded; this dependency becomes available after publication.

```kotlin
implementation("com.freetime:Donations:3.2.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Public packages require no consumer credentials. The [public publishing flow](../README.md#publishing) requests public visibility for new packages without an initial dashboard step; existing private version conflicts are not automatically made public.

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
