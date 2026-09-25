# FreetimeWarn

A Kotlin/Jetpack Compose acknowledgement and warning library inspired by FreeDroidWarn.

The UI uses Material 3 directly and stays independent from the removed Freetime-prefixed Design component system.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:FreetimeWarn:2.0.0")
```

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
