# FreetimeWarn

A Kotlin/Jetpack Compose acknowledgement and warning library inspired by FreeDroidWarn.

The shared UI uses Material 3 exported by `:Design`.

## Dependency

Version **4.0.1** is staged locally; this dependency becomes available once the release is published and anonymously verified.

```kotlin
implementation("com.freetime:FreetimeWarn:4.0.1")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Anonymous access and complete KMP metadata must be verified before release. See the [publishing requirements](../README.md#publishing); the historical AAR-only public uploader is not suitable for KMP.

## Required app name

The application name is mandatory:

```kotlin
val warning = rememberFreetimeWarnState(
    context = context,
    appName = "GeoWeather",
    versionCode = BuildConfig.VERSION_CODE.toLong(),
)

FreetimeWarn(
    state = warning,
    onLearnMore = {
        // Open the current information page.
    },
)
```

A missing `appName` does not compile and a blank runtime value is rejected.

## Frequency

- `ONCE` — persists acknowledgement for this warning ID/app.
- `ONCE_PER_VERSION` — default; shows once per application version code.
- `ALWAYS` — persistence is disabled and the host controls visibility.

## Custom text

```kotlin
val content = FreetimeWarnContent(
    title = "Distribution notice",
    message = { appName -> "Current information affecting $appName." },
)

FreetimeWarn(
    state = warning,
    content = content,
)
```

Keep policy-specific wording current rather than relying indefinitely on text embedded in an old library release.

## Android and desktop KMP

The state, frequency handling, and all dialog UI live in `commonMain`. Shared calls
no longer need a Context:

```kotlin
val warning = rememberFreetimeWarnState(
    appName = "My app",
    versionCode = 42L,
)
FreetimeWarn(state = warning)
```

The Android Context overload remains available and uses the original
`freetime_warn` SharedPreferences file and acknowledgement keys. Desktop uses
`java.util.prefs.Preferences` under `com/freetime/warn`, hashing keys to accommodate
Java Preferences' key-length limit. Both persist ONCE and ONCE_PER_VERSION
acknowledgements across restarts; dismissing without acknowledgement does not
persist. Desktop writes are flushed and Android writes are committed before the
warning closes. Storage failures propagate rather than falsely acknowledging.
Hosts may inject `WarningPreferences` for custom storage or tests.

The default content deliberately remains the existing Android-distribution notice,
including on desktop. Supply `FreetimeWarnContent` for platform-specific messaging.
