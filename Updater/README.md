# Freetime Updater

Source-selectable update checking without forcing an app store or APK installer.

## Dependency

```kotlin
implementation("me.free-time:freetime-updater:<version>")
```

## Sources

Built-in source categories are:

- `LUMA_STORE`
- `F_DROID`
- `GITHUB`
- `OTHER`

A source supplies the latest version for a package:

```kotlin
val githubSource = UpdateSource { packageName ->
    // Query your GitHub release implementation.
    AppVersion(
        versionName = "2.0.0",
        versionCode = 20,
        downloadUrl = "...",
        changelog = "...",
    )
}
```

Register all sources supported by the app:

```kotlin
val updater = FreetimeUpdater(
    listOf(
        RegisteredUpdateSource(UpdateSourceType.LUMA_STORE, lumaSource),
        RegisteredUpdateSource(UpdateSourceType.F_DROID, fdroidSource),
        RegisteredUpdateSource(UpdateSourceType.GITHUB, githubSource),
    )
)
```

## Selecting sources

All registered sources at once:

```kotlin
updater.check(packageName, currentVersion)
```

Only one:

```kotlin
updater.check(
    packageName,
    currentVersion,
    UpdateSourceSelection.only(UpdateSourceType.F_DROID),
)
```

Several:

```kotlin
updater.check(
    packageName,
    currentVersion,
    UpdateSourceSelection.of(
        UpdateSourceType.LUMA_STORE,
        UpdateSourceType.GITHUB,
    ),
)
```

When several sources return versions, the candidate with the highest `versionCode` becomes `UpdateInfo.latest`. `UpdateInfo.source` identifies its source and `candidates` contains all returned candidates.

The module checks versions only; downloading/installing remains under the host app's control.
