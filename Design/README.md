# Freetime Design

Freetime's standalone Compose UI system with Liquid Glass.

Version **1.6.0** moves the module away from Material 3 as its UI foundation. Freetime Design now owns its theme, palette, typography, shapes, spacing, sizing, motion and reusable controls.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.6.0")
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
