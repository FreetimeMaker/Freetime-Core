# Freetime Core module

Small, dependency-light base module shared by the other Freetime Core libraries.

## Dependency

Version **4.0.2** is staged locally; this dependency becomes available once the release is published and anonymously verified.

```kotlin
implementation("com.freetime:Core:4.0.2")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Anonymous access and complete KMP metadata must be verified before release. See the [publishing requirements](../README.md#publishing); the historical AAR-only public uploader is not suitable for KMP.

## What it provides

- `FreetimeCore` — SDK metadata and platform helpers.
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

## Multiplatform

`Core` targets Android (the Android KMP library plugin) and desktop JVM.
Models, preference state, and the `read`/`update`/`reset` API live in common code.
Android retains `FreetimeCore.appName(context)` and
`FreetimePreferences.from(context, name = "freetime_preferences")`, backed by
application-context SharedPreferences with the existing file name and keys.
Desktop creates a store with `FreetimePreferences.from(name = "freetime_preferences")`,
backed by `java.util.prefs.Preferences` under `com/freetime/<name>`; writes flush
before returning and storage failures propagate to the caller. Use an app-specific
name when multiple desktop applications use this library.

Platform entry points create the preferences instance and can pass it to shared UI.
