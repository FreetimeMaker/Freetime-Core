# Android and desktop Sample

The existing Design, Settings, Browser, and Donations UI lives in
`src/commonMain/kotlin/com/freetime/sample/SampleApp.kt`. Browser and donation
callbacks use the shared platform-aware remembered launchers. The Android host's `MainActivity` and `src/desktopMain/kotlin/com/freetime/sample/Main.kt` (Compose Desktop `Window`) both render the shared `SampleApp`.

`Sample` is an Android KMP library plus a desktop JVM Compose application.
`Sample-android` is a thin Android application in `androidApp/`, with its own
Activity, manifest, and theme. Keeping the application plugin out of the KMP
project avoids AGP 9's incompatible application/KMP plugin combination, without
requiring `android.builtInKotlin=false` or `android.newDsl=false`.

## Project mapping

Root settings map the projects to existing folders (other library mappings are omitted here):

```kotlin
include(":Core", ":Sample", ":Sample-android")
project(":Core").projectDir = file("Core")
project(":Sample").projectDir = file("Sample")
project(":Sample-android").projectDir = file("Sample/androidApp")
```

The version catalog must supply `kotlin.multiplatform`,
`android.multiplatform.library`, `compose.multiplatform`, `compose.compiler`, and
`android.application`. Project dependencies retain their names: `:Core`, `:Design`, `:Browser`, and `:Donations`. The Kotlin Multiplatform plugin remains required for `:Sample`.
The Android host uses AGP 9 built-in Kotlin; leave it enabled.

Run desktop with `:Sample:run`, or build/install the
Android application with `:Sample-android:assembleDebug` /
`:Sample-android:installDebug`. Successful reruns passed `:Sample:compileKotlinDesktop`, `:Sample:desktopJar`, `:Sample-android:assembleDebug` and `:Sample-android:assembleRelease`, along with `:Design:checkTemplates`. GUI/device execution and `installDebug` are not established by those results.

The project release version is **4.0.2**. The Android host uses version code **7** and `versionName = project.version.toString()`. Desktop GUI smoke tests and device tests have not been run. Only Android and desktop JVM are supported, not iOS or web. Desktop Browser opens external URLs; Android has the embedded WebView. See the [migration guide](../MIGRATION-4.0.md).

## Recorded 4.0.1 validation

The results below describe the previous release. Version `4.0.2` requires a fresh build and local publication validation; they do not establish remote availability.

`desktopTest` and `testAndroidHostTest` for Core, Browser, Donations, FreetimeWarn and Notifications passed, along with the Python publishing test suite. JVM test counts (desktop / Android host) were Core 3 / 3, Browser 6 / 6, Donations 2 / 2, FreetimeWarn 2 / 2 and Notifications 36 / 34.

Credential-free `stagePublications` passed. Local publication verification covered six unified publications (one coordinate per library), totaling 48 files. All `.module` component versions are `4.0.1` in group `com.freetime`; POM and `.module` contents contain no `-kmp` references and no `available-at` redirects.

Desktop GUI smoke tests and device execution have not been performed; the Core device-test APK has not been compiled. These build and automated-test results do not establish GUI/device runtime behavior. Remote KMP metadata preservation and public/anonymous access remain unverified: the earlier 4.0.0 upload exposed only POMs and root JARs and was rejected by the anonymous verification step.
