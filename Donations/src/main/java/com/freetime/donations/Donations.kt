package com.freetime.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freetime.design.FreetimeButton
import com.freetime.design.FreetimeCard
import com.freetime.design.FreetimeDesign
import com.freetime.design.FreetimeGlassTitle
import com.freetime.design.FreetimeText

sealed interface DonationTarget {
    val label: String
    data class Link(override val label: String, val url: String) : DonationTarget
    data class Wallet(
        override val label: String,
        val currency: String,
        val address: String,
        val qrPayload: String = address,
    ) : DonationTarget
}

data class DonationActions(
    val openLink: (DonationTarget.Link) -> Unit,
    val useWallet: (DonationTarget.Wallet) -> Unit,
    val copyWallet: ((DonationTarget.Wallet) -> Unit)? = null,
    val showQr: ((DonationTarget.Wallet) -> Unit)? = null,
)

@Composable
fun FreetimeDonationScreen(
    targets: List<DonationTarget>,
    onLinkClick: (DonationTarget.Link) -> Unit,
    onWalletClick: (DonationTarget.Wallet) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Support development",
    onCopyWallet: ((DonationTarget.Wallet) -> Unit)? = null,
    onShowQr: ((DonationTarget.Wallet) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "donation-title") {
            FreetimeGlassTitle(title)
        }
        items(
            items = targets,
            key = { target ->
                when (target) {
                    is DonationTarget.Link -> "link:${target.url}"
                    is DonationTarget.Wallet -> "wallet:${target.currency}:${target.address}:${target.label}"
                }
            },
        ) { target ->
            FreetimeCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FreetimeText(target.label, style = FreetimeDesign.typography.titleMedium)
                    when (target) {
                        is DonationTarget.Link ->
                            FreetimeButton("Open", { onLinkClick(target) })
                        is DonationTarget.Wallet -> {
                            FreetimeText(target.currency, style = FreetimeDesign.typography.labelMedium)
                            FreetimeText(target.address, style = FreetimeDesign.typography.bodyMedium, maxLines = 4)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FreetimeButton("Use", { onWalletClick(target) })
                                if (onCopyWallet != null) FreetimeButton("Copy", { onCopyWallet(target) })
                                if (onShowQr != null) FreetimeButton("QR", { onShowQr(target) })
                            }
                        }
                    }
                }
            }
        }
    }
}
