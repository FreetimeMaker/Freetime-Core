package com.freetime.notifications

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class SourceFetcher(private val http: NotificationHttpClient) {
    /** Fetch full text only for posts about to be delivered, not every historical feed entry. */
    suspend fun blogMessage(source: DeveloperBlogSource, entry: NotificationEntry): NotificationEntry {
        val base = java.net.URI(source.apiUrl)
        val slug = java.net.URLEncoder.encode(entry.id, "UTF-8").replace("+", "%20")
        val url = base.resolve(base.rawPath.trimEnd('/') + "/" + slug).toString()
        try {
            requireHttpsUrl(url)
            val response = http.get(url)
            currentCoroutineContext().ensureActive()
            return SourceParsers.blogMessage(entry, response)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            throw IOException("Notification blog post could not be read. Check the public post endpoint.")
        }
    }

    suspend fun fetch(source: NotificationSource): List<NotificationEntry> {
        val url = when (source) {
            is GitHubReleaseSource -> "https://api.github.com/repos/${source.owner}/${source.repository}/releases?per_page=100"
            is FDroidVersionSource -> source.apiUrl
                ?: "https://f-droid.org/api/v1/packages/${source.packageName}"
            is DeveloperBlogSource -> source.apiUrl
        }
        try {
            requireHttpsUrl(url)
            val response = http.get(url)
            currentCoroutineContext().ensureActive()
            return SourceParsers.parse(source, response).also { currentCoroutineContext().ensureActive() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            currentCoroutineContext().ensureActive()
            val detail = error.message?.takeIf {
                error is IOException && it.startsWith("Notification ") && !it.contains("://")
            }
            throw IOException(detail ?: "Notification source could not be read. Check the endpoint and expected JSON schema.")
        }
    }
}
