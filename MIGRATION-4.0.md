# Migrating to 4.0.0

Version **4.0.0** is the breaking Android/desktop JVM Kotlin Multiplatform successor to Android-only **3.2.0**. It is not published yet. No iOS or web support is claimed.

## Names and dependencies stay stable

The library projects remain `:Core`, `:Design`, `:Browser`, `:Donations`, `:FreetimeWarn` and `:Notifications`, in their existing folders. Root Maven artifact IDs retain the same case-sensitive names as 3.2.0; no module/artifact rename is required. Namespaces and Kotlin packages are unchanged.

After publication, use the repository `https://repo.free-time.me/maven/` and add only the root dependencies you need:

```kotlin
commonMain.dependencies {
    implementation("com.freetime:Core:4.0.0")
    implementation("com.freetime:Design:4.0.0")
    implementation("com.freetime:Browser:4.0.0")
    implementation("com.freetime:Donations:4.0.0")
    implementation("com.freetime:FreetimeWarn:4.0.0")
}
```

Notifications uses JVM-shared Java and `org.json` APIs, not portable Native/JS common code. Add `com.freetime:Notifications:4.0.0` to the Android/desktop JVM source sets that use it. Local consumers use `implementation(project(":Core"))`, `implementation(project(":Design"))`, or the corresponding library project instead of Maven dependencies. Historical JitPack consumers should move to the `com.freetime` coordinates after publication.

Keep Gradle Module Metadata enabled and depend on root artifacts so Gradle selects the appropriate platform. Generated target names such as `Core-android` and `Core-desktop` are correct platform suffixes, not module renames.

## Breaking integration changes

- **Source sets and plugins:** shared Kotlin/Compose APIs move to `commonMain`; platform implementations use `androidMain` and `desktopMain`. KMP modules still require the Kotlin Multiplatform plugin and the appropriate Android KMP library and Compose plugins. Adapt imports and source-set dependencies to the APIs each platform exposes.
- **Metadata:** 4.0.0 requires complete root and target publications with `.module` metadata, POMs and referenced artifacts. The historical Android AAR/POM-only publication is not a usable KMP release.
- **Platform APIs:** Android Context overloads remain platform-specific. Shared browser and donation callbacks use remembered platform-aware launchers and handle returned failures. Desktop browsing is external-only; wallet handlers, clipboard and tray capabilities depend on the OS and graphical session. QR encoding remains host-supplied.
- **Preferences and acknowledgements:** Android retains SharedPreferences-backed storage; desktop uses Java Preferences. Create platform stores at the entry point and pass them to shared UI where appropriate.
- **Notification services:** Android retains its Context facade, WorkManager, DataStore, native channels and host-owned notification-permission request. Desktop uses `DesktopFreetimeNotifications`, atomic JSON storage, AWT tray delivery and in-process polling. Call `initialize()` or `resume()` from an application-owned coroutine after startup. Desktop cannot poll after exit and has no OS background scheduler. See [Notifications](Notifications/README.md) for lifecycle, storage and delivery limits.

Material 3 Expressive remains the default. Default Liquid Glass is navigation-only; imported full-glass templates remain explicit opt-ins. See the [capability matrix](README.md#android-and-desktop-capability-matrix) and individual module guides for platform details.

## Sample is shared KMP

`:Sample` contains real shared UI in `Sample/src/commonMain/kotlin/com/freetime/sample/SampleApp.kt`. Both the Android host's `MainActivity` and the desktop `Main.kt` entry point in `desktopMain`, using a Compose Desktop `Window`, render `SampleApp`.

`:Sample-android` is only the thin Android application host in `Sample/androidApp`. Keep its application plugin separate from the KMP shared module; do not remove the Kotlin Multiplatform plugin from `:Sample`.

```sh
./gradlew :Sample:run
./gradlew :Sample-android:assembleDebug
```

The Android debug assembly passed; desktop GUI execution has not been tested. See [Sample](Sample/README.md) for the completed compilation and assembly checks.

## Validation and publication status

Successful reruns with the unsuffixed module names passed: `:Design:checkTemplates`, `:Sample:compileKotlinDesktop`, `:Sample:desktopJar`, `:Sample-android:assembleDebug` and `:Sample-android:assembleRelease`.

`desktopTest` and `testAndroidHostTest` for Core, Browser, Donations, FreetimeWarn and Notifications passed, along with all 15 Python tests. JVM test counts (desktop / Android host) were Core 3 / 3, Browser 6 / 6, Donations 2 / 2, FreetimeWarn 2 / 2 and Notifications 36 / 34.

Desktop GUI smoke tests and device execution have not been performed; the Core device-test APK has not been compiled. These build and automated-test results do not establish GUI/device runtime behavior. Remote KMP metadata preservation and public/anonymous access remain unverified. No remote publication, upload, release tag or commit has been performed for this milestone.

The credential-free local staging entry point is:

```sh
./gradlew stagePublications -PreleaseVersion=4.0.0
```

Credential-free `stagePublications` passed. Local publication verification covered 18 publications (root, Android and desktop for six libraries), totaling 78 files. All `.module` component versions are `4.0.0` in group `com.freetime`; POM and `.module` contents contain no `-kmp` references. Future public verification must confirm anonymous retrieval and preserved KMP metadata after an explicit authorized upload. Do not use the historical AAR-only uploader.
