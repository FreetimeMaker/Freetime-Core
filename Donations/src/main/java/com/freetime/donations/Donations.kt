package com.freetime.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freetime.design.liquidGlass
import com.freetime.design.liquidGlassCapsule

sealed interface DonationTarget {
    val label: String

    data class Link(
        override val label: String,
        val url: String,
    ) : DonationTarget

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
private fun DonationAction(
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.liquidGlassCapsule(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(text)
    }
}

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
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }

        items(
            items = targets,
            key = { target ->
                when (target) {
                    is DonationTarget.Link -> "link:${target.url}"
                    is DonationTarget.Wallet ->
                        "wallet:${target.currency}:${target.address}:${target.label}"
                }
            },
        ) { target ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlass(
                        shape = MaterialTheme.shapes.large,
                        interactive = false,
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            target.label,
                            style = MaterialTheme.typography.titleMedium,
                        )

                        when (target) {
                            is DonationTarget.Link -> {
                                DonationAction("Open") { onLinkClick(target) }
                            }

                            is DonationTarget.Wallet -> {
                                Text(
                                    target.currency,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Text(
                                    target.address,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 4,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DonationAction("Use") { onWalletClick(target) }
                                    if (onCopyWallet != null) {
                                        DonationAction("Copy") { onCopyWallet(target) }
                                    }
                                    if (onShowQr != null) {
                                        DonationAction("QR") { onShowQr(target) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
