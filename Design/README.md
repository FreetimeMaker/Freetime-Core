# Freetime Core Design

Material 3 Expressive, Material You and a focused Liquid Glass treatment for the floating bottom navigation.

Version 3.x exposes Liquid Glass only through the dedicated floating bottom navigation. Normal UI uses Material 3 Expressive directly.

## Dependency

```kotlin
implementation("com.freetime:Design:3.1.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Public packages require no consumer credentials; they must be marked public in Gemfury.

## Theme

```kotlin
AppTheme {
    AppContent()
}
```

`AppTheme` uses `MaterialExpressiveTheme`, `MotionScheme.expressive()` and Material You dynamic colors on Android 12+. Older devices keep the same Expressive component and motion system with regular Material light/dark color schemes.

Available modes:

- `SYSTEM`
- `LIGHT`
- `DARK`
- `OLED`
- `AUTO_TIME`

`AUTO_TIME` defaults to light from **07:00** and dark from **19:00**.

## App-owned theme templates (without Liquid Glass)

[`templates/`](src/main/java/com/freetime/design/templates/README.md) provides two copyable variants with a structure
similar to GeoWeather **v4.1.0**, but without its branding or design tokens:

- [`material3/`](src/main/java/com/freetime/design/templates/material3/): classic `MaterialTheme`.
- [`expressive/`](src/main/java/com/freetime/design/templates/expressive/): `MaterialExpressiveTheme` with expressive motion.

Their packages are `com.freetime.design.templates.material3` and
`com.freetime.design.templates.expressive`. Copy the desired variant into your app
and change the package to your own `templates` subpackage.

Each variant separates colors (`Color.kt`), typography (`Type.kt`), shapes
(`Shapes.kt`) and theme composition (`Theme.kt`). Optional `Preview.kt` files
show light and dark examples. Optional `FreetimeTheme.kt` adapters disable
floating-navigation glass; the core templates depend only on Compose.

Apps own all design values and theme behavior. The starting values are Material
3 defaults, Material You is opt-in, and there is no prescribed schedule or OLED
behavior. The existing library `AppTheme` remains unchanged.

Templates live in `src/main/java/com/freetime/design/templates` and are included
in the Design library. Normal library builds compile both variants, including
previews and optional adapters. You can also run the dedicated compilation check:

```sh
sh gradlew :Design:checkTemplates
```

This check also runs through `:Design:check` and the build workflow. See the
[template guide](src/main/java/com/freetime/design/templates/README.md) for setup, previews, custom fonts and settings.

## Liquid Glass policy

Liquid Glass is used only by the floating bottom navigation.

Cards, buttons, text fields, dialogs, top bars, donation surfaces and normal content stay on regular Material 3 Expressive styling.

The navigation follows SimpMusic's current Android geometry and interaction: a 64dp glass capsule, 56dp sliding/frosted selection pill, tabs capped at 96dp, 6dp inner inset, 12dp gap and a separate 56dp circular search action. The selection pill can be dragged and uses spring scale/squash animation.

```kotlin
AppTheme(
    floatingBottomNavigationGlassEnabled = settings.floatingBottomNavigationGlassEnabled,
) {
    FloatingBottomNavigationGlassRoot(
        source = { AppBackground() },
    ) {
        FloatingBottomNavigationBar(
            items = tabs,
            selectedItemIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            searchItem = searchItem,
            searchSelected = isSearchSelected,
            onSearchSelected = ::openSearch,
        )
    }
}
```

When disabled, the bar keeps the same floating geometry and automatically falls back to Material surfaces.

## Glass optics

The floating navigation uses the shared SimpMusic-inspired recipe:

- backdrop vibrancy
- saturation **1.5**
- brightness **0.05**
- adaptive blur from **2dp to 16dp**
- refraction up to half the surface height
- adaptive scrim from **0.12 to 0.50**

There are no public generic Liquid Glass modifiers, containers, icon buttons, circle helpers or capsule helpers. Liquid Glass is encapsulated inside `FloatingBottomNavigationBar`.

The module currently pins `androidx.compose.material3:material3:1.5.0-rc01` for the current Expressive APIs.
