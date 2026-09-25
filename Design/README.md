# Freetime Design

Freetime's Material 3 / Material You Compose UI layer with reusable Liquid Glass.

Material 3 is the UI foundation again. `FreetimeTheme` installs `MaterialTheme`, uses Material You dynamic colors on Android 12+ by default, keeps Freetime Liquid Glass as a reusable modifier/component layer, and preserves the older Freetime token APIs as a compatibility bridge for existing apps.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.10.0")
```

## Theme

```kotlin
FreetimeTheme {
    FreetimeGlassRoot {
        AppContent()
    }
}
```

`FreetimeTheme` now uses Material 3 as the source of truth. Access `MaterialTheme.colorScheme`, `MaterialTheme.typography` and `MaterialTheme.shapes` directly, or use `FreetimeDesign.colorScheme`, `FreetimeDesign.materialTypography` and `FreetimeDesign.materialShapes`. The older Freetime palette/typography accessors remain available for source compatibility.

```kotlin
val spacing = FreetimeDesign.spacing.lg
val title = MaterialTheme.typography.titleLarge
val foreground = MaterialTheme.colorScheme.onSurface
```

## Liquid Glass

The glass engine is based on Compose UI/Foundation plus Kyant Backdrop and Shapes. The default glass recipe uses a transparent surface, backdrop blur, increased saturation, subtle brightness, highlights and interactive press scaling.

Where a sampled backdrop is unavailable, Freetime Design falls back to its own translucent light/dark glass surface instead of a Material surface.

Keep the backdrop source and foreground glass content separated:

```kotlin
FreetimeGlassRoot {
    FreetimeCard {
        // Content
    }
}
```

Available modifiers include:

```kotlin
Modifier.freetimeGlass()
Modifier.freetimeGlass(shape = FreetimeDesign.shapes.surface)
Modifier.freetimeGlassCapsule()
```

Use `interactive = false` for non-clickable surfaces.

## Components

The module provides reusable Freetime-native controls:

```kotlin
FreetimeButton("Continue", onClick = ::continueFlow)

FreetimeCard {
    // Content
}

FreetimeTextField(
    value = query,
    onValueChange = { query = it },
    placeholder = "Search",
)

FreetimeSwitch(
    checked = enabled,
    onCheckedChange = { enabled = it },
)
```

Navigation components include `FreetimeTopBar`, `FreetimeNavigationItem`, `FreetimeBottomBar` and `FreetimeAdaptiveBottomBar`. Other controls include `FreetimeIconButton`, `FreetimeChip`, `FreetimeDialog`, `FreetimeSnackbar`, `FreetimeProgressIndicator` and `FreetimeSlider`.

Legacy `FreetimeGlassDepth` and `FreetimeDesignTokens` APIs remain temporarily available but are deprecated in favor of the typed `FreetimeDesign` token API.

## Material 3 and Material You

The `Design` module directly exposes Material 3. Core controls such as buttons, cards, switches, text fields, snackbars, sliders and progress indicators are backed by Material 3 while Liquid Glass is applied as the transparent visual surface where appropriate.

Dynamic Material You colors are enabled by default on Android 12 and newer. Devices below Android 12 use the standard Material 3 light/dark color schemes.

`FreetimeApp()` defaults to `AUTO_TIME`: light mode starts at **07:00** and dark mode starts at **19:00**. The time state is refreshed while the app stays open, and the hours can still be overridden through `FreetimeAppConfig` or persisted Freetime preferences.


## App-level building blocks

Common patterns found across GeoWeather, Luma Store and SuperSMP Companion are available as reusable Freetime components:

- `FreetimeText` — Freetime typography and palette-aware text.
- `FreetimeScreen` — lightweight top/content/bottom screen structure without Material Scaffold.
- `FreetimeSectionHeader` — title/subtitle header with an optional trailing slot.
- `FreetimeListItem` — reusable leading/content/trailing list row.
- `FreetimeOptionGroup` — settings and filter choices backed by Freetime choice rows.
- `FreetimeStatusBanner` — offline, cached-data, warning and informational state surface.
- `FreetimeEmptyState`, `FreetimeLoadingState`, `FreetimeErrorState` — consistent screen states.
- `FreetimeDivider` and `FreetimeBadge` — lightweight supporting primitives.
- `FreetimeInfoCard` — titled glass card for information and dashboard sections.

All higher-level components expose generic data or composable slots rather than depending on GeoWeather, Luma Store, SuperSMP, Navigation Compose or Material Icons.


## Layout and adaptive navigation

Version **1.9.0** adds Freetime-native layout primitives that avoid Material Scaffold:

- `FreetimeScaffold` for top/content/bottom/FAB screen structure.
- `FreetimeTabRow` with a Liquid Glass selection surface.
- `FreetimeFloatingActionButton` and `FreetimeExtendedFloatingActionButton`.
- `FreetimeBottomSheet` for floating sheet content.
- `FreetimeAdaptiveNavigation` to switch between bottom navigation and a navigation rail at a configurable width.
- `FreetimeNavigationRail` for larger screens.
- `FreetimePopupMenu` and `FreetimeMenuItem` for Material-free glass menus.
