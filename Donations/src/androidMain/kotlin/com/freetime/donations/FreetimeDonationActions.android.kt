package com.freetime.donations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

actual object FreetimeDonationActions {
    actual fun copyWallet(wallet: DonationTarget.Wallet): Result<Unit> = Result.failure(IllegalStateException("Android requires a Context; use copyWallet(context, wallet)."))
    actual fun openWallet(wallet: DonationTarget.Wallet): Result<Unit> = Result.failure(IllegalStateException("Android requires a Context; use openWallet(context, wallet)."))
    fun copyWallet(context: Context, wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(wallet.label, wallet.address))
    }

    fun walletUri(wallet: DonationTarget.Wallet): Uri {
        return Uri.parse(walletUriString(wallet))
    }

    fun openWallet(context: Context, wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, walletUri(wallet))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}


@androidx.compose.runtime.Composable
actual fun rememberDonationLauncher(): DonationLauncher {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    return androidx.compose.runtime.remember(context) {
        DonationLauncher({ FreetimeDonationActions.copyWallet(context, it) }, { FreetimeDonationActions.openWallet(context, it) })
    }
}
