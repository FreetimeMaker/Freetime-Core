package com.freetime.donations
import kotlin.test.*
class WalletTest {
    @Test fun aliasesAndPayload() {
        assertEquals("bitcoin:address", walletUriString(DonationTarget.Wallet("BTC", " BTC ", "address")))
        assertEquals("ethereum:address", walletUriString(DonationTarget.Wallet("ETH", "ethereum", "address")))
    }
    @Test fun invalidSchemeIsRejected() {
        assertFailsWith<IllegalArgumentException> { walletUriString(DonationTarget.Wallet("bad", "bad scheme", "address")) }
    }
}
