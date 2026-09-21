package com.freetime.donations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freetime.design.FreetimeButton
import com.freetime.design.FreetimeDialog
import com.freetime.design.FreetimeText

object FreetimeDonationActions {
    fun copyWallet(context: Context, wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(wallet.label, wallet.address))
    }

    fun walletUri(wallet: DonationTarget.Wallet): Uri {
        val scheme = when (wallet.currency.trim().lowercase()) {
            "btc", "bitcoin" -> "bitcoin"
            "ltc", "litecoin" -> "litecoin"
            "eth", "ethereum" -> "ethereum"
            else -> wallet.currency.trim().lowercase()
        }
        return Uri.parse("$scheme:${wallet.address}")
    }

    fun openWallet(context: Context, wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, walletUri(wallet))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

/** QR presentation shell without forcing a QR encoding dependency. */
@Composable
fun FreetimeWalletQrDialog(
    wallet: DonationTarget.Wallet,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    qrContent: @Composable (payload: String) -> Unit,
) {
    FreetimeDialog(
        title = wallet.label,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                qrContent(wallet.qrPayload)
                FreetimeText(wallet.qrPayload)
            }
        },
        actions = {
            FreetimeButton("Close", onDismissRequest)
        },
    )
}