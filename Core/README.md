# Freetime Core module

Small, dependency-light base module shared by the other Freetime Core libraries.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Core:2.0.0")
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

The module intentionally stays small. It does not require Luma Store, authentication, Compose, Material 3 or a Freetime backend. UI belongs in the separate `Design` module.
