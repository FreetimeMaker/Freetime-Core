# Material 3 and Expressive glass components

Package: `com.freetime.design.templates.fullglass`.

This is an opt-in, state-hoisted component toolkit for Compose Multiplatform Material 3 **1.10.0-alpha05**, shared by the Material 3 and Expressive templates on Android and desktop JVM. It reuses native controls for input, focus, keyboard navigation, selection semantics and animation, replacing appropriate container fills with a refracting glass surface. It does not intercept or replace ordinary `androidx.compose.material3` calls.

## Open the interactive catalog

```kotlin
com.freetime.design.templates.material3glass.FreetimeTheme {
    LiquidGlassCatalog()
}
```

For Expressive motion and the additional Expressive samples:

```kotlin
com.freetime.design.templates.expressiveglass.FreetimeTheme {
    LiquidGlassCatalog(expressive = true)
}
```

`LiquidGlassCatalog.kt` contains light/dark previews for both themes. The catalog has editable inputs, real selection and slider state, actionable buttons, search results, native dialogs and sheets, and Expressive carousel/button-group examples. Navigation and tabs change selection only; they never navigate away. The gallery is illustrative, not a runtime or accessibility test suite, and does not display every API variant.

## Use the components in an app

Apply the app's Material theme, then place a `LiquidGlassRoot` around the relevant screen. Its `source` draws only the background; its `content` draws the controls as a sibling, not as part of their own recorded source. A colorful gradient or artwork makes refraction visible; an empty or flat source cannot create detail to refract.

```kotlin
FreetimeTheme {
    val colors = MaterialTheme.colorScheme
    var checked by rememberSaveable { mutableStateOf(false) }
    LiquidGlassRoot(
        source = {
            Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(colors.primaryContainer, colors.tertiaryContainer))
                )
            )
        },
    ) {
        Column(Modifier.safeDrawingPadding().padding(24.dp)) {
            LiquidGlassButton(onClick = { checked = !checked }) {
                Text(if (checked) "Enabled" else "Enable")
            }
            LiquidGlassSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
                controlModifier = Modifier.semantics { contentDescription = "Enable feature" },
            )
        }
    }
}
```

The example uses standard Compose imports plus the chosen template's `FreetimeTheme` and this package's components. To copy sources into an app, copy this shared package and the selected theme package, then update all package declarations/imports. The optional `LiquidGlassCatalog.kt` imports both template themes for its previews: omit that file, adapt its previews, or copy both theme packages. The Design/backdrop dependencies remain necessary. A normal Material theme also works; surface tint follows the current color scheme.

Do not put `LiquidGlassSurface` or any glass component inside the `source` that it reads. This creates a render-feedback loop. The root does not automatically provide the separate floating-navigation backdrop; use `FullGlassScreen` or `FloatingBottomNavigationGlassRoot` when using the library's `FloatingBottomNavigationBar`.

## Implemented families

Function names below have the `LiquidGlass` prefix unless stated otherwise.

- **Foundations:** `Root`, `Surface`, `OverlaySurface`, and `Catalog`.
- **Actions:** `Button` (text and content-slot overloads), `ElevatedButton`, `FilledTonalButton`, `OutlinedButton`, `TextButton`.
- **Icon actions:** `IconButton`, `IconToggleButton`, `FilledIconButton`, `FilledTonalIconButton`, `OutlinedIconButton`, `FilledIconToggleButton`, `FilledTonalIconToggleButton`, `OutlinedIconToggleButton`.
- **Floating actions:** `FloatingActionButton`, `SmallFloatingActionButton`, `LargeFloatingActionButton`, `ExtendedFloatingActionButton`.
- **Cards:** `Card` (passive or clickable), `ElevatedCard`, `OutlinedCard`.
- **Selection:** `Checkbox`, `TriStateCheckbox`, `RadioButton`, `Switch`, `Slider`, `RangeSlider`, `SingleChoiceSegmentedButtonRow`, `MultiChoiceSegmentedButtonRow`.
- **Chips:** `AssistChip`, `FilterChip`, `InputChip`, `SuggestionChip`.
- **Input and search:** `TextField` (string-label and configurable composable-label overloads), `OutlinedTextField`, `SearchBar`, `DockedSearchBar`.
- **Content:** `ListItem`, `Badge`, `BadgedBox`, `HorizontalDivider`, `VerticalDivider`.
- **Progress:** `LinearProgressIndicator`, `CircularProgressIndicator` (both determinate and indeterminate).
- **App bars:** `TopAppBar`, `CenterAlignedTopAppBar`, `MediumTopAppBar`, `LargeTopAppBar`, `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `BottomAppBar`.
- **Navigation:** `NavigationBar`, `ShortNavigationBar`, `NavigationRail`, `WideNavigationRail`, `NavigationDrawerSheet`, `PermanentDrawerSheet`, `DismissibleDrawerSheet`, `PrimaryTabRow`, `SecondaryTabRow`, `PrimaryScrollableTabRow`, `SecondaryScrollableTabRow`. Native navigation items and tabs are supplied through the slots; callers own selection and destinations.
- **Overlays:** `AlertDialog`, `ModalBottomSheet`, `DropdownMenu`, `DropdownMenuItem`, `Tooltip`, `RichTooltip`, `Snackbar`.
- **Pickers:** `DatePicker`, `DateRangePicker`, `TimePicker`, `TimeInput`.
- **Expressive:** `ToggleButton`, `ButtonGroup`, `SplitButton`, `LoadingIndicator`, `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator`, `HorizontalFloatingToolbar`, `VerticalFloatingToolbar`, `HorizontalMultiBrowseCarousel`, `HorizontalUncontainedCarousel`.
- **Layouts and gestures:** `Scaffold`, `BottomSheetScaffold`, `SwipeToDismissBox`, `PullToRefreshBox`. These delegate layout/gestures to Material; glass is applied only to appropriate slots or moving surfaces, never as a blur over the whole viewport.

## Behavior and boundaries

- Native foregrounds, focus/error borders, checkmarks, selection fills, thumbs and progress tracks intentionally remain visible Material paint. A glass container is not a fully refractive replacement for every drawn pixel. Filled/tonal variants can look more similar when their opaque fill is removed. Elevated glass cards intentionally do not add a shadow that would show through the transparent surface.
- These are adapters, not API-for-API replacements for every Material overload or future component. Native child slots retain their own styling unless callers choose transparent colors. Helpers, defaults, state objects, shapes, icons and typography remain the original Compose APIs. For example, segmented rows take native `SegmentedButton` children and the split layout takes native leading/trailing buttons.
- Not every search-window variant or component overload has a dedicated adapter. For an additional inline native component, use `LiquidGlassSurface` with a transparent native container. Separately rooted windows require their own source or the explicit safe fallback, not the page's backdrop.
- Dialogs, modal sheets, dropdowns and tooltips are separate windows. `LiquidGlassOverlaySurface` deliberately clears the window-local backdrop and supplies an **opaque Material fallback**, including to nested controls. These overlays do **not** blur or refract the page behind them. Inline date/time pickers and persistent sheets can sample the page. Cross-window glass needs its own per-window source and is not implemented.
- A rootless surface also falls back to an opaque Material surface. Keep readable foregrounds and test contrast over the actual artwork/background.
- `modifier` configures the outer glass. Where available, `controlModifier`, `carouselModifier`, and related parameters configure native content. Minimum sizing constraints propagate to children so a sized action's native click target fills its glass. Keep labels/content descriptions meaningful; group radio options and segmented choices properly.
- State, persistence, navigation, menu dismissal, snackbar scheduling, sheet hiding, and picker confirmation belong to callers. Segmented-row wrappers do not enforce exclusivity for supplied children. Use a `SnackbarHost` to retain timeout and announcement behavior. Hide a sheet before removing it for an explicit close action.
- Constrain picker, carousel and rail heights when embedding them in scrollable content. Keep expanding search outside a parent that cannot accommodate its expansion. Do not nest vertically unbounded scrolling containers. Dialog bodies scroll while action buttons remain outside the scrolling region.
- Surface shapes clip their children. Match shape/geometry deliberately when supplying animated native children. Apply toolbar motion to the whole glass wrapper, not independently to a transparent toolbar inside it.
- Refraction is shader-based. Review supported Android versions, reduced-motion needs, large fonts, touch/keyboard/TalkBack behavior and GPU cost on real devices. Avoid rendering an entire large dataset as live glass at once; use lazy lists and a modest visible surface count.

## Validation

```sh
sh gradlew :Design:checkTemplates :Design:assembleDebug :Design:testDebugUnitTest
```

`checkTemplates` compiles the real library sources and previews, not copies. Compilation and assembly do not prove visual appearance or gesture/accessibility correctness. The Design module currently has no unit or instrumentation test sources; the unit-test task is not evidence of UI coverage. Use the four catalog previews and real devices for visual/runtime review.
