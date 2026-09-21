package com.freetime.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freetime.browser.*
import com.freetime.core.*
import com.freetime.design.*
import com.freetime.donations.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { SampleApp() } }
}

@Composable
private fun SampleApp() {
    val context = LocalContext.current
    val preferences = remember { FreetimePreferences.from(context, "sample_preferences") }
    val controller = rememberFreetimePreferencesController(preferences)
    var selectedTab by remember { mutableIntStateOf(0) }
    val messages = rememberFreetimeMessageHostState()
    val tint = Color(0xFF6EA8FF)
    FreetimeApp(preferences = preferences, modifierConfig = {
        it.copy(backdropColors = listOf(Color(0xFF15233A), Color(0xFF513A70), Color(0xFF214F54)))
    }) {
        FreetimeScaffold(
            topBar = { FreetimeGlassTopBar("Freetime Core", subtitle = "${FreetimeCore.SDK_VERSION} library showcase", modifier = Modifier.padding(horizontal = 12.dp)) },
            bottomBar = {
                FreetimeBottomBar(
                    listOf(
                        FreetimeNavigationDestination("Design", Icons.Default.Home),
                        FreetimeNavigationDestination("Settings", Icons.Default.Settings),
                        FreetimeNavigationDestination("Browser", Icons.Default.Search),
                        FreetimeNavigationDestination("Donate", Icons.Default.Add),
                    ), selectedTab, { selectedTab = it },
                )
            },
        ) {
            Box(Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> DesignSample(messages, tint)
                    1 -> FreetimeAccessibilitySettings(controller)
                    2 -> BrowserSample(messages, tint)
                    else -> DonationSample(messages)
                }
                FreetimeSnackbarHost(state = messages, modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(20.dp))
            }
        }
    }
}

@Composable
private fun DesignSample(messages: FreetimeMessageHostState, tint: Color) {
    var query by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FreetimeGlassTitle("Liquid Glass", tint = tint)
        FreetimeGlassText("Reusable Freetime Design components.", tint = tint)
        FreetimeInfoCard("Core", "Shared SDK helpers") { FreetimeText("SDK: ${FreetimeCore.SDK_VERSION}") }
        FreetimeSettingsGroup("Components") {
            FreetimeSwitchSetting("Enabled", enabled, { enabled = it })
            FreetimeButton("Show message", { messages.show("Hello from Freetime Design") }, tint = tint)
        }
        FreetimeSearchBar(query, { query = it }, listOf("Liquid Glass", "Scaffold", "Browser", "Donations"), { messages.show("Selected: " + it) }, placeholder = "Search components…")
        FreetimeGlassSkeleton()
    }
}

@Composable
private fun BrowserSample(messages: FreetimeMessageHostState, tint: Color) {
    val context = LocalContext.current
    val url = "https://free-time.me"
    val options = BrowserOptions(mode = BrowserMode.EXTERNAL, allowedHosts = setOf("free-time.me"))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FreetimeGlassTitle("Browser", tint = tint)
        FreetimeInfoCard("URL validation") {
            FreetimeText("Allowed: ${FreetimeBrowser.isAllowed(url, options)}")
            FreetimeButton("Open website", {
                FreetimeBrowser.open(context, url, options).onFailure { messages.show(it.message ?: "Could not open URL") }
            }, tint = tint)
        }
    }
}

@Composable
private fun DonationSample(messages: FreetimeMessageHostState) {
    val context = LocalContext.current
    FreetimeDonationScreen(
        targets = listOf(DonationTarget.Link("Support page", "https://free-time.me"), DonationTarget.Wallet("Example wallet", "LTC", "ltc1qexampleaddress")),
        onLinkClick = { FreetimeBrowser.open(context, it.url, BrowserOptions(allowedHosts = setOf("free-time.me"))) },
        onWalletClick = { FreetimeDonationActions.openWallet(context, it).onFailure { error -> messages.show(error.message ?: "No wallet app found") } },
        onCopyWallet = { FreetimeDonationActions.copyWallet(context, it).onSuccess { messages.show("Wallet address copied") } },
        onShowQr = { messages.show("QR payload: " + it.qrPayload) },
        modifier = Modifier.fillMaxSize(),
    )
}
