# Freetime Design

Shared Material You and Liquid Glass UI for Freetime Android apps.

## Dependency

```kotlin
implementation("me.free-time:freetime-design:<version>")
```

## Theme

```kotlin
FreetimeTheme {
    FreetimeGlassRoot {
        AppContent()
    }
}
```

`FreetimeTheme` follows system dark mode by default and uses Android dynamic colors when available.

## Liquid Glass

The implementation follows GeoWeather's current glass system. Android 13+ uses Kyant Backdrop/Shapes with backdrop sampling, vibrancy, 12dp blur, 24dp lens distortion and spring press animation. Older versions use a Material color-aware fallback.

The root is important because the sampled backdrop and glass surfaces are kept in separate layers to avoid RuntimeShader feedback loops on affected GPUs.

### Components

```kotlin
FreetimeGlassCard {
    Text("Glass card")
}

FreetimeGlassButton("Continue", onClick = ::continueFlow)

FreetimeGlassNavigationBar {
    // NavigationBarItems
}
```

### Modifiers

```kotlin
Modifier.freetimeGlass()
Modifier.freetimeGlass(shape = RoundedCornerShape(32.dp))
Modifier.freetimeGlassCapsule()
```

Use `interactive = false` for non-pressable glass surfaces.
