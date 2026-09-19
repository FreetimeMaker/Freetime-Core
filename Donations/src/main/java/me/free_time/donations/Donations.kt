package me.free_time.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.free_time.design.FreetimeGlassButton
import me.free_time.design.FreetimeGlassCard

sealed interface DonationTarget {
    val label: String
    data class Link(override val label: String, val url: String) : DonationTarget
    data class Wallet(override val label: String, val currency: String, val address: String) : DonationTarget
}

@Composable
fun FreetimeDonationScreen(
    targets: List<DonationTarget>,
    onLinkClick: (DonationTarget.Link) -> Unit,
    onWalletClick: (DonationTarget.Wallet) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Support development",
) {
    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        targets.forEach { target ->
            FreetimeGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(target.label, style = MaterialTheme.typography.titleMedium)
                    when (target) {
                        is DonationTarget.Link ->
                            FreetimeGlassButton("Open", { onLinkClick(target) })
                        is DonationTarget.Wallet -> {
                            Text(target.currency)
                            Text(target.address, color = MaterialTheme.colorScheme.onSurface)
                            FreetimeGlassButton("Use wallet address", { onWalletClick(target) })
                        }
                    }
                }
            }
        }
    }
}
