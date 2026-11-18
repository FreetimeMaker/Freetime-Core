package com.freetime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class FreetimeCoreTest {
    @Test
    fun sdkIdentityMatchesRelease() {
        assertEquals("Freetime Core", FreetimeCore.SDK_NAME)
        assertEquals("4.0.1", FreetimeCore.SDK_VERSION)
    }

    @Test
    fun successPreservesAppInfo() {
        val info = FreetimeAppInfo("com.example.app", "4.0.1", 4_000_000_000L)
        val result: FreetimeResult<FreetimeAppInfo> = FreetimeResult.Success(info)

        assertSame(info, assertIs<FreetimeResult.Success<FreetimeAppInfo>>(result).value)
        assertEquals(4_000_000_000L, result.value.versionCode)
    }

    @Test
    fun errorPreservesOriginalThrowableForAnyResultType() {
        val cause = IllegalStateException("Unavailable")
        val result: FreetimeResult<FreetimeAppInfo> = FreetimeResult.Error(cause)

        assertSame(cause, assertIs<FreetimeResult.Error>(result).throwable)
    }
}
