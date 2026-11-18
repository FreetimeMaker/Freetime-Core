package com.freetime.donations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

expect object FreetimeDonationActions {
    fun copyWallet(wallet: DonationTarget.Wallet): Result<Unit>
    fun openWallet(wallet: DonationTarget.Wallet): Result<Unit>
}

fun walletUriString(wallet: DonationTarget.Wallet): String {
    val scheme = when (val currency = wallet.currency.trim().lowercase()) {
        "btc", "bitcoin" -> "bitcoin"
        "ltc", "litecoin" -> "litecoin"
        "eth", "ethereum" -> "ethereum"
        else -> currency
    }
    require(scheme.matches(Regex("[a-z][a-z0-9+.-]*"))) { "Invalid wallet URI scheme" }
    return "$scheme:${wallet.address}"
}

/** QR presentation shell without forcing a QR encoding dependency. */
@Composable
fun FreetimeWalletQrDialog(
    wallet: DonationTarget.Wallet,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    qrContent: @Composable (payload: String) -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(wallet.label) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                qrContent(wallet.qrPayload)
                Text(wallet.qrPayload)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        },
    )
}

/** Platform-bound actions suitable for callbacks from shared Compose UI. */
class DonationLauncher(
    val copyWallet: (DonationTarget.Wallet) -> Result<Unit>,
    val openWallet: (DonationTarget.Wallet) -> Result<Unit>,
)

@Composable
expect fun rememberDonationLauncher(): DonationLauncher
