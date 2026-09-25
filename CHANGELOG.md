## Unreleased

### Design

- Switched the Material foundation to Material 3 Expressive via `MaterialExpressiveTheme` and `MotionScheme.expressive()`.
- Pinned Compose Material 3 to `1.5.0-alpha29`, because the stable 1.4.0 line does not ship the current Expressive APIs.
- Updated the Sample app to use expressive increased shapes and medium expressive button sizing.

- Removed the legacy Freetime-prefixed Design API, including custom buttons, cards, typography, shapes, spacing, layout, settings, picker, feedback and scaffold wrappers.
- Reduced the Design module to Material 3 / Material You theming plus reusable Liquid Glass primitives.
- Added `AppTheme`, `ThemeMode`, `LocalIsDarkTheme` and the global `LocalLiquidGlassEnabled` setting.
- Matched the shared Liquid Glass recipe to SimpMusic's current implementation: 1.5 saturation, 0.05 brightness, adaptive 2–16dp blur, half-height refraction, 0.12–0.50 scrim, pointer glow and spring press bulge.
- Made the shared `liquidGlass` primitive own the on/off behavior globally. Disabled glass now automatically falls back to Material 3 `surfaceContainerHighest` at 80% opacity.
- Kept automatic theming with light from 07:00 and dark from 19:00, plus Material You dynamic colors on Android 12+.
- Migrated Browser, Donations, FreetimeWarn and the Sample app away from the removed Design wrappers and onto Material 3.

# Changelog

All notable changes to Freetime Core are documented here.

The project follows semantic versioning. Older entries summarize the known evolution of the repository; the latest release entries document the current public changes in detail.

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
