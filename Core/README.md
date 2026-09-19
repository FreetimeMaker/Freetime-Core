# Freetime Core module

Base module used by the other Freetime Core libraries.

## Dependency

```kotlin
implementation("me.free-time:freetime-core:<version>")
```

## What it provides

- `FreetimeCore` — SDK metadata and common Android helpers.
- `FreetimeAppInfo` — package/version information shared between modules.
- `FreetimeResult<T>` — lightweight success/error result type.

## Example

```kotlin
val name = FreetimeCore.appName(context)

val app = FreetimeAppInfo(
    packageName = context.packageName,
    versionName = BuildConfig.VERSION_NAME,
    versionCode = BuildConfig.VERSION_CODE.toLong(),
)
```

This module intentionally stays small. It does not require Luma Store, authentication, Compose, or a Freetime backend.
