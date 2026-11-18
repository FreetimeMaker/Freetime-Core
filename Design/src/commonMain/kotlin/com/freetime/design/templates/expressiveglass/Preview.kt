package com.freetime.design.templates.expressiveglass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freetime.design.FloatingBottomNavigationBar
import com.freetime.design.FloatingBottomNavigationGlassRoot
import com.freetime.design.FloatingBottomNavigationItem

@Preview(name = "Expressive Glass · Light", showBackground = true)
@Composable
private fun LightPreview() {
    FreetimeTheme(darkTheme = false) { NavigationTemplate() }
}

@Preview(name = "Expressive Glass · Dark", showBackground = true)
@Composable
private fun DarkPreview() {
    FreetimeTheme(darkTheme = true) { NavigationTemplate() }
}

/** Selection-only example: navigation never changes content or opens a destination. */
@Composable
fun NavigationTemplate(modifier: Modifier = Modifier) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        FloatingBottomNavigationItem("Home") { Text("H") },
        FloatingBottomNavigationItem("Favorites") { Text("F") },
        FloatingBottomNavigationItem("Profile") { Text("P") },
    )
    FloatingBottomNavigationGlassRoot(
        modifier = modifier,
        source = {
            Column(
                modifier = Modifier.fillMaxSize().navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Expressive Glass", style = MaterialTheme.typography.headlineSmall)
                Card {
                    Text("Tap a navigation button to highlight it. The content stays unchanged.",
                        Modifier.padding(24.dp))
                }
            }
        },
    ) {
        FloatingBottomNavigationBar(
            items = items,
            selectedItemIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            searchItem = FloatingBottomNavigationItem("Search") { Text("S") },
            onSearchSelected = { selectedIndex = -1 },
            searchSelected = selectedIndex == -1,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
