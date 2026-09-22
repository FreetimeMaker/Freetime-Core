# Freetime Design

Freetime's standalone Compose UI system with Liquid Glass.

Since **1.6.0**, the module is independent from Material 3 as its UI foundation. Version **1.7.0** expanded it with reusable app-level building blocks shared across Freetime apps. Version **1.9.0** expands Liquid Glass and the reusable showcase surface. Freetime Design now owns its theme, palette, typography, shapes, spacing, sizing, motion and reusable controls.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.10.0")
```

## Theme

```kotlin
FreetimeTheme(
    darkTheme = isSystemInDarkTheme(),
    oledBlack = false,
) {
    FreetimeGlassRoot {
        AppContent()
    }
}
```

`FreetimeTheme` provides `FreetimePalette`, `FreetimeTypography`, `FreetimeShapes`, `FreetimeSpacing`, `FreetimeSizes`, `FreetimeMotion` and `FreetimeGlassTokens` through the `FreetimeDesign` API.

```kotlin
val spacing = FreetimeDesign.spacing.lg
val title = FreetimeDesign.typography.titleLarge
val foreground = FreetimeDesign.colors.contentStrong
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

## Material 3

The `Design` module does **not** depend on `androidx.compose.material3`. Applications can still use Material 3 alongside Freetime Design if they choose, but Freetime components themselves are implemented with Compose UI/Foundation and the Freetime design system.


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
