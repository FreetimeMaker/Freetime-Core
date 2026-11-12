# Copyable Android theme templates

These sources live under `Design/src/main/java/com/freetime/design/templates` and are compiled and published as part of the Design library. For a fully app-owned theme, copy and customize the desired variant. There are no branding tokens or dependencies on the library's root `AppTheme`.

## Choose and copy a variant

- **Material 3:** `material3/` → package `com.freetime.design.templates.material3`, using regular `MaterialTheme`.
- **Expressive:** `expressive/` → package `com.freetime.design.templates.expressive`, using `MaterialExpressiveTheme` with `MotionScheme.expressive()`.

For either variant, copy `Color.kt`, `Type.kt`, `Shapes.kt` and `Theme.kt` into your app's Android source directory, such as `app/src/main/java/<app-package>/ui/theme/templates/material3/` or `…/templates/expressive/`. Update the package declaration in **every copied file** to your app's own subpackage, for example `com.example.app.ui.theme.templates.material3`. Both variants can coexist in separate subpackages; explicitly import the desired variant when function names match.

The core files require only Android and Compose, with no Freetime dependency. The templates use Android's `LocalContext` and are not commonMain sources. They require Compose and Material 3 **1.5.0-rc01** with compatible Compose and Kotlin configuration in the app.

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

This directory belongs to the Design module's main source set. Every normal library build compiles both variants, including previews and Freetime wrappers; their classes are included in the library artifact. The dedicated task checks those same original sources:

```sh
sh gradlew :Design:checkTemplates
```

The check is attached to `:Design:check` and explicitly invoked by the GitHub build workflow. `:Design:assembleDebug` also compiles the templates. Compilation checks API compatibility, not visual appearance; use the light and dark previews to review the result.
