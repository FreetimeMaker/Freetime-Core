# Freetime Donations

Reusable Compose donation UI built with Material 3 Expressive-compatible components.

## Dependency

Version **4.0.0** is prepared but not yet uploaded; this dependency becomes available after publication.

```kotlin
implementation("com.freetime:Donations:4.0.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Anonymous access and complete KMP metadata must be verified before release. See the [publishing requirements](../README.md#publishing); the historical AAR-only public uploader is not suitable for KMP.

The module uses `:Core`, `:Browser`, and the Material 3 implementation exported by `:Design`.

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

## Android and desktop KMP

All cards and the QR presentation shell live in `commonMain`. QR encoding remains
host-supplied, as before. Shared Compose code obtains real platform actions with:

```kotlin
val launcher = rememberDonationLauncher()
// From event callbacks; handle failures in the returned Result:
launcher.copyWallet(wallet)
launcher.openWallet(wallet)
```

Desktop copies through the AWT system clipboard and opens wallet URIs with
`java.awt.Desktop`. A registered wallet URI handler is required; headless systems,
unavailable clipboard access, or unsupported desktop integrations return failures.
Desktop also supports `FreetimeDonationActions.copyWallet(wallet)` and
`FreetimeDonationActions.openWallet(wallet)` outside composition.
Android Context overloads and the Android `walletUri(wallet): Uri` API remain.
Context-free object actions return descriptive failures on Android; use the shared
launcher there. `walletUriString(wallet)` is the platform-neutral URI API.
