@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.freetime.design.templates.fullglass

import com.freetime.design.platformDateLabel
import com.freetime.design.platformTimeLabel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.freetime.design.templates.material3glass.FreetimeTheme as M3GlassTheme
import com.freetime.design.templates.expressiveglass.FreetimeTheme as ExpressiveGlassTheme

/** Interactive wrapper catalog. The caller supplies the Material or Expressive glass theme. */
@Composable
fun LiquidGlassCatalog(modifier: Modifier = Modifier, expressive: Boolean = false) {
    var count by rememberSaveable { mutableIntStateOf(0) }
    var message by rememberSaveable { mutableStateOf("Try a control to see its state change.") }
    var favorite by rememberSaveable { mutableStateOf(false) }
    var checked by rememberSaveable { mutableStateOf(true) }
    var notifications by rememberSaveable { mutableStateOf(false) }
    var triState by rememberSaveable { mutableIntStateOf(1) }
    var radio by rememberSaveable { mutableIntStateOf(0) }
    var filter by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf(false) }
    var segment by rememberSaveable { mutableIntStateOf(0) }
    var multi by rememberSaveable { mutableIntStateOf(1) }
    var progress by rememberSaveable { mutableFloatStateOf(0.4f) }
    var rangeStart by rememberSaveable { mutableFloatStateOf(0.2f) }
    var rangeEnd by rememberSaveable { mutableFloatStateOf(0.8f) }
    var destination by rememberSaveable { mutableIntStateOf(0) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var secondaryTab by rememberSaveable { mutableIntStateOf(0) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var sheet by rememberSaveable { mutableStateOf(false) }
    var menu by rememberSaveable { mutableStateOf(false) }
    var picker by rememberSaveable { mutableStateOf("") }
    var dateLabel by rememberSaveable { mutableStateOf("No date selected") }
    var timeLabel by rememberSaveable { mutableStateOf("No time selected") }
    val dateState = rememberDatePickerState()
    val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 30)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val destinations = listOf("Home", "Explore", "Saved")
    val add: () -> Unit = { count++; message = "Added item $count" }
    val background = MaterialTheme.colorScheme.background

    LiquidGlassRoot(
        modifier = modifier.fillMaxSize(),
        backgroundColor = background,
        source = {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.linearGradient(listOf(
                    Color(0xFF315AA6), Color(0xFF8758AC), Color(0xFF279DAB),
                )))
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFFB879), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.2f),
                        radius = size.maxDimension * 0.65f,
                    ),
                    radius = size.maxDimension * 0.65f,
                    center = Offset(size.width * 0.9f, size.height * 0.2f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFB0F4DD), Color.Transparent),
                        center = Offset(0f, size.height * 0.85f),
                        radius = size.maxDimension * 0.55f,
                    ),
                    radius = size.maxDimension * 0.55f,
                    center = Offset(0f, size.height * 0.85f),
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Liquid glass", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        if (expressive) "Expressive • live component playground"
                        else "Material 3 • live component playground",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text("Scroll, select, and open real overlays. The colorful source stays behind the sharp controls.")
                    Text("$count items • ${if (favorite) "Favorite" else "Not favorited"}")
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
            }

            CatalogSection("Actions", "Native buttons, icon controls, and floating actions") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidGlassButton(onClick = add) { Text("Add item") }
                    LiquidGlassElevatedButton(onClick = { count++; message = "Elevated action added an item" }) { Text("Elevated") }
                    LiquidGlassFilledTonalButton(onClick = { favorite = !favorite }) { Text(if (favorite) "Unfavorite" else "Favorite") }
                    LiquidGlassOutlinedButton(onClick = { count = 0; message = "Counter reset" }) { Text("Reset count") }
                    LiquidGlassTextButton(onClick = { message = "Text action selected" }) { Text("Text action") }
                    LiquidGlassButton(onClick = add, enabled = false) { Text("Disabled") }
                    LiquidGlassIconButton(onClick = add) { CatalogSymbol("+", "Add one item") }
                    LiquidGlassIconToggleButton(checked = favorite, onCheckedChange = { favorite = it }) {
                        CatalogSymbol(if (favorite) "★" else "☆", "Favorite item")
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidGlassSmallFloatingActionButton(onClick = add) { CatalogSymbol("+", "Add with small floating action") }
                    LiquidGlassFloatingActionButton(onClick = add) { CatalogSymbol("+", "Add with floating action") }
                    LiquidGlassLargeFloatingActionButton(onClick = add) { CatalogSymbol("+", "Add with large floating action") }
                    LiquidGlassExtendedFloatingActionButton(onClick = add) { Text("Add to collection") }
                }
            }

            CatalogSection("Cards", "Tap each card to update the collection") {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), onClick = add) {
                    Text("Standard card • $count items", Modifier.padding(16.dp))
                }
                LiquidGlassElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = { favorite = !favorite }) {
                    Text("Elevated card • ${if (favorite) "favorited" else "tap to favorite"}", Modifier.padding(16.dp))
                }
                LiquidGlassOutlinedCard(modifier = Modifier.fillMaxWidth(), onClick = { message = "Outlined card selected" }) {
                    Text("Outlined card • tap to select", Modifier.padding(16.dp))
                }
            }

            CatalogSection("Selection", "State belongs to this demo, not to the wrappers") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassCheckbox(checked, { checked = it }, controlModifier = catalogLabel("Include artwork"))
                        Text("Artwork: $checked", style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassTriStateCheckbox(
                            state = when (triState) { 0 -> ToggleableState.Off; 1 -> ToggleableState.Indeterminate; else -> ToggleableState.On },
                            onClick = { triState = (triState + 1) % 3 },
                            controlModifier = catalogLabel("Cycle collection selection"),
                        )
                        Text(listOf("None", "Some", "All")[triState], style = MaterialTheme.typography.labelMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LiquidGlassSwitch(notifications, { notifications = it }, controlModifier = catalogLabel("Enable notifications"))
                        Text("Alerts: ${if (notifications) "on" else "off"}", style = MaterialTheme.typography.labelMedium)
                    }
                }
                FlowRow(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf("Personal", "Shared").forEachIndexed { index, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiquidGlassRadioButton(radio == index, { radio = index }, controlModifier = catalogLabel("$label collection"))
                            Text(label)
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidGlassAssistChip(onClick = { message = "Assist chip added a reminder" }, label = { Text("Add reminder") })
                    LiquidGlassFilterChip(filter, { filter = !filter }, { Text(if (filter) "Favorites only" else "All items") })
                    LiquidGlassInputChip(input, { input = !input }, { Text(if (input) "Jamie selected" else "Select Jamie") })
                    LiquidGlassSuggestionChip(onClick = { message = "Suggestion accepted: weekend collection" }, label = { Text("Try a weekend collection") })
                }
                LiquidGlassSingleChoiceSegmentedButtonRow {
                    listOf("Day", "Week", "Month").forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = segment == index, onClick = { segment = index },
                            shape = SegmentedButtonDefaults.itemShape(index, 3),
                        ) { Text(label) }
                    }
                }
                Text("View: ${listOf("Day", "Week", "Month")[segment]}")
                LiquidGlassMultiChoiceSegmentedButtonRow {
                    listOf("Audio", "Video").forEachIndexed { index, label ->
                        val bit = 1 shl index
                        SegmentedButton(
                            checked = multi and bit != 0,
                            onCheckedChange = { selected -> multi = if (selected) multi or bit else multi and bit.inv() },
                            shape = SegmentedButtonDefaults.itemShape(index, 2),
                        ) { Text(label) }
                    }
                }
                Text("Media: ${listOf("Audio", "Video").filterIndexed { index, _ -> multi and (1 shl index) != 0 }.joinToString().ifEmpty { "none" }}")
            }

            CatalogInputs(onAction = { message = it })

            CatalogSection("Sliders & progress", "Drag a real thumb; the progress samples follow it") {
                Text("Progress: ${(progress * 100).toInt()}%")
                LiquidGlassSlider(progress, { progress = it }, modifier = Modifier.fillMaxWidth(), controlModifier = Modifier.fillMaxWidth().then(catalogLabel("Demo progress")))
                Text("Range: ${(rangeStart * 100).toInt()}–${(rangeEnd * 100).toInt()}%")
                LiquidGlassRangeSlider(
                    rangeStart..rangeEnd, { rangeStart = it.start; rangeEnd = it.endInclusive },
                    modifier = Modifier.fillMaxWidth(), controlModifier = Modifier.fillMaxWidth().then(catalogLabel("Collection range")),
                )
                LiquidGlassLinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), controlModifier = Modifier.fillMaxWidth())
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LiquidGlassCircularProgressIndicator(progress = { progress }, controlModifier = catalogLabel("Demo completion"))
                    LiquidGlassCircularProgressIndicator(controlModifier = catalogLabel("Indeterminate progress sample"))
                }
            }

            CatalogSection("Navigation", "Highlight-only samples: selections change indicators, never page content") {
                LiquidGlassTopAppBar(
                    title = { Text("Collection") }, windowInsets = WindowInsets(0, 0, 0, 0),
                    actions = { IconButton(onClick = add) { CatalogSymbol("+", "Add item from top bar") } },
                )
                LiquidGlassNavigationBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
                    destinations.forEachIndexed { index, label ->
                        NavigationBarItem(selected = destination == index, onClick = { destination = index },
                            icon = { CatalogSymbol(listOf("⌂", "◎", "☆")[index], label) }, label = { Text(label) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().height(260.dp), verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassNavigationRail(windowInsets = WindowInsets(0, 0, 0, 0)) {
                        destinations.forEachIndexed { index, label ->
                            NavigationRailItem(selected = destination == index, onClick = { destination = index },
                                icon = { CatalogSymbol(listOf("⌂", "◎", "☆")[index], label) }, label = { Text(label) })
                        }
                    }
                    Text("Navigation rail\nSelection is shared with the bar above.", Modifier.weight(1f).padding(16.dp))
                }
                LiquidGlassPrimaryTabRow(selectedTabIndex = tab) {
                    listOf("Collection", "Activity").forEachIndexed { index, label ->
                        Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                    }
                }
                Text("Primary tabs demonstrate selection highlights only.")
                LiquidGlassSecondaryTabRow(selectedTabIndex = secondaryTab) {
                    listOf("Recent", "Popular").forEachIndexed { index, label ->
                        Tab(selected = secondaryTab == index, onClick = { secondaryTab = index }, text = { Text(label) })
                    }
                }
                Text("Secondary tabs demonstrate selection highlights only.")
                LiquidGlassBottomAppBar(windowInsets = WindowInsets(0, 0, 0, 0)) {
                    IconButton(onClick = add) { CatalogSymbol("+", "Add item from bottom bar") }
                    Text("$count items", Modifier.weight(1f))
                    TextButton(onClick = { count = 0; message = "Bottom bar cleared the collection" }) { Text("Clear") }
                }
            }

            if (expressive) {
                CatalogExpressive(progress = progress, onAction = { message = it })
            }

            CatalogSection("Overlays", "Dialogs, sheets, menus, and tooltips use an opaque Material fallback: separate windows cannot sample the page backdrop.") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidGlassButton(onClick = { dialog = true }) { Text("Open dialog") }
                    LiquidGlassFilledTonalButton(onClick = { sheet = true }) { Text("Open sheet") }
                    Box {
                        LiquidGlassOutlinedButton(onClick = { menu = true }) { Text("Open menu") }
                        LiquidGlassDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            listOf("Sort by name", "Sort by date").forEach { choice ->
                                LiquidGlassDropdownMenuItem(text = { Text(choice) }, onClick = { message = choice; menu = false })
                            }
                        }
                    }
                    LiquidGlassTextButton(onClick = { picker = "date" }) { Text("Choose date") }
                    LiquidGlassTextButton(onClick = { picker = "time" }) { Text("Choose time") }
                    LiquidGlassOutlinedButton(onClick = {
                        scope.launch {
                            val result = snackbar.showSnackbar("Collection saved", actionLabel = "Undo", withDismissAction = true)
                            message = if (result == SnackbarResult.ActionPerformed) "Save undone" else "Save notification dismissed"
                        }
                    }) { Text("Show snackbar") }
                    LiquidGlassTooltip(state = rememberTooltipState(), tooltip = { Text("Adds one item. Long-press or hover to read this tip.") }) {
                        LiquidGlassIconButton(onClick = add) { CatalogSymbol("+", "Add item; hold for help") }
                    }
                }
                Text("$dateLabel • $timeLabel")
            }
            Text("Scope: drawers and additional app-bar variants are not shown here.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp))
        }
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter).safeDrawingPadding().padding(16.dp)) {
            LiquidGlassSnackbar(it)
        }

        if (dialog) {
            LiquidGlassAlertDialog(
                onDismissRequest = { dialog = false }, title = { Text("Add to your collection?") },
                text = { Text("You currently have $count items. Confirm adds one more; cancel leaves the collection unchanged.") },
                confirmButton = { TextButton(onClick = { add(); dialog = false }) { Text("Add item") } },
                dismissButton = { TextButton(onClick = { dialog = false; message = "Dialog cancelled" }) { Text("Cancel") } },
            )
        }
        if (sheet) {
            LiquidGlassModalBottomSheet(onDismissRequest = { sheet = false }, sheetState = sheetState) {
                Text("Collection quick actions", style = MaterialTheme.typography.headlineSmall)
                Text("$count items • ${if (favorite) "favorited" else "not favorited"}", Modifier.padding(vertical = 16.dp))
                Button(onClick = { add() }) { Text("Add item ($count)") }
                TextButton(onClick = {
                    scope.launch { sheetState.hide(); if (!sheetState.isVisible) sheet = false }
                }) { Text("Done") }
            }
        }
        if (picker.isNotEmpty()) {
            LiquidGlassAlertDialog(
                onDismissRequest = { picker = "" },
                title = { Text(if (picker == "date") "Choose a date" else "Choose a time") },
                text = {
                    // The calendar and clock must not inherit the catalog's unbounded scroll height.
                    Box(Modifier.fillMaxWidth().heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                        if (picker == "date") {
                            LiquidGlassDatePicker(dateState, modifier = Modifier.fillMaxWidth().heightIn(min = 520.dp))
                        } else {
                            LiquidGlassTimePicker(timeState, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp))
                        }
                    }
                },
                confirmButton = {
                    TextButton(enabled = picker != "date" || dateState.selectedDateMillis != null, onClick = {
                        if (picker == "date") {
                            dateState.selectedDateMillis?.let { millis ->
                                dateLabel = platformDateLabel(millis)
                                message = "Date selected: $dateLabel"
                            }
                        } else {
                            timeLabel = platformTimeLabel(timeState.hour, timeState.minute)
                            message = "Time selected: $timeLabel"
                        }
                        picker = ""
                    }) { Text("Use selection") }
                },
                dismissButton = { TextButton(onClick = { picker = "" }) { Text("Cancel") } },
            )
        }
    }
}

@Composable
private fun CatalogSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    LiquidGlassSurface(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            content()
        }
    }
}

@Composable
private fun CatalogInputs(onAction: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("Weekend collection") }
    var description by rememberSaveable { mutableStateOf("A little room for downtime.") }
    var savedName by rememberSaveable { mutableStateOf("Weekend collection") }
    var savedDescription by rememberSaveable { mutableStateOf("A little room for downtime.") }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var chosenResult by rememberSaveable { mutableStateOf("") }
    var openedCount by rememberSaveable { mutableIntStateOf(0) }
    val searchState = rememberTextFieldState()
    val focusManager = LocalFocusManager.current
    val collections = listOf("Weekend collection", "Quiet hours", "Daily favorites", "On the move", "New discoveries")
    val query = searchState.text.toString().trim()
    val results = collections.filter { it.contains(query, ignoreCase = true) }

    CatalogSection("Inputs & content", "Edit a collection, save its details, or search the sample library") {
        LiquidGlassTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Collection name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = name.isBlank(),
            supportingText = { Text(if (name.isBlank()) "A name is required to save." else "Your draft is kept while exploring the catalog.") },
            trailingIcon = {
                IconButton(onClick = { name = "" }, enabled = name.isNotEmpty()) {
                    CatalogSymbol("×", "Clear collection name")
                }
            },
        )
        LiquidGlassOutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
            supportingText = { Text("${description.length} characters") },
        )
        LiquidGlassButton(
            onClick = {
                savedName = name.trim()
                savedDescription = description.trim()
                focusManager.clearFocus()
                onAction("Saved collection: $savedName")
            },
            enabled = name.isNotBlank(),
        ) { Text("Save details") }
        LiquidGlassHorizontalDivider(modifier = Modifier.fillMaxWidth())
        LiquidGlassListItem(
            headlineContent = { Text(savedName) },
            overlineContent = { Text("Saved collection") },
            supportingContent = { Text(savedDescription.ifBlank { "No description" }) },
            leadingContent = {
                LiquidGlassBadgedBox(
                    badge = {
                        LiquidGlassBadge(controlModifier = catalogLabel("Opened $openedCount times")) {
                            Text(openedCount.toString())
                        }
                    },
                ) { CatalogSymbol("▤", "Collection") }
            },
            trailingContent = { Text("Open") },
            controlModifier = Modifier.fillMaxWidth().clickable(
                role = Role.Button,
                onClickLabel = "Open saved collection",
                onClick = { openedCount++; onAction("Opened $savedName ($openedCount times)") },
            ),
        )
        Text("Opened $openedCount times", style = MaterialTheme.typography.labelMedium)
        LiquidGlassHorizontalDivider(modifier = Modifier.fillMaxWidth())
        Text("Search the sample library", style = MaterialTheme.typography.titleMedium)
        LiquidGlassDockedSearchBar(
            state = searchState,
            onSearch = {
                searchExpanded = true
                focusManager.clearFocus()
                onAction("Search: ${it.ifBlank { "all collections" }} • ${results.size} results")
            },
            expanded = searchExpanded,
            onExpandedChange = { searchExpanded = it },
            modifier = Modifier.fillMaxWidth(),
            controlModifier = Modifier.fillMaxWidth(),
            inputModifier = catalogLabel("Search sample collections"),
            placeholder = { Text("Search collections") },
            trailingIcon = {
                IconButton(onClick = {
                    searchExpanded = !searchExpanded
                    if (!searchExpanded) focusManager.clearFocus()
                }) {
                    CatalogSymbol(if (searchExpanded) "×" else "⌕", if (searchExpanded) "Close search results" else "Show search results")
                }
            },
        ) {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (results.isEmpty()) {
                    Text("No collections match this search.", Modifier.padding(16.dp))
                }
                results.forEach { result ->
                    LiquidGlassListItem(
                        headlineContent = { Text(result) },
                        supportingContent = { Text("Use this collection name") },
                        controlModifier = Modifier.fillMaxWidth().clickable(
                            role = Role.Button,
                            onClickLabel = "Use $result",
                            onClick = {
                                name = result
                                chosenResult = result
                                searchExpanded = false
                                focusManager.clearFocus()
                                onAction("Search selected: $result; draft name updated")
                            },
                        ),
                    )
                }
            }
        }
        Text(if (chosenResult.isEmpty()) "Choose a result to fill the name draft." else "Last chosen: $chosenResult")
    }
}

private fun catalogLabel(label: String): Modifier = Modifier.semantics { contentDescription = label }

@Composable
private fun CatalogSymbol(symbol: String, label: String) {
    Text(symbol, modifier = catalogLabel(label), style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun CatalogExpressive(progress: Float, onAction: (String) -> Unit) {
    var toggled by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(true) }
    var splitExpanded by rememberSaveable { mutableStateOf(false) }
    var pinned by rememberSaveable { mutableStateOf(false) }
    var selectedCollection by rememberSaveable { mutableStateOf("None") }
    val collections = listOf("Weekend", "Quiet hours", "On the move", "Discoveries")
    val multiBrowseState = rememberCarouselState { collections.size }
    val uncontainedState = rememberCarouselState { collections.size }
    CatalogSection("Expressive", "Native wavy motion, morphing loading, toggles, and floating toolbars") {
        LiquidGlassToggleButton(checked = toggled, onCheckedChange = { toggled = it; onAction("Expressive toggle: $it") }) {
            Text(if (toggled) "Following" else "Follow collection")
        }
        LiquidGlassLinearWavyProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LiquidGlassCircularWavyProgressIndicator(progress = { progress })
            LiquidGlassCircularWavyProgressIndicator()
            LiquidGlassLoadingIndicator(modifier = catalogLabel("Expressive loading sample"))
        }
        LiquidGlassSplitButton(
            leadingButton = { SplitButtonDefaults.LeadingButton(onClick = { onAction("Split button saved the collection") }) { Text("Save") } },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(checked = splitExpanded, onCheckedChange = { splitExpanded = it }) {
                    CatalogSymbol(if (splitExpanded) "−" else "+", "Toggle save options")
                }
            },
        )
        if (splitExpanded) {
            Text("Save options")
            LiquidGlassTextButton(onClick = { onAction("Saved a copy of the collection"); splitExpanded = false }) { Text("Save a copy") }
        }
        Text("Button group", style = MaterialTheme.typography.titleMedium)
        LiquidGlassButtonGroup(
            modifier = Modifier.fillMaxWidth(),
            overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        ) {
            clickableItem(onClick = { onAction("Button group saved the collection") }, label = "Save")
            toggleableItem(checked = pinned, label = "Pin", onCheckedChange = {
                pinned = it
                onAction(if (it) "Collection pinned" else "Collection unpinned")
            })
            clickableItem(onClick = { onAction("Button group created a collection copy") }, label = "Make a copy")
        }
        Text(if (pinned) "Collection is pinned" else "Collection is not pinned")
        Text("Browse collections", style = MaterialTheme.typography.titleMedium)
        Text("Swipe either carousel, or tap a card. Selected: $selectedCollection")
        LiquidGlassHorizontalMultiBrowseCarousel(
            state = multiBrowseState,
            preferredItemWidth = 200.dp,
            modifier = Modifier.fillMaxWidth(),
            carouselModifier = Modifier.fillMaxWidth().height(148.dp),
            itemSpacing = 8.dp,
        ) { index ->
            Card(
                onClick = { selectedCollection = collections[index]; onAction("Selected ${collections[index]}") },
                modifier = Modifier.fillMaxSize().maskClip(RoundedCornerShape(24.dp)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(collections[index], style = MaterialTheme.typography.titleMedium)
                    Text(if (selectedCollection == collections[index]) "Selected" else "Tap to select")
                }
            }
        }
        Text("Uncontained carousel", style = MaterialTheme.typography.titleMedium)
        LiquidGlassHorizontalUncontainedCarousel(
            state = uncontainedState,
            itemWidth = 180.dp,
            modifier = Modifier.fillMaxWidth(),
            carouselModifier = Modifier.fillMaxWidth().height(120.dp),
            itemSpacing = 8.dp,
        ) { index ->
            Card(
                onClick = { selectedCollection = collections[index]; onAction("Selected ${collections[index]}") },
                modifier = Modifier.fillMaxSize().maskClip(RoundedCornerShape(24.dp)),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(collections[index], style = MaterialTheme.typography.titleMedium)
                    Text(if (selectedCollection == collections[index]) "Selected" else "Tap to select")
                }
            }
        }
        LiquidGlassAssistChip(onClick = { expanded = !expanded }, label = { Text(if (expanded) "Collapse toolbars" else "Expand toolbars") })
        LiquidGlassHorizontalFloatingToolbar(
            expanded = expanded,
            trailingContent = { IconButton(onClick = { onAction("Horizontal toolbar: favorite") }) { CatalogSymbol("☆", "Favorite from horizontal toolbar") } },
        ) {
            IconButton(onClick = { onAction("Horizontal toolbar: edit") }) { CatalogSymbol("✎", "Edit from horizontal toolbar") }
        }
        LiquidGlassVerticalFloatingToolbar(
            expanded = expanded,
            trailingContent = { IconButton(onClick = { onAction("Vertical toolbar: favorite") }) { CatalogSymbol("☆", "Favorite from vertical toolbar") } },
        ) {
            IconButton(onClick = { onAction("Vertical toolbar: edit") }) { CatalogSymbol("✎", "Edit from vertical toolbar") }
        }
    }
}

@Preview(name = "Liquid glass · M3 light", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LiquidGlassCatalogM3LightPreview() {
    M3GlassTheme(darkTheme = false, dynamicColor = false) { LiquidGlassCatalog() }
}

@Preview(name = "Liquid glass · M3 dark", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LiquidGlassCatalogM3DarkPreview() {
    M3GlassTheme(darkTheme = true, dynamicColor = false) { LiquidGlassCatalog() }
}

@Preview(name = "Liquid glass · Expressive light", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LiquidGlassCatalogExpressiveLightPreview() {
    ExpressiveGlassTheme(darkTheme = false, dynamicColor = false) { LiquidGlassCatalog(expressive = true) }
}

@Preview(name = "Liquid glass · Expressive dark", showBackground = true, widthDp = 412, heightDp = 915)
@Composable
private fun LiquidGlassCatalogExpressiveDarkPreview() {
    ExpressiveGlassTheme(darkTheme = true, dynamicColor = false) { LiquidGlassCatalog(expressive = true) }
}
