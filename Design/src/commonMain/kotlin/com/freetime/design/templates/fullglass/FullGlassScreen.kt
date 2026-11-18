package com.freetime.design.templates.fullglass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.freetime.design.FloatingBottomNavigationBar
import com.freetime.design.FloatingBottomNavigationGlassRoot
import com.freetime.design.FloatingBottomNavigationItem
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/** Background and glass are siblings: recording the glass into its own source creates feedback. */
@Composable
fun FullGlassScreen(
    title: String,
    expressive: Boolean = false,
    modifier: Modifier = Modifier,
    navigationBar: (@Composable (selectedIndex: Int, onItemSelected: (Int) -> Unit) -> Unit)? = null,
) {
    val backdrop = rememberLayerBackdrop()
    val scheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    var navigationHeightPx by remember { mutableIntStateOf(0) }
    val bottomPadding = if (navigationHeightPx > 0) {
        with(density) { navigationHeightPx.toDp() } + 16.dp
    } else {
        112.dp
    }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var notifications by rememberSaveable { mutableStateOf(true) }
    var saved by rememberSaveable { mutableStateOf(false) }
    FloatingBottomNavigationGlassRoot(
        modifier = modifier,
        source = {
            Canvas(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                drawRect(Brush.linearGradient(listOf(scheme.primaryContainer, scheme.background, scheme.tertiaryContainer)))
                drawCircle(
                    brush = Brush.radialGradient(listOf(scheme.primary.copy(alpha = 0.55f), scheme.primary.copy(alpha = 0f)),
                        center = Offset(size.width * 0.15f, size.height * 0.2f), radius = size.width * 0.9f),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.15f, size.height * 0.2f),
                )
                drawCircle(
                    brush = Brush.radialGradient(listOf(scheme.tertiary.copy(alpha = 0.45f), scheme.tertiary.copy(alpha = 0f)),
                        center = Offset(size.width * 0.9f, size.height * 0.8f), radius = size.width * 0.8f),
                    radius = size.width * 0.8f,
                    center = Offset(size.width * 0.9f, size.height * 0.8f),
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalPageBackdrop provides backdrop) {
            Column(
                modifier = Modifier.fillMaxSize().safeDrawingPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(if (expressive) 24.dp else 16.dp),
            ) {
                LiquidGlassSurface(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, style = MaterialTheme.typography.headlineMedium)
                        Text("A complete glass workspace", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                LiquidGlassSurface(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Make room for your ideas", style = if (expressive)
                            MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall)
                        Text("Refracting surfaces, soft light and a clear focus. All panels share the same colorful backdrop.")
                        LiquidGlassButton(if (saved) "Saved" else "Save workspace", onClick = { saved = !saved })
                    }
                }
                LiquidGlassTextField(name, { name = it }, "Workspace name", Modifier.fillMaxWidth())
                LiquidGlassSurface(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Preferences", style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifications", Modifier.weight(1f))
                            LiquidGlassSwitch(
                                                            checked = notifications,
                                                            onCheckedChange = { notifications = it },
                                                            controlModifier = Modifier.semantics { contentDescription = "Notifications" },
                                                        )
                        }
                        Text("Navigation below only highlights your selection. It never changes this workspace.",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).onSizeChanged { navigationHeightPx = it.height }) {
            if (navigationBar != null) {
                navigationBar(selected) { selected = it }
            } else {
                FloatingBottomNavigationBar(
                    items = listOf(
                        FloatingBottomNavigationItem("Home") { Text("H") },
                        FloatingBottomNavigationItem("Favorites") { Text("F") },
                        FloatingBottomNavigationItem("Profile") { Text("P") },
                    ),
                    selectedItemIndex = selected,
                    onItemSelected = { selected = it },
                    searchItem = FloatingBottomNavigationItem("Search") { Text("S") },
                    onSearchSelected = { selected = -1 },
                    searchSelected = selected == -1,
                )
            }
        }
    }
}
