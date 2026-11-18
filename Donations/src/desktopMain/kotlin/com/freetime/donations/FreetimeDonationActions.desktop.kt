package com.freetime.donations

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

actual object FreetimeDonationActions {
    actual fun copyWallet(wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(wallet.address), null)
    }
    actual fun openWallet(wallet: DonationTarget.Wallet): Result<Unit> = runCatching {
        check(Desktop.isDesktopSupported()) { "Desktop integration is unavailable" }
        val desktop = Desktop.getDesktop()
        check(desktop.isSupported(Desktop.Action.BROWSE)) { "Opening wallet URIs is unavailable" }
        desktop.browse(URI(walletUriString(wallet)))
    }
}

@androidx.compose.runtime.Composable
actual fun rememberDonationLauncher(): DonationLauncher {
    return androidx.compose.runtime.remember {
        DonationLauncher(FreetimeDonationActions::copyWallet, FreetimeDonationActions::openWallet)
    }
}
