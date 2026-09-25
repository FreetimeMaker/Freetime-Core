# Design

The Freetime Core Design module is now a thin **Material 3 / Material You + Liquid Glass** layer.

The old Freetime-prefixed design system has been removed. There are no custom `FreetimeButton`, `FreetimeCard`, `FreetimeTheme`, `FreetimeDesign`, typography, shape, spacing or layout wrappers anymore. Use Material 3 directly and apply Liquid Glass only where it improves the surface.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:1.11.0")
```

## Theme

```kotlin
AppTheme {
    AppContent()
}
```

`AppTheme` uses Material You dynamic colors on Android 12+ and regular Material 3 schemes below Android 12.

The default time mode is:

- light from **07:00**
- dark from **19:00**

You can still select `SYSTEM`, `LIGHT`, `DARK`, `OLED` or `AUTO_TIME`.

## Global Liquid Glass setting

Liquid Glass follows the same architecture as SimpMusic: one boolean is provided at the app theme and every glass surface reads it from the shared composition local.

```kotlin
AppTheme(
    liquidGlassEnabled = settings.liquidGlassEnabled,
) {
    LiquidGlassRoot {
        AppContent()
    }
}
```

The global value is available as `LocalLiquidGlassEnabled`. Apps that already own their Material theme can use:

```kotlin
ProvideLiquidGlass(enabled = settings.liquidGlassEnabled) {
    AppContent()
}
```

When the setting is disabled, `Modifier.liquidGlass()` automatically keeps the same shape and switches to a flat Material 3 `surfaceContainerHighest` fallback at 80% opacity. Call sites do not need their own `if (liquidGlassEnabled)` branches.

## Glass optics

The shared recipe follows SimpMusic's current Liquid Glass behavior:

- backdrop vibrancy
- saturation **1.5**
- brightness **0.05**
- adaptive blur from **2dp to 16dp**, centered around 8dp
- refraction up to half the surface height
- adaptive surface scrim from **0.12 to 0.50**
- press glow following the pointer
- spring press interaction
- **1.12x** press bulge for compact controls
- **1.04x** press bulge for wide surfaces

## Usage

Keep the sampled source and glass foreground separate:

```kotlin
LiquidGlassRoot(
    source = {
        ArtworkOrGradient()
    },
) {
    Button(
        onClick = ::continueFlow,
        modifier = Modifier.liquidGlassCapsule(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Text("Continue")
    }
}
```

Available primitives:

```kotlin
Modifier.liquidGlass()
Modifier.liquidGlassCapsule()
Modifier.liquidGlassCircle()
LiquidGlassContainer(...)
LiquidGlassIconButton(...)
rememberLiquidGlassBackdrop()
Modifier.liquidGlassSource(...)
```

Use Material 3 for buttons, cards, text fields, navigation, dialogs, switches, sliders and every other normal UI component. Liquid Glass is an effect layer, not a second design system.
