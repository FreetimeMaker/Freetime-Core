# Copyable Android theme templates

These sources live under `Design/src/main/java/com/freetime/design/templates` and are compiled and published as part of the Design library. For a fully app-owned theme, copy and customize the desired variant. There are no branding tokens or dependencies on the library's root `AppTheme`.

## Choose and copy a variant

- **Material 3:** `material3/` → package `com.freetime.design.templates.material3`, using regular `MaterialTheme`.
- **Expressive:** `expressive/` → package `com.freetime.design.templates.expressive`, using `MaterialExpressiveTheme` with `MotionScheme.expressive()`.

For either variant, copy `Color.kt`, `Type.kt`, `Shapes.kt` and `Theme.kt` into your app's Android source directory, such as `app/src/main/java/<app-package>/ui/theme/templates/material3/` or `…/templates/expressive/`. Update the package declaration in **every copied file** to your app's own subpackage, for example `com.example.app.ui.theme.templates.material3`. Both variants can coexist in separate subpackages; explicitly import the desired variant when function names match.

The core files require only Android and Compose, with no Freetime dependency. The templates use Android's `LocalContext` and are not commonMain sources. They require Compose and Material 3 **1.5.0-rc01** with compatible Compose and Kotlin configuration in the app.

## Liquid Glass variants

Two additional variants, `material3glass/` and `expressiveglass/`, keep the corresponding Material theme and enable Liquid Glass only for the floating navigation. Their packages are `com.freetime.design.templates.material3glass` and `com.freetime.design.templates.expressiveglass`.

Copy the Kotlin files for the desired example from the chosen glass variant and update every package declaration. These variants require the Design library. Wrap `NavigationTemplate()` in the variant's `FreetimeTheme(...)`; `AppTheme` alone only supplies Material styling. `Preview.kt` includes a public, copyable `NavigationTemplate` and explicit light/dark previews.

The example records its content through `FloatingBottomNavigationGlassRoot`. Tapping a tab or the separate search button only updates a saveable selection highlight: no routes, content changes or search actions are triggered. Search uses `selectedItemIndex = -1` so no tab remains highlighted alongside it. The letter icons are placeholders that can be replaced with app icons. The original non-glass variants remain unchanged.

## Complete Liquid Glass screens

Both glass variants additionally expose `FullGlassTemplate(modifier, darkTheme, dynamicColor)`. Call this directly: it owns its `FreetimeTheme` and needs no outer theme wrapper. Each includes explicit light/dark previews, a glass header, hero card, save button, editable workspace name, preferences panel and floating navigation. The Expressive version uses larger display typography and more generous spacing.

The Save button toggles local saved feedback, the text field is editable and the notification switch updates local state. Navigation still only changes its highlight. All example state is saveable; no settings are written to disk or external services.

Shared components are in [`fullglass/`](fullglass/README.md): `LiquidGlassRoot`, `LiquidGlassSurface`, `FullGlassScreen` and opt-in Material/Expressive component families. When copying a complete design, copy this shared package too and update its package/imports. `FullGlassScreen` installs their background provider; isolated surfaces without it use an opaque Material fallback. These templates require the Design library and its backdrop dependency.

The colorful background is recorded separately from every glass panel to avoid render feedback. Panels use blur, vibrancy, refraction, a rim highlight and a theme-aware scrim; the input text and controls remain sharp. The navigation records the background independently. The standard library theme and the two non-glass templates are unaffected. Runtime appearance, contrast and shader performance still need review on target devices, especially with large font sizes and older Android versions.

## Liquid Glass with Material navigation

For glass content with a non-glass navigation bar, use `MaterialNavigationTemplate()` from either glass package:

- `com.freetime.design.templates.material3glass.MaterialNavigationTemplate`: standard Material 3 `NavigationBar` and `NavigationBarItem`.
- `com.freetime.design.templates.expressiveglass.MaterialNavigationTemplate`: Material 3 Expressive `ShortNavigationBar` and `ShortNavigationBarItem`, inside the Expressive theme.

Both entry points own their theme and accept `modifier`, `darkTheme` and `dynamicColor`. Each includes explicit light/dark previews. Cards, buttons and input containers retain Liquid Glass; navigation uses an opaque Material surface with the standard selected-item indicator. All four items, including Search, only change selection and never navigate or modify the workspace.

The shared `FullGlassScreen` accepts an optional `navigationBar` composable slot with the current selection and a selection callback. Omitting it preserves the original floating glass navigation. The screen measures its navigation height and reserves scrolling space accordingly, including the bar's system insets. Copy `fullglass/MaterialNavigation.kt` with the other shared files when using these combinations.

## Material 3 and Expressive component catalog

The shared [`fullglass` toolkit guide](fullglass/README.md) lists glass variants for actions, cards, inputs/search, chips, selection controls, sliders, progress, navigation/app bars, dialogs/sheets, pickers and layout/gesture containers. Expressive variants include button groups, split/toggle buttons, loading/wavy indicators, toolbars and carousels. These reuse native state/semantics and do not automatically replace ordinary Material calls.

For an interactive playground, wrap `LiquidGlassCatalog()` in the Material 3 glass `FreetimeTheme`, or `LiquidGlassCatalog(expressive = true)` in the Expressive glass theme. Four previews show both themes in light and dark. Catalog navigation only changes highlighting. The catalog source imports both template themes for these previews; when copying only one theme into an app, omit this optional catalog file or adapt its preview imports.

Use `LiquidGlassRoot(source = { ... }) { ... }` to supply glass to arbitrary screens. Background and glass are siblings. Window-separated dialogs, modal sheets, menus and tooltips deliberately receive opaque fallback surfaces; their names do not imply cross-window refraction. Native selection/error/progress indicators remain visible Material paint. The toolkit is not a one-to-one replacement for every overload of the Material library.

## Custom colors, shapes and fonts

`AppLightColorScheme`, `AppDarkColorScheme`, `AppTypography` and `AppShapes` are app-owned values initialized with Material defaults. Customize colors in `Color.kt` or use complete light and dark schemes exported from Material Theme Builder. No library or product palette is applied implicitly.

Place custom font files in your app's `res/font` directory, then configure the text styles in `Type.kt` using `FontFamily`, `Font` and the appropriate weights. The templates do not include font files; Material and system font defaults apply unless customized. Configure corner shapes in `Shapes.kt`.

## App settings and Material You

`AppTheme()` follows the system appearance by default; `dynamicColor` defaults to `false`. Resolve a stored app mode to a Boolean outside the theme (system → `isSystemInDarkTheme()`, light → `false`, dark → `true`) and pass it together with the stored dynamic-color preference:

```kotlin
AppTheme(darkTheme = resolvedDarkTheme, dynamicColor = useDynamicColor) {
    AppContent()
}
```

The names in this example represent the calling app's state. The template does not read or store app settings or enforce another mode.

With `dynamicColor = true`, Android 12 and later replace the entire selected color scheme with Material You system colors, typically derived from the wallpaper. This affects all components that read theme color roles, including text, surfaces, cards, buttons and text fields. Custom static scheme colors are not active in this mode; fonts, shapes and expressive motion remain unchanged. On older Android versions, the app's own light and dark schemes remain active. Expressive does not require dynamic colors.

## Optional Freetime integration without glass

Only if your app uses Freetime Core Design components, also copy the `FreetimeTheme.kt` file **from the selected variant** and update its package declaration. This requires the app to depend on the Design library. Without this file, the core themes remain fully independent of Freetime.

At the app entry point, use `FreetimeTheme(...)` instead of `AppTheme(...)`. The wrapper forwards `darkTheme` and `dynamicColor` unchanged, provides the same dark-mode Boolean through `LocalIsDarkTheme`, and sets `ProvideFloatingBottomNavigationGlass(enabled = false)`. It does not call the library's root theme or introduce additional color or mode selection. The core themes do not draw glass either.

## Light and dark previews

Optionally copy `Preview.kt` from the same variant and update its package declaration. The app needs `androidx.compose.ui:ui-tooling-preview` and typically `ui-tooling` as a debug dependency, using versions compatible with the app.

Each file contains two private preview entry points and a private sample with `Surface`, `Column`, `Text`, `Card`, `Button` and `TextField`. Light and dark modes are set explicitly, and dynamic colors are disabled so both app-owned schemes are visible regardless of the preview device. The samples require neither icons nor Freetime. The text field is a static visual example, not an interactive form.

## Compilation check

This directory belongs to the Design module's main source set. Every normal library build compiles all four variants, including previews and Freetime wrappers; their classes are included in the library artifact. The dedicated task checks those same original sources:

```sh
sh gradlew :Design:checkTemplates
```

The check is attached to `:Design:check` and explicitly invoked by the GitHub build workflow. `:Design:assembleDebug` also compiles the templates. Compilation checks API compatibility, not visual appearance; use the light and dark previews to review the result.
