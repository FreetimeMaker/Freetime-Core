package com.freetime.browser
import kotlin.test.*
class BrowserTest {
    @Test fun hostAndSchemePolicy() {
        val options = BrowserOptions(allowedHosts = setOf("EXAMPLE.org"))
        assertTrue(FreetimeBrowser.isAllowed("https://example.org/path", options))
        assertFalse(FreetimeBrowser.isAllowed("http://example.org", options))
        assertFalse(FreetimeBrowser.isAllowed("https://example.org.evil.test", options))
        assertFalse(FreetimeBrowser.isAllowed("https:///path", options))
        assertFalse(FreetimeBrowser.isAllowed("javascript:alert(1)", options))
    }
    @Test fun malformedUrlsAreRejected() {
        val options = BrowserOptions()
        listOf(
            "",
            "example.org/path",
            "https:///path",
            "https://example.org/a b",
            "https://exa mple.org/path",
            "https://example.org/%ZZ",
            "https://[invalid]/",
        ).forEach { url ->
            assertFalse(FreetimeBrowser.isAllowed(url, options), url)
        }
        assertTrue(FreetimeBrowser.isAllowed("https://example.org/a%20b", options))
    }
    @Test fun explicitHttpAndCaseInsensitivePolicy() {
        val options = BrowserOptions(allowedHosts = setOf("EXAMPLE.org"), allowHttp = true)
        assertTrue(FreetimeBrowser.isAllowed("HTTP://example.ORG/path", options))
        assertFalse(FreetimeBrowser.isAllowed("ftp://example.org/path", options))
    }
    @Test fun malformedUrlsNeverReachHandlers() {
        var handlerCalls = 0
        val result = routeBrowserUrl(
            "https://example.org/a b",
            BrowserOptions(mode = BrowserMode.IN_APP),
            { handlerCalls++; true },
        ) { handlerCalls++; Result.success(Unit) }
        assertTrue(result.isFailure)
        assertEquals(0, handlerCalls)
    }
    @Test fun hostHandledAndFallbackPolicy() {
        var externalCalls = 0
        val options = BrowserOptions(mode = BrowserMode.IN_APP, allowExternalFallback = false)
        assertTrue(routeBrowserUrl("https://example.org", options, { true }) { externalCalls++; Result.success(Unit) }.isSuccess)
        assertTrue(routeBrowserUrl("https://example.org", options, { false }) { externalCalls++; Result.success(Unit) }.isFailure)
        assertEquals(0, externalCalls)
    }
    @Test fun handlerExceptionsAreResults() {
        assertTrue(routeBrowserUrl("https://example.org", BrowserOptions(mode = BrowserMode.IN_APP), { error("failure") }) { Result.success(Unit) }.isFailure)
    }
}
