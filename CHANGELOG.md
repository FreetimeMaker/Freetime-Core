# Changelog

All notable changes to Freetime Core are documented here.

The project follows semantic versioning. Older entries summarize the known evolution of the repository; the latest release entries document the current public changes in detail.

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
