package com.freetime.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freetime.design.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SampleApp() }
    }
}

@Composable
private fun SampleApp() {
    var dark by remember { mutableStateOf(true) }
    var tintEnabled by remember { mutableStateOf(true) }
    var enabled by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var choice by remember { mutableStateOf("Glass") }
    var slider by remember { mutableFloatStateOf(.62f) }
    var dialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tint = if (tintEnabled) Color(0xFF6EA8FF) else Color.Unspecified

    FreetimeTheme(darkTheme = dark) {
        FreetimeGlassRoot(
            FreetimeDynamicBackdrop(
                listOf(
                    Color(0xFF15233A),
                    Color(0xFF513A70),
                    Color(0xFF214F54),
                )
            )
        ) {
            Column(Modifier.fillMaxSize()) {
                FreetimeGlassTopBar(
                    title = "Freetime Design",
                    subtitle = "Liquid Glass component showcase",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    actions = {
                        FreetimeButton(if (dark) "Light" else "Dark", { dark = !dark }, tint = tint)
                    },
                )

                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    FreetimeGlassTitle("Liquid Glass", tint = tint)
                    FreetimeGlassText(
                        "The text itself uses the same glass optics as the controls.",
                        tint = tint,
                    )

                    FreetimeSectionHeader(
                        "Actions",
                        subtitle = "Buttons, tint, badges and interactive glass",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FreetimeButton("Default", {})
                        FreetimeButton("Tinted", {}, tint = tint)
                        FreetimeBadge("1.7+")
                    }
                    FreetimeGlassAction(onClick = {}) {
                        FreetimeText("Full-width glass action", Modifier.weight(1f))
                        FreetimeText("→")
                    }

                    FreetimeInfoCard(
                        title = "Cards",
                        subtitle = "All Freetime surfaces use the shared liquid-glass primitive",
                    ) {
                        FreetimeText("Backdrop blur, vibrancy, lens, highlight and press interaction are shared.")
                        FreetimeChip("Selected chip", true, {}, tint = tint)
                    }

                    FreetimeSettingsGroup("Settings") {
                        FreetimeSwitchSetting(
                            "Enable feature",
                            enabled,
                            { enabled = it },
                            "Switches tint their active liquid-glass surface.",
                        )
                        FreetimeSwitchSetting(
                            "Glass tint",
                            tintEnabled,
                            { tintEnabled = it },
                            "Tint is an extension layered on top of the glass optics.",
                        )
                        FreetimeSwitchSetting("Dark theme", dark, { dark = it })
                        FreetimeOptionGroup(
                            listOf("Glass" to "Liquid Glass", "Plain" to "Plain"),
                            choice,
                            { choice = it },
                        )
                    }

                    FreetimeSectionHeader("Inputs", subtitle = "Text field, search and slider")
                    FreetimeGlassSearchField(query, { query = it }, "Search components…")
                    FreetimeTextField(query, { query = it }, label = "Text field", placeholder = "Type something")
                    FreetimeSlider(slider, { slider = it })

                    FreetimeStatusBanner(
                        "Liquid Glass is active",
                        actionLabel = "Details",
                        onAction = { dialog = true },
                    )
                    FreetimeGlassSnackbar("This is a glass snackbar", actionLabel = "OK", onAction = {})

                    FreetimeSectionHeader("States")
                    FreetimeLoadingState(message = "Loading sample data…")
                    FreetimeEmptyState(
                        "Nothing here yet",
                        message = "Empty and error states are reusable too.",
                        actionLabel = "Create",
                        onAction = {},
                    )
                    FreetimeErrorState(
                        "Example error",
                        message = "Use the same design language for failures.",
                        retryLabel = "Retry",
                        onRetry = {},
                    )

                    FreetimeSectionHeader("Skeleton")
                    FreetimeGlassSkeleton()
                    Spacer(Modifier.height(8.dp))
                }

                FreetimeBottomBar(
                    destinations = listOf(
                        FreetimeNavigationDestination("Home", androidx.compose.material.icons.Icons.Default.Home),
                        FreetimeNavigationDestination("Explore", androidx.compose.material.icons.Icons.Default.Search),
                        FreetimeNavigationDestination("Settings", androidx.compose.material.icons.Icons.Default.Settings),
                    ),
                    selectedIndex = selectedTab,
                    onDestinationSelected = { selectedTab = it },
                )
            }

            if (dialog) {
                FreetimeDialog(
                    title = "Liquid Glass",
                    text = "This dialog also uses the shared Freetime liquid-glass renderer.",
                    confirmText = "Done",
                    onConfirm = { dialog = false },
                    dismissText = "Close",
                    onDismissRequest = { dialog = false },
                )
            }
        }
    }
}
