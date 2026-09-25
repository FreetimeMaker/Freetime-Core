package com.freetime.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freetime.browser.BrowserMode
import com.freetime.browser.BrowserOptions
import com.freetime.browser.FreetimeBrowser
import com.freetime.core.FreetimeCore
import com.freetime.design.AppTheme
import com.freetime.design.LiquidGlassRoot
import com.freetime.design.ThemeMode
import com.freetime.design.liquidGlass
import com.freetime.design.liquidGlassCapsule
import com.freetime.donations.DonationTarget
import com.freetime.donations.FreetimeDonationActions
import com.freetime.donations.FreetimeDonationScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SampleApp() }
    }
}

@Composable
private fun SampleApp() {
    var liquidGlassEnabled by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    AppTheme(
        themeMode = ThemeMode.AUTO_TIME,
        lightHour = 7,
        darkHour = 19,
        liquidGlassEnabled = liquidGlassEnabled,
    ) {
        LiquidGlassRoot(
            modifier = Modifier.fillMaxSize(),
            source = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                                ),
                            ),
                        ),
                )
            },
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .liquidGlassCapsule(interactive = false)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Freetime Core",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            FreetimeCore.SDK_VERSION,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .padding(12.dp)
                            .liquidGlassCapsule(interactive = false),
                        containerColor = Color.Transparent,
                    ) {
                        val items = listOf(
                            Triple("Design", Icons.Default.Home, 0),
                            Triple("Settings", Icons.Default.Settings, 1),
                            Triple("Browser", Icons.Default.Search, 2),
                            Triple("Donate", Icons.Default.Add, 3),
                        )
                        items.forEach { (label, icon, index) ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                            )
                        }
                    }
                },
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> DesignSample(
                        modifier = Modifier.padding(innerPadding),
                        showMessage = showMessage,
                    )
                    1 -> SettingsSample(
                        modifier = Modifier.padding(innerPadding),
                        liquidGlassEnabled = liquidGlassEnabled,
                        onLiquidGlassEnabledChange = { liquidGlassEnabled = it },
                    )
                    2 -> BrowserSample(
                        modifier = Modifier.padding(innerPadding),
                        showMessage = showMessage,
                    )
                    else -> DonationSample(
                        modifier = Modifier.padding(innerPadding),
                        showMessage = showMessage,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = ButtonDefaults.MediumContainerHeight)
            .liquidGlassCapsule(),
        contentPadding = ButtonDefaults.MediumContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text)
    }
}

@Composable
private fun DesignSample(
    modifier: Modifier,
    showMessage: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Material 3 Expressive + Liquid Glass", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Material 3 Expressive provides the component, shape, typography and motion system; Liquid Glass stays a shared effect layer.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(MaterialTheme.shapes.largeIncreased, interactive = false),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(
                Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Core", style = MaterialTheme.typography.titleMedium)
                Text("SDK: ${FreetimeCore.SDK_VERSION}")
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(MaterialTheme.shapes.medium, interactive = false),
            label = { Text("Search") },
            placeholder = { Text("Material 3, Liquid Glass…") },
        )

        GlassButton("Show message", { showMessage("Hello from Material 3 + Liquid Glass") })
    }
}

@Composable
private fun SettingsSample(
    modifier: Modifier,
    liquidGlassEnabled: Boolean,
    onLiquidGlassEnabledChange: (Boolean) -> Unit,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Global appearance", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(MaterialTheme.shapes.largeIncreased, interactive = false),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Liquid Glass", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "One AppTheme setting controls every liquidGlass surface.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Switch(
                    checked = liquidGlassEnabled,
                    onCheckedChange = onLiquidGlassEnabledChange,
                )
            }
        }

        Text(
            "Theme mode defaults to light from 07:00 and dark from 19:00.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BrowserSample(
    modifier: Modifier,
    showMessage: (String) -> Unit,
) {
    val context = LocalContext.current
    val url = "https://free-time.me"
    val options = BrowserOptions(
        mode = BrowserMode.EXTERNAL,
        allowedHosts = setOf("free-time.me"),
    )

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Browser", style = MaterialTheme.typography.headlineMedium)
        Text("Allowed: ${FreetimeBrowser.isAllowed(url, options)}")

        GlassButton("Open website", {
            FreetimeBrowser.open(context, url, options)
                .onFailure { showMessage(it.message ?: "Could not open URL") }
        })
    }
}

@Composable
private fun DonationSample(
    modifier: Modifier,
    showMessage: (String) -> Unit,
) {
    val context = LocalContext.current

    FreetimeDonationScreen(
        targets = listOf(
            DonationTarget.Link("Support page", "https://free-time.me"),
            DonationTarget.Wallet("Example wallet", "LTC", "ltc1qexampleaddress"),
        ),
        onLinkClick = {
            FreetimeBrowser.open(
                context,
                it.url,
                BrowserOptions(allowedHosts = setOf("free-time.me")),
            )
        },
        onWalletClick = {
            FreetimeDonationActions.openWallet(context, it)
                .onFailure { error -> showMessage(error.message ?: "No wallet app found") }
        },
        onCopyWallet = {
            FreetimeDonationActions.copyWallet(context, it)
                .onSuccess { showMessage("Wallet address copied") }
        },
        onShowQr = { showMessage("QR payload: " + it.qrPayload) },
        modifier = modifier.fillMaxSize(),
    )
}
