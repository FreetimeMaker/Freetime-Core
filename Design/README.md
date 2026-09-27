# Freetime Core Design

Material 3 Expressive, Material You and a focused Liquid Glass treatment for the floating bottom navigation.

Version 2.x removes the old Freetime-prefixed component system. Normal UI uses Material 3 Expressive directly. Liquid Glass is intentionally limited to the floating bottom navigation.

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

## Liquid Glass policy

Liquid Glass is used only by the floating bottom navigation.

Cards, buttons, text fields, dialogs, top bars, donation surfaces and normal content stay on regular Material 3 Expressive styling.

One boolean controls the bottom-navigation effect:

```kotlin
AppTheme(
    floatingBottomNavigationGlassEnabled = settings.floatingBottomNavigationGlassEnabled,
) {
    FloatingBottomNavigationGlassRoot(
        source = { AppBackground() },
    ) {
        NavigationBar(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .floatingBottomNavigationGlass(interactive = false),
            containerColor = Color.Transparent,
        ) {
            // NavigationBarItem(...)
        }
    }
}
```

The value is available through `LocalFloatingBottomNavigationGlassEnabled`.

Apps that already own their Material theme can provide only the navigation glass setting:

```kotlin
ProvideFloatingBottomNavigationGlass(enabled = settings.floatingBottomNavigationGlassEnabled) {
    AppContent()
}
```

When disabled, the floating navigation keeps its capsule shape and automatically uses the Material fallback.

## Glass optics

The floating navigation uses the shared SimpMusic-inspired recipe:

- backdrop vibrancy
- saturation **1.5**
- brightness **0.05**
- adaptive blur from **2dp to 16dp**
- refraction up to half the surface height
- adaptive scrim from **0.12 to 0.50**

There are no public generic Liquid Glass modifiers, containers, icon buttons, circle helpers or capsule helpers. The only public glass modifier is `Modifier.floatingBottomNavigationGlass()`.

The module currently pins `androidx.compose.material3:material3:1.5.0-alpha29` for the current Expressive APIs.
