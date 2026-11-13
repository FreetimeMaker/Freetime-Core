package com.freetime.notifications

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Reads repository metadata only; ZIP/JAR signatures are not validated. */
internal fun decodeNotificationResponse(
    bytes: ByteArray,
    contentType: String? = null,
    maxDecompressedBytes: Int = 16 * 1024 * 1024,
): String {
    require(maxDecompressedBytes > 0)
    val zipMagic = bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte() &&
        ((bytes[2] == 3.toByte() && bytes[3] == 4.toByte()) ||
            (bytes[2] == 5.toByte() && bytes[3] == 6.toByte()) ||
            (bytes[2] == 7.toByte() && bytes[3] == 8.toByte()))
    if (!zipMagic) {
        val charsetName = contentType?.split(';')
            ?.map { it.trim() }?.firstOrNull { it.startsWith("charset=", true) }
            ?.substringAfter('=')?.trim()?.trim('"', '\'')
        val charset = charsetName?.let { java.nio.charset.Charset.forName(it) } ?: Charsets.UTF_8
        return String(bytes, charset)
    }
    var remaining = maxDecompressedBytes
    java.util.zip.ZipInputStream(bytes.inputStream()).use { zip ->
        val buffer = ByteArray(8192)
        while (true) {
            val entry = zip.nextEntry ?: break
            val target = entry.name == "index-v1.json" && !entry.isDirectory
            val output = if (target) java.io.ByteArrayOutputStream() else null
            // Drain even ignored entries through the bound: nextEntry otherwise inflates them unchecked.
            while (true) {
                val count = zip.read(buffer)
                if (count < 0) break
                if (count > remaining) throw IOException("Notification archive exceeds the decompressed metadata limit")
                remaining -= count
                output?.write(buffer, 0, count)
            }
            if (output != null) return output.toString("UTF-8")
            zip.closeEntry()
        }
    }
    throw IOException("Notification archive is missing index-v1.json metadata")
}

internal interface NotificationHttpClient {
    suspend fun get(url: String): String
}

internal class HttpUrlConnectionClient(private val userAgent: String) : NotificationHttpClient {
    init {
        require(userAgent.isNotBlank() && '\r' !in userAgent && '\n' !in userAgent)
    }

    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val deadline = System.nanoTime() + 60_000_000_000L
        fun remainingMillis(): Int {
            val remaining = (deadline - System.nanoTime()) / 1_000_000
            if (remaining <= 0) throw IOException("Notification request exceeded its 60-second deadline")
            return remaining.coerceAtMost(Int.MAX_VALUE.toLong()).toInt().coerceAtLeast(1)
        }
        var target = url
        var redirects = 0
        while (true) {
            ensureActive()
            remainingMillis()
            requireHttpsUrl(target)
            val connection = URI(target).toURL().openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.requestMethod = "GET"
                connection.connectTimeout = minOf(15_000, remainingMillis())
                connection.readTimeout = minOf(20_000, remainingMillis())
                connection.setRequestProperty("User-Agent", userAgent)
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Accept-Encoding", "identity")
                val status = connection.responseCode
                ensureActive()
                if (status in listOf(301, 302, 303, 307, 308)) {
                    if (redirects++ >= 5) throw IOException("Notification source exceeded five redirects")
                    val location = connection.getHeaderField("Location")
                        ?: throw IOException("Notification source redirect has no Location")
                    target = URI(target).resolve(location).toString()
                    requireHttpsUrl(target)
                    continue
                }
                if (status == 429 || (status == 403 && connection.getHeaderField("X-RateLimit-Remaining") == "0")) {
                    throw IOException("Notification source rate limit reached (HTTP $status). Wait before retrying or increase the check interval.")
                }
                if (status == 404) throw IOException("Notification source not found (HTTP 404). Check the repository, package name, or API endpoint.")
                if (status !in 200..299) throw IOException("Notification source returned HTTP $status")
                val limit = 16 * 1024 * 1024
                if ((connection.getHeaderField("Content-Length")?.toLongOrNull() ?: 0L) > limit) {
                    throw IOException("Notification response exceeds 16 MiB")
                }
                val bytes = java.io.ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        ensureActive()
                        connection.readTimeout = minOf(20_000, remainingMillis())
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (bytes.size() > limit - count) throw IOException("Notification response exceeds 16 MiB")
                        bytes.write(buffer, 0, count)
                    }
                }
                ensureActive()
                val decoded = decodeNotificationResponse(bytes.toByteArray(), connection.contentType)
                ensureActive()
                return@withContext decoded
            } catch (error: kotlinx.coroutines.CancellationException) {
                throw error
            } catch (error: Exception) {
                currentCoroutineContext().ensureActive()
                // Platform exception messages can contain the request URL and its query secrets.
                throw IOException(when {
                    error is IOException && error.message?.startsWith("Notification ") == true -> error.message
                    else -> "Notification request failed. Check connectivity, HTTPS configuration, and the API endpoint."
                })
            } finally {
                connection.disconnect()
                // A blocking socket failure must not disguise cancellation as a source failure.
                currentCoroutineContext().ensureActive()
            }
        }
        @Suppress("UNREACHABLE_CODE")
        error("Unreachable")
    }
}
