# Freetime Core Design

Material 3 Expressive, Material You and reusable Liquid Glass for Freetime Android apps.

Version 2.0 removes the old Freetime-prefixed component system. Use Material 3 Expressive directly for normal UI and apply Liquid Glass only where the backdrop effect is useful.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:2.0.0")
```

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

## Global Liquid Glass setting

One boolean controls every shared glass surface:

```kotlin
AppTheme(
    liquidGlassEnabled = settings.liquidGlassEnabled,
) {
    LiquidGlassRoot {
        AppContent()
    }
}
```

The value is available through `LocalLiquidGlassEnabled`.

Apps that already own their Material theme can provide only the glass setting:

```kotlin
ProvideLiquidGlass(enabled = settings.liquidGlassEnabled) {
    AppContent()
}
```

When disabled, `Modifier.liquidGlass()` keeps the requested shape and automatically uses a Material `surfaceContainerHighest` fallback at 80% opacity.

## Glass optics

The shared Liquid Glass recipe uses:

- backdrop vibrancy
- saturation **1.5**
- brightness **0.05**
- adaptive blur from **2dp to 16dp**
- refraction up to half the surface height
- adaptive scrim from **0.12 to 0.50**
- pointer-following press glow
- spring press interaction
- **1.12x** press bulge for compact controls
- **1.04x** press bulge for wide surfaces

## Usage

Keep the sampled background source and glass foreground separate:

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

Use Material 3 Expressive for buttons, cards, text fields, navigation, dialogs, switches, sliders and other normal UI components. The module currently pins `androidx.compose.material3:material3:1.5.0-alpha29` for the current Expressive APIs.
