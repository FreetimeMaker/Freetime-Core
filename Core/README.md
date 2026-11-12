# Freetime Core module

Small, dependency-light base module shared by the other Freetime Core libraries.

## Dependency

```kotlin
implementation("com.freetime:Core:3.1.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Public packages require no consumer credentials. The [public publishing flow](../README.md#publishing) requests public visibility for new packages without an initial dashboard step; existing private version conflicts are not automatically made public.

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
