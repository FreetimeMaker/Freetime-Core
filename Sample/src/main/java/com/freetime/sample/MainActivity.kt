package com.freetime.sample

import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.freetime.core.FreetimeCore
import com.freetime.core.FreetimeAppInfo
import com.freetime.core.FreetimeResult
import com.freetime.browser.BrowserMode
import com.freetime.browser.BrowserOptions
import com.freetime.browser.FreetimeBrowser
import com.freetime.donations.DonationTarget
import com.freetime.donations.FreetimeDonationScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
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
    var sheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
            FreetimeScaffold(
                topBar = {
                    FreetimeGlassTopBar(
                        title = "Freetime Core",
                        subtitle = "1.8.0 library showcase",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        actions = {
                            FreetimeButton(if (dark) "Light" else "Dark", { dark = !dark }, tint = tint)
                        },
                    )
                },
                bottomBar = {
                    FreetimeBottomBar(
                        destinations = listOf(
                            FreetimeNavigationDestination("Design", Icons.Default.Home),
                            FreetimeNavigationDestination("Core", Icons.Default.Settings),
                            FreetimeNavigationDestination("Browser", Icons.Default.Search),
                            FreetimeNavigationDestination("Donate", Icons.Default.Add),
                        ),
                        selectedIndex = selectedTab,
                        onDestinationSelected = { selectedTab = it },
                    )
                },
                floatingActionButton = {
                    FreetimeFloatingActionButton(Icons.Default.Add, "Open sheet", { sheet = true }, tint = tint)
                },
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    FreetimeGlassTitle("Liquid Glass", tint = tint)
                    FreetimeGlassText(
                        "The text itself uses the same glass optics as the controls.",
                        tint = tint,
                    )
                    FreetimeTabRow(
                        tabs = listOf("Design", "Core", "Browser", "Donate"),
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it },
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

                    FreetimeSectionHeader("Core", subtitle = "Shared SDK helpers and result models")
                    FreetimeInfoCard(
                        title = FreetimeCore.SDK_NAME,
                        subtitle = "Core module",
                    ) {
                        val info = FreetimeAppInfo(
                            packageName = context.packageName,
                            versionName = "1.0",
                            versionCode = 1,
                        )
                        val result: FreetimeResult<String> = FreetimeResult.Success(FreetimeCore.appName(context))
                        FreetimeText("SDK: ${FreetimeCore.SDK_VERSION}")
                        FreetimeText("Package: ${info.packageName}")
                        FreetimeText("Result: ${(result as FreetimeResult.Success).value}")
                    }

                    FreetimeSectionHeader("Browser", subtitle = "URL validation and browser modes")
                    FreetimeInfoCard("Freetime Browser") {
                        val browserOptions = BrowserOptions(
                            mode = BrowserMode.EXTERNAL,
                            allowedHosts = setOf("free-time.me"),
                        )
                        val sampleUrl = "https://free-time.me"
                        FreetimeText("Allowed: ${FreetimeBrowser.isAllowed(sampleUrl, browserOptions)}")
                        FreetimeButton("Open free-time.me", {
                            FreetimeBrowser.open(context, sampleUrl, browserOptions)
                        }, tint = tint)
                    }

                    FreetimeSectionHeader("Donations", subtitle = "Donation targets rendered with Freetime Design")
                    Box(Modifier.height(430.dp)) {
                        FreetimeDonationScreen(
                            targets = listOf(
                                DonationTarget.Link("Support page", "https://free-time.me"),
                                DonationTarget.Wallet("Example wallet", "LTC", "ltc1qexampleaddress"),
                            ),
                            onLinkClick = { link ->
                                FreetimeBrowser.open(
                                    context,
                                    link.url,
                                    BrowserOptions(allowedHosts = setOf("free-time.me")),
                                )
                            },
                            onWalletClick = { wallet ->
                                Toast.makeText(context, "${wallet.currency}: ${wallet.address}", Toast.LENGTH_SHORT).show()
                            },
                            onCopyWallet = { wallet ->
                                Toast.makeText(context, "Copy: ${wallet.address}", Toast.LENGTH_SHORT).show()
                            },
                            onShowQr = { wallet ->
                                Toast.makeText(context, "QR: ${wallet.qrPayload}", Toast.LENGTH_SHORT).show()
                            },
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

                }
            }

            FreetimeBottomSheet(visible = sheet, onDismissRequest = { sheet = false }) {
                FreetimeGlassTitle("Freetime Bottom Sheet", tint = tint)
                FreetimeText("This sheet is implemented by Freetime Design without Material Scaffold.")
                FreetimeButton("Close", { sheet = false }, tint = tint)
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
