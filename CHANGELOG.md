# Changelog

All notable changes to Freetime Core are documented here.

The project follows semantic versioning. Older entries summarize the known evolution of the repository; the 1.6.0 entry documents the current release in detail.

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
