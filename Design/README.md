# Freetime Core Design

Material 3 Expressive, Material You, floating Liquid Glass navigation and an opt-in Material/Expressive glass component toolkit.

The default app theme uses Liquid Glass only for the floating bottom navigation. The opt-in full-glass templates additionally provide refracting content surfaces; normal app UI remains Material 3 Expressive unless these components are chosen explicitly.

## Dependency

Version **3.2.0** is prepared but not yet uploaded; this dependency becomes available after publication.

```kotlin
implementation("com.freetime:Design:3.2.0")
```

Add `https://repo.free-time.me/maven/` to your repositories. See the root [installation and JitPack migration guide](../README.md#installation) for repository setup and access requirements. Public packages require no consumer credentials. The [public publishing flow](../README.md#publishing) requests public visibility for new packages without an initial dashboard step; existing private version conflicts are not automatically made public.

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
in the Design library. Normal library builds compile all four variants, including
previews and optional adapters. You can also run the dedicated compilation check:

```sh
sh gradlew :Design:checkTemplates
```

This check also runs through `:Design:check` and the build workflow. See the
[template guide](src/main/java/com/freetime/design/templates/README.md) for setup, previews, custom fonts and settings.

## Additional Liquid Glass templates

`templates/material3glass/` and `templates/expressiveglass/` add glass-enabled counterparts to the two plain themes. Each provides a `FreetimeTheme` wrapper and a `NavigationTemplate()` with light/dark previews. Navigation taps only move the selected highlight, including the separate search button; the screen content never changes. See the [template guide](src/main/java/com/freetime/design/templates/README.md#liquid-glass-variants) for integration.

## Liquid Glass component toolkit

[`templates/fullglass/`](src/main/java/com/freetime/design/templates/fullglass/README.md) now provides native Material-based glass variants for actions, icon buttons, FABs, cards, selection controls, chips, text/search inputs, lists, badges, progress, app bars, navigation, overlays, pickers and layouts. Expressive adapters include toggle/split buttons, button groups, toolbars, wavy progress, loading and carousels.

Use `LiquidGlassRoot` to supply a sibling background layer to arbitrary app content, or open `LiquidGlassCatalog()` under the Material 3 glass theme. Under the Expressive glass theme, use `LiquidGlassCatalog(expressive = true)`. The catalog has four light/dark previews and interactive controls; navigation remains highlight-only.

These are explicit opt-in adapters, not automatic replacements for every Material API or overload. Native foreground indicators remain visible. Separate-window dialogs, sheets, menus and tooltips deliberately use an opaque fallback rather than unsafe cross-window backdrop sampling. See the toolkit guide for component inventory, integration, sizing and runtime limitations.

## Liquid Glass policy

In the default theme and navigation-only examples, Liquid Glass is used only by the floating bottom navigation. Existing cards, buttons, text fields, dialogs and donation surfaces remain unchanged.

For a complete glass screen, use `FullGlassTemplate()` from `templates.material3glass` or `templates.expressiveglass`. Each owns its theme and includes a glass header, cards, buttons, text-field container and preferences panel over a refracting gradient background. Their reusable opt-in components live in `templates.fullglass`. Standard Material components are not automatically converted into glass.

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

Navigation optics remain encapsulated inside `FloatingBottomNavigationBar`. The separate `templates.fullglass` package exposes a shared `LiquidGlassRoot`, `LiquidGlassSurface` and Material-based component families for complete opt-in glass layouts. These sample a dedicated background, never their own rendered output.

The module currently pins `androidx.compose.material3:material3:1.5.0-rc01` for the current Expressive APIs.
