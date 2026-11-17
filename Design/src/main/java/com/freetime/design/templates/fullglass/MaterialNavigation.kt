package com.freetime.design.templates.fullglass

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Opaque Material navigation: only the supplied selection callback runs on a tap. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialNavigation(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    expressive: Boolean = false,
) {
    val labels = listOf("Home", "Favorites", "Profile", "Search")
    val icons = listOf("H", "F", "P", "S")
    if (expressive) {
        ShortNavigationBar(modifier = modifier) {
            labels.forEachIndexed { index, label ->
                ShortNavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = { Text(icons[index]) },
                    label = { Text(label) },
                )
            }
        }
    } else {
        NavigationBar(modifier = modifier) {
            labels.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    icon = { Text(icons[index]) },
                    label = { Text(label) },
                )
            }
        }
    }
}
