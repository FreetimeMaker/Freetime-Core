# Freetime Core Design

Material 3 Expressive, Material You and a focused Liquid Glass treatment for the floating bottom navigation.

Version 3.x exposes Liquid Glass only through the dedicated floating bottom navigation. Normal UI uses Material 3 Expressive directly.

## Dependency

```kotlin
implementation("com.github.FreetimeMaker.Freetime-Core:Design:3.0.0")
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

The module currently pins `androidx.compose.material3:material3:1.5.0-alpha29` for the current Expressive APIs.
