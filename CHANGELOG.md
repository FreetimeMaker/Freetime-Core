# Changelog

All notable changes to Freetime Core are documented here.

The project follows semantic versioning. Only **v4.0.1** is supported. Versions below **v4.0.1** are available only through [JitPack](https://jitpack.io/) and are no longer supported.

## Unreleased

No unreleased changes yet.

## 4.0.1 - 2026-11-19

### Unified KMP publication

- Replaced the three-coordinate KMP layout (root package plus `Core-android`/`Core-desktop` target packages) with **one Maven coordinate per library**. `com.freetime:<Module>:4.0.1` carries the common metadata JAR, sources JAR, `-android.aar`, `-android-sources.jar`, `-desktop.jar` and `-desktop-sources.jar` as classifiers, together with the POM and `.module` metadata. Consumers only ever reference the root coordinate.
- Added a three-stage credential-free pipeline: `stageNativePublications` publishes the native root/Android/desktop publications to the private `build/native-maven` input tree, `prepareUnifiedPublications` runs `.github/scripts/consolidate_kmp_publications.py`, and `stagePublications` publishes the resulting `unified` publication to `build/public-maven`.
- The consolidator validates the whole input before writing anything: SemVer shape, group/version/module agreement, one file per variant, size and SHA-512/SHA-256/SHA-1/MD5 agreement, allowed variant roles, no nested `available-at`, and a complete set of eight artifacts per module. Output files are replaced individually and atomically, and staging/output must be disjoint. Cross-module dependencies on `Core-android`/`Core-desktop` and `Design-desktop` are rewritten to their root coordinates; unrelated third-party dependencies are left untouched.
- Only the unified publication may leave the build. Native publications are restricted to the private staging repository, the `unified` publication is the only one that can reach the public staging and Gemfury repositories, and the historical AAR-only `publishPublic` path still fails with an explicit message.
- The unified POM keeps the native dependency block and the `published-with-gradle-metadata` marker so Gradle requests the complete variant metadata instead of falling back to the POM. It also reports `jar` packaging: attaching the `.module` file as a second classifier-less artifact otherwise makes Gradle emit `pom`.

### Release verification

- Rewrote `verify_gemfury_release.py` around the unified layout. It now requires the POM, `.module`, main JAR, Android AAR and desktop JAR, parses the metadata and requires every referenced artifact, rejects `available-at` redirects, unsafe or non-root URLs, wrong `formatVersion`/component identity and missing API/runtime variants, then downloads every staged file anonymously and compares SHA-256 hashes. Added `--local-only` to validate a staging tree without network access and a CLI that reports failures without a traceback.
- Expanded `test_gemfury_release.py` from 7 to 18 tests covering the required-file set, metadata validation, redirect/URL rejection, HTTP error handling and both CLI modes, and added `test_consolidate_kmp.py` with 14 offline integration tests over native-shaped metadata and ZIPs. The publishing suite now runs 41 tests.

### Build, dependencies and version consistency

- Updated Compose Multiplatform from `1.10.3` to `1.12.2` and the Android device-test libraries to AndroidX Test runner `1.7.0` and JUnit ext `1.3.0`.
- Updated the shared release version, both `FreetimeCore.SDK_VERSION` actuals, the SDK identity tests, the dependency examples in the root and module READMEs, the migration guide and the Sample documentation to `4.0.1`.
- Verified locally: the complete CI command set (`:Design:checkTemplates`, five `testAndroidHostTest`, `:Sample-android:assembleDebug`/`assembleRelease`, the six `desktopTest` tasks and `:Sample:compileKotlinDesktop`), `stagePublications` with six publications and 48 files, `verify_gemfury_release.py --local-only` over that tree, and a scratch Gradle consumer that resolves `com.freetime:Core:4.0.1` to `Core-4.0.1-desktop.jar`.

## Unsupported versions (JitPack only)

Versions below **v4.0.1** are available only through [JitPack](https://jitpack.io/) and are no longer supported. The following entries are historical release notes, not supported release recommendations.

## 4.0.0 - 2026-11-18

### Breaking KMP import

- Migrated `:Sample` to real shared KMP UI with a desktop entry point and a thin `:Sample-android` host in `Sample/androidApp`. Breaking changes concern source sets, KMP metadata, platform APIs and notification services, not module/artifact renaming.
- Added platform-aware browser/donation launchers, desktop preferences and warning persistence. Desktop Browser is external-only, not an embedded WebView.
- Added desktop in-process notification polling, atomic JSON storage and AWT tray delivery. Notification engine ports are JVM-shared with Java imports; no iOS/web support is claimed.
- Preserved imported optional full-glass Design templates separately from the default navigation-only glass policy.
- KMP consumption requires root and Android/desktop publications with Gradle Module Metadata. The previous AAR-only public uploader is not a KMP publication path.
- Documented API migration and capability limits in [MIGRATION-4.0.md](MIGRATION-4.0.md).
- Aligned both platform `FreetimeCore.SDK_VERSION` actuals to `4.0.0`, cleaned up migrated SDK tests, and advanced the Android sample migration counter to version code `6`, with version name derived from `project.version`.
- Successful reruns with the unsuffixed module names passed: `:Design:checkTemplates`, `:Sample:compileKotlinDesktop`, `:Sample:desktopJar`, `:Sample-android:assembleDebug` and `:Sample-android:assembleRelease`.
- `desktopTest` and `testAndroidHostTest` for Core, Browser, Donations, FreetimeWarn and Notifications passed, along with all 15 Python tests. JVM test counts (desktop / Android host) were Core 3 / 3, Browser 6 / 6, Donations 2 / 2, FreetimeWarn 2 / 2 and Notifications 36 / 34.
- Credential-free `stagePublications` passed. Local publication verification covered 18 publications (root, Android and desktop for six libraries), totaling 78 files. All `.module` component versions are `4.0.0` in group `com.freetime`; POM and `.module` contents contain no `-kmp` references.
- Desktop GUI smoke tests and device execution have not been performed; the Core device-test APK has not been compiled. These build and automated-test results do not establish GUI/device runtime behavior. Remote KMP metadata preservation and public/anonymous access remain unverified. No remote publication, upload, release tag or commit had been performed when this entry was written; the CI release attempt that followed uploaded only POMs and root JARs, failed anonymous verification and created no tag (see 4.0.1).
- Remote publication remains an explicit action through `publishKmp`/`publishGemfury`, not the historical AAR-only `publishPublic` path.

## 3.2.0 - 2026-11-13

### Notifications

- Added the Android `Notifications` library with namespace `com.freetime.notifications` and an API dependency on `Core`. Its Maven coordinates are `com.freetime:Notifications:3.2.0`.
- Added local GitHub release and F-Droid version detection, GitHub release-note changelog notifications, and developer messages from `blog.free-time.me`. No Firebase, UnifiedPush, Google Play Services, companion app or push backend is required.
- Compared GitHub numeric/semver-style release tags against the installed app's version name, excluding drafts and optionally prereleases. Compared F-Droid's suggested version, or the highest available version when no suggestion exists, against the installed long version code.
- Added custom F-Droid per-package APIs, raw index-v1 JSON and index-v1.jar metadata support. Metadata checks are informational; repository signatures are not validated and APKs are not downloaded or installed.
- Read developer messages from the blog's verified public API, with case-insensitive category filters and encoded post links. Loaded full post text only for pending deliveries, falling back to a headline notification if the detail endpoint is unavailable.
- Added independent update, changelog and developer-message channels with app-configurable names, descriptions and importance. Persisted user category overrides take precedence over initialization defaults; Android's permission and channel settings remain authoritative.
- Persisted configuration and up to 500 delivered IDs per source/category in DataStore. The first successful check establishes a silent baseline unless first-check notifications are explicitly enabled; unposted entries remain pending.
- Scheduled network-constrained WorkManager polling with a six-hour default and 15-minute minimum. Added foreground `checkNow`, best-effort queued checks and stop controls. This is polling, not guaranteed immediate push, and Android can delay background execution.
- Cancelled and invalidated active checks on configuration changes or stop, preventing stale delivery without blocking lifecycle changes behind network requests. Resolved persisted icon resource names against the current app installation after updates.
- Bounded HTTPS response sizes, redirects and request duration; bounded archive inflation and sanitized background failure logs. The host app requests notification permission on Android 13+.
- Added an English integration guide in [Notifications/README.md](Notifications/README.md).

### Build and publishing

- Included Notifications in the six-module public-upload and anonymous-verification flow, using the shared release version and explicit `public=true` uploads.
- Added Notifications compilation, unit tests and Android lint to CI.
- Added AndroidX WorkManager `2.12.0`, Preferences DataStore `1.3.0` and coroutines `1.11.0` for Notifications, plus JVM JSON fixtures for parser tests.
- Added unit coverage for source parsing, archive limits, version ordering, configuration persistence formats, deduplication policy and lifecycle invalidation.

### Version consistency

- Updated the shared version catalog, `FreetimeCore.SDK_VERSION` and current dependency examples to `3.2.0`.
- Updated the Sample app to version `3.2.0` with version code `5`.
- Kept existing 3.1.x APIs unchanged. Notifications is an optional dependency; notification permission and runtime setup are required only by apps adopting that module.

## 3.1.0 - 2026-11-12

### Publishing

- Migrated public publishing from JitPack to Gemfury, using the stable Maven group `com.freetime`, case-sensitive artifact IDs `Core`, `Design`, `Browser`, `Donations` and `FreetimeWarn`, and exact catalog version `3.1.0`. Artifact versions are not randomized; multipart boundaries may be.
- Added root `sh gradlew publishPublic` to stage all five actual release AARs and POMs through a file Maven repository in `build/public-maven`, then run `.github/scripts/publish_public_maven.py VERSION`. `sh gradlew publish` stages locally only.
- Embedded `META-INF/maven/com.freetime/<Module>/{pom.xml,pom.properties}` in uploaded AARs and used multipart `public=true` and `file` with `POST https://push.fury.io/uploads?as=freetimemaker`. The account name is push-only; public consumption uses `https://repo.free-time.me/maven/` without credentials.
- Limited public publication and exact-coordinate anonymous verification to AARs and POMs. Disabled Gradle `.module` metadata; POM metadata retains dependency information. Sources and Javadoc are still built and staged locally, but are not advertised or downloadable publicly.
- Recorded that same-version sources/Javadoc classifier multipart uploads were refused with HTTP 409; native Maven PUT returned HTTP 200 without exposing artifacts and metadata finalization conflicted. Neither is a supported public publication path.
- Removed the mandatory initial dashboard visibility step for new packages: public uploads request visibility directly. Existing private duplicate versions still fail with a conflict and are not automatically made public. Retries skip HTTP 409 only if the public AAR is identical by archive entry contents.
- Documented only `FURY_TOKEN` as a CI secret or `furyToken` in the user’s untracked `~/.gradle/gradle.properties`; `FURY_USERNAME` is not needed for the public path. Publishing tokens must never be embedded in apps or tracked files.
- Added credential-free staged-archive validation with `python3 .github/scripts/publish_public_maven.py 3.1.0 --dry-run`.
- Configured CI to stage and publicly upload, verify anonymous POM/AAR downloads for all five exact coordinates, then create the release tag. Uploaded all five actual `com.freetime` modules at version `3.1.0` and verified anonymous downloads of every AAR and POM through the custom repository URL.
- Kept existing tagged versions from being automatically republished; normally, bump the shared version for the next release, not merely for documentation changes.

### Theme templates

- Added classic Material 3 and Material 3 Expressive theme templates under `com.freetime.design.templates.material3` and `com.freetime.design.templates.expressive`.
- Included both template variants in the Design library and its locally staged sources archive; the public publication does not expose sources classifiers.
- Separated colors, typography, shapes and theme composition into dedicated files that apps can copy and customize without inheriting product-specific branding.
- Made Material You opt-in for the templates, with system light/dark appearance as the default and no prescribed schedule or OLED treatment.
- Kept the core template sources independent of Freetime-specific providers and added separate `FreetimeTheme` adapters that disable floating-navigation Liquid Glass.
- Added light and dark component previews for both variants.
- Added an English template guide covering setup, package names, custom fonts, app settings and dynamic-color behavior.
- Kept the existing root `AppTheme` and navigation APIs unchanged. Direct template usage currently exposes dark-mode and dynamic-color options; custom palettes, typography and shapes are configured in app-owned copies.

### Build and dependencies

- Added `:Design:checkTemplates` to compile the template sources, including previews and Freetime adapters, through the Design verification lifecycle and CI.
- Updated Gradle to `9.9.1`, Android Gradle Plugin to `9.5.1` and Kotlin to `2.4.21`.
- Updated Material 3 to `1.5.0-rc01`, Core KTX to `1.19.1` and Activity Compose to `1.13.0`.
- Updated the CI build job to use JDK 25 and refreshed checkout and Java setup actions.

### Version consistency

- Updated the shared release version, `FreetimeCore.SDK_VERSION` and current dependency examples to `3.1.0`.
- Updated the Sample app to version `3.1.0` with version code `4`.

## 3.0.0 - 2026-09-27

### Breaking Design API

- Removed the generic Liquid Glass API surface introduced in 2.0.0.
- Removed `Modifier.liquidGlass()`, `liquidGlassCapsule()`, `liquidGlassCircle()`, `LiquidGlassContainer`, `LiquidGlassIconButton`, public generic backdrop helpers and the generic `LiquidGlassRoot`.
- Replaced the global glass API with navigation-specific `LocalFloatingBottomNavigationGlassEnabled`, `floatingBottomNavigationGlassEnabled` and `ProvideFloatingBottomNavigationGlass`.
- Renamed the stored glass preference to `floatingBottomNavigationGlassEnabled`.
- Liquid Glass is now supported only by the floating bottom navigation; other surfaces remain Material 3 Expressive.

### Floating bottom navigation

- Added the dedicated `FloatingBottomNavigationBar` and `FloatingBottomNavigationItem` APIs.
- Added `FloatingBottomNavigationGlassRoot` as the navbar-only backdrop host.
- Matched the current SimpMusic Android geometry: 64dp capsule, 56dp sliding frosted selection indicator, tabs capped at 96dp, 6dp inset, 12dp gap and a separate 56dp circular search action.
- Added adaptive backdrop luminance sampling for blur and scrim behavior.
- Added draggable selection with spring scale, squash/stretch and snap-to-tab behavior.
- Added navbar-only Material fallback when Liquid Glass is disabled.
- Fixed a grey rectangular selection artifact by enforcing capsule clipping on the composited selection layer.

### Material 3 Expressive

- Kept normal cards, buttons, text fields, dialogs, top bars, donation surfaces and content on Material 3 Expressive.
- Kept Material You dynamic colors, OLED mode and automatic 07:00/19:00 light/dark switching.

### Donations

- Removed Liquid Glass from the donation screen.
- Removed the Donations module's dependency on Design.

### Build and sample

- Updated the Sample app to demonstrate only the navbar-specific Liquid Glass API.
- Fixed Compose restricted-suspension errors in the navigation drag handler.
- Fixed Compose annotation errors in the selection renderer.
- Confirmed the complete build and test workflow succeeds before the 3.0.0 version bump.

### Version consistency

- Updated the shared Freetime version catalog to `3.0.0`.
- Updated `FreetimeCore.SDK_VERSION` to `3.0.0`.
- Updated all current dependency examples to `3.0.0`.
- Updated the Sample app to version `3.0.0`.

### Migration from 2.x

Replace generic Liquid Glass usages with `FloatingBottomNavigationGlassRoot` and `FloatingBottomNavigationBar`. Keep all non-navigation UI on Material 3 Expressive.

## 2.0.0 - 2026-09-25

### Breaking Design migration

- Removed the legacy Freetime-prefixed Design component API, including the custom button, card, typography, shape, spacing, layout, settings, picker, feedback and scaffold wrappers.
- Material 3 Expressive is now the component foundation through `MaterialExpressiveTheme` and `MotionScheme.expressive()`.
- Material You dynamic colors remain enabled on Android 12+.
- Kept automatic light/dark behavior with light mode from 07:00 and dark mode from 19:00 by default.
- Reduced the Design module to theming plus reusable Liquid Glass primitives.
- Updated Browser, Donations, FreetimeWarn and Sample to use Material 3 directly instead of the removed Design wrappers.

### Liquid Glass

- Added one global `LocalLiquidGlassEnabled` setting controlled through `AppTheme` or `ProvideLiquidGlass`.
- Made shared `liquidGlass` modifiers own their enabled/disabled behavior so screens do not need individual branches.
- Added Material fallback surfaces when Liquid Glass is disabled.
- Matched the shared optics to the SimpMusic-inspired recipe with vibrancy, 1.5 saturation, 0.05 brightness, adaptive 2–16dp blur, refraction, adaptive scrim, pointer glow and spring press behavior.

### Material 3 Expressive

- Pinned Compose Material 3 to `1.5.0-alpha29` for the current Expressive APIs.
- Updated the Sample app to demonstrate increased Expressive shapes, Expressive motion and medium button sizing.

### Version consistency

- Updated the shared version catalog to `2.0.0`.
- Updated `FreetimeCore.SDK_VERSION` to `2.0.0`.
- Updated all current module dependency examples to `2.0.0`.
- Updated the Sample app version to `2.0.0`.

### Migration

Applications migrating from 1.x should replace removed Freetime-prefixed Design components with Material 3 equivalents and use Liquid Glass only as a modifier/effect layer.

## 1.11.0

### FreetimeWarn

- Added the new `FreetimeWarn` Kotlin/Jetpack Compose library module.
- Added mandatory `appName` configuration with compile-time enforcement through a required parameter and runtime rejection of blank values.
- Added `FreetimeWarnState` with `show()`, `dismiss()`, `acknowledge()` and `reset()` lifecycle controls.
- Added `ONCE`, `ONCE_PER_VERSION` and `ALWAYS` warning frequencies.
- Added private SharedPreferences-backed acknowledgement persistence with independent `warningId` support.
- Added customizable `FreetimeWarnContent` and a host-controlled Learn more callback so policy-specific wording and navigation remain replaceable.
- Added `FreetimeWarnDialog` and `FreetimeWarn` composables using Freetime Design components and Liquid Glass instead of Material dialogs.
- Added module-specific README documentation and registered FreetimeWarn in the multi-module build.

### Version consistency

- Updated the shared Freetime version catalog and `FreetimeCore.SDK_VERSION` to 1.11.0.
- Updated the root dependency example to 1.11.0.

## 1.10.0

### Freetime Design

- Reworked Liquid Glass to closely follow SimpMusic-style optics and interaction while retaining Freetime's tintable glass colors.
- Added recorded-backdrop luminance sampling with a lightweight 5×5 sample, Rec.709 luminance calculation and smooth luminance transitions.
- Added GraphicsLayer backdrop recording support for reusable glass surfaces.
- Upgraded bottom navigation with a draggable spring-driven selection blob, velocity-based squash/stretch, press bulge and snap-to-destination behavior.
- Added stronger selected-glass frosting, chromatic lens refraction, highlights, shadow and press-driven inner shadow.
- Added observe-only glass gesture tracking so liquid interactions do not consume normal control clicks.
- Added a persistent Liquid Glass enable/disable preference and a non-glass fallback.
- Added a shared Freetime preferences controller at the app root so appearance and accessibility changes update the renderer immediately.
- Fixed Compose/Kyant compatibility issues in backdrop recording, luminance capture, density conversion and navigation coroutines.
- Made Liquid Glass substantially more transparent by reducing surface, fallback and adaptive scrim opacity.
- Strengthened the glass refraction effect with a deeper lens and chromatic aberration.
- Updated `FreetimeScaffold` to respect safe drawing insets so content is not obscured by status or navigation bars.

## 1.9.1

### Freetime Design

- Expanded `FreetimeText` with `fontSize`, `fontWeight`, `textAlign`, `overflow`, `softWrap` and `minLines` while keeping existing calls source-compatible.

## 1.9.0

### Freetime Design

- Added `FreetimeScaffold` as a Material-free screen layout with top bar, content, bottom bar and floating action slots.
- Added Liquid Glass tabs, floating action buttons and bottom sheets.
- Added adaptive navigation that switches between floating bottom navigation and a glass navigation rail for wider layouts.
- Added `FreetimeNavigationRail`, `FreetimePopupMenu` and reusable `FreetimeMenuItem` models.
- Expanded the Sample app to showcase the new layout primitives.

## 1.8.0

### Freetime Design

- Expanded the Liquid Glass primitives with tintable, adaptive glass optics, compact and wide interaction behavior, selection glass and round glass highlights.
- Added sliding frosted selection treatment for bottom navigation.
- Added `FreetimeGlassText` and `FreetimeGlassTitle` for glass-styled typography.
- Updated reusable Freetime controls to use the shared Liquid Glass primitives consistently.

### Donations

- Migrated the donation screen from Material 3 UI components to Freetime Design components and Liquid Glass.
- Removed the Material 3 dependency from the Donations module.

### Sample app

- Added the `:Sample` Android application demonstrating Core, Design, Browser and Donations together.
- Added examples for SDK helpers, result models, browser validation/opening, donation targets and the reusable Freetime Design component set.
- Added the Sample application to CI assembly checks.

### Build

- Added Android application plugin configuration for the Sample module.
- Added Compose activity and icon dependencies used by the Sample application.

## 1.7.0

### Freetime Design

- Added reusable app-level components derived from common GeoWeather, Luma Store and SuperSMP Companion UI patterns: text, screen structure, section headers, list items, option groups, status banners, loading/empty/error states, dividers, badges and info cards.
- Kept these components app-agnostic through generic values and composable slots, avoiding dependencies on Navigation Compose, Material Icons or app-specific models.

## 1.6.0

### Freetime Design

- Turned `Design` into a standalone Freetime Compose design system rather than a Material 3-based skin.
- Removed the Material 3 dependency from the Design module.
- Added the Freetime palette and standalone light, dark and OLED-aware theme foundation.
- Added Freetime typography independent from Material 3 `Typography`.
- Added reusable spacing, sizing, shape, depth and motion tokens.
- Exposed theme values through `FreetimeDesign`: glass, colors, palette, typography, shapes, spacing, sizes and motion.
- Reworked Liquid Glass to use Freetime tokens and colors.
- Tuned Liquid Glass toward a clearer SimpMusic-inspired appearance with lower surface opacity, backdrop blur, saturation, brightness, highlights and interactive press behavior.
- Kept a translucent Freetime fallback for devices where the sampled backdrop path is unavailable.
- Added `FreetimeButton`, `FreetimeIconButton`, `FreetimeCard`, `FreetimeTopBar` and `FreetimeChip`.
- Added `FreetimeBottomBar` and `FreetimeAdaptiveBottomBar` with a floating capsule layout and compact scrolling state.
- Added `FreetimeTextField`, `FreetimeSwitch`, `FreetimeSlider`, `FreetimeDialog`, `FreetimeSnackbar` and `FreetimeProgressIndicator`.
- Added Foundation-based text and icon rendering so Freetime controls no longer require Material UI components.
- Added a Design showcase and light/dark previews.
- Deprecated the older `FreetimeGlassDepth` and `FreetimeDesignTokens` APIs in favor of the typed token system.
- Fixed Compose/Foundation migration issues in text colors, icons, shapes, Canvas drawing, settings controls and feedback components.

### Build and CI

- Made the CI workflow explicitly assemble and test the Design module.
- Kept SDK and dependency versions centralized in the version catalog.
- Updated module documentation for the standalone Freetime Design architecture.

## 1.5.1

- Maintenance release immediately preceding the standalone Design-system work.
- Served as the previous version baseline for the 1.6.0 migration.

## 1.5.0

- Expanded the reusable Freetime library set used by Freetime Android applications.
- Continued consolidation of shared design, browser and donation functionality into Freetime Core.

## Earlier development

Freetime Core began as a shared Android foundation for Freetime Maker applications. During its initial development the project established the module structure that evolved into the current library:

- `Core` for shared application metadata, result types and lightweight Android helpers.
- `Design` for reusable Compose UI and Liquid Glass.
- `Browser` for external and app-owned in-app URL routing.
- `Donations` for reusable donation targets and donation UI.
- Centralized Gradle version management and reusable multi-module publishing.
- An architecture that keeps consuming applications independent from Luma Store, authentication and hard-coded Freetime backends.
- Open-source and F-Droid-friendly dependency choices as a project principle.

From that base, the Design module evolved from Material-themed reusable glass components into the independent Freetime Design system introduced in 1.6.0.
