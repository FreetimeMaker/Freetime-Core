# FreetimeWarn

A Kotlin/Jetpack Compose warning library inspired by FreeDroidWarn, using Freetime Design instead of Android/Material dialogs.

The warning mechanism is intentionally separate from the warning text. Platform policies can change, so apps can replace the copy without replacing the persistence/UI implementation.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:FreetimeWarn:<version>")
```

## Required app name

The application name is mandatory. There is no default:

```kotlin
val warning = rememberFreetimeWarnState(
    context = context,
    appName = "GeoWeather",
    versionCode = BuildConfig.VERSION_CODE.toLong(),
)

FreetimeWarn(
    state = warning,
    onLearnMore = {
        // Open the current information page with your preferred browser flow.
    },
)
```

Calling `rememberFreetimeWarnState` without `appName` does not compile. A blank runtime value is rejected with `require`.

## Frequency

- `ONCE` — persists acknowledgement permanently for this warning ID/app.
- `ONCE_PER_VERSION` — default; shows once for each app version code.
- `ALWAYS` — persistence is disabled and the host controls visibility.

## Custom text

```kotlin
val content = FreetimeWarnContent(
    title = "Distribution notice",
    message = { appName -> "Current information affecting $appName." },
)

FreetimeWarn(state = warning, content = content)
```

Keep policy-specific wording current rather than relying indefinitely on text embedded in an old library release.
