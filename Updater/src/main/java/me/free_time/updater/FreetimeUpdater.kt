package me.free_time.updater

data class AppVersion(
    val versionName: String,
    val versionCode: Long,
    val downloadUrl: String? = null,
    val changelog: String? = null,
    val sha256: String? = null,
    val signature: String? = null,
    val downloadSizeBytes: Long? = null,
)

sealed interface UpdateDownloadState {
    data object Idle : UpdateDownloadState
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long?) : UpdateDownloadState {
        val progress: Float? get() = totalBytes?.takeIf { it > 0 }?.let { (bytesDownloaded.toFloat() / it).coerceIn(0f, 1f) }
    }
    data object Verifying : UpdateDownloadState
    data class Ready(val filePath: String) : UpdateDownloadState
    data class Failed(val reason: String) : UpdateDownloadState
}

object UpdateIntegrity {
    fun sha256Matches(bytes: ByteArray, expectedHex: String): Boolean {
        val actual = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        return actual.equals(expectedHex.trim(), ignoreCase = true)
    }
}

enum class UpdateSourceType {
    LUMA_STORE,
    F_DROID,
    GITHUB,
    OTHER,
}

sealed interface UpdateSourceSelection {
    data object All : UpdateSourceSelection
    data class Selected(val sources: Set<UpdateSourceType>) : UpdateSourceSelection {
        init { require(sources.isNotEmpty()) { "At least one update source must be selected." } }
    }

    companion object {
        fun only(source: UpdateSourceType): UpdateSourceSelection = Selected(setOf(source))
        fun of(vararg sources: UpdateSourceType): UpdateSourceSelection = Selected(sources.toSet())
    }
}

data class UpdateCandidate(
    val source: UpdateSourceType,
    val version: AppVersion,
)

data class UpdateInfo(
    val current: AppVersion,
    val latest: AppVersion,
    val source: UpdateSourceType,
    val candidates: List<UpdateCandidate> = emptyList(),
) {
    val updateAvailable: Boolean get() = latest.versionCode > current.versionCode
}

fun interface UpdateSource {
    suspend fun latest(packageName: String): AppVersion?
}

data class RegisteredUpdateSource(
    val type: UpdateSourceType,
    val source: UpdateSource,
)

class FreetimeUpdater(
    sources: Collection<RegisteredUpdateSource>,
) {
    private val sourcesByType = sources.associateBy { it.type }

    constructor(type: UpdateSourceType, source: UpdateSource) :
        this(listOf(RegisteredUpdateSource(type, source)))

    suspend fun check(
        packageName: String,
        current: AppVersion,
        selection: UpdateSourceSelection = UpdateSourceSelection.All,
    ): Result<UpdateInfo?> = runCatching {
        val selectedTypes = when (selection) {
            UpdateSourceSelection.All -> sourcesByType.keys
            is UpdateSourceSelection.Selected -> selection.sources
        }

        val candidates = selectedTypes.mapNotNull { type ->
            sourcesByType[type]?.source?.latest(packageName)?.let { UpdateCandidate(type, it) }
        }

        val latest = candidates.maxByOrNull { it.version.versionCode } ?: return@runCatching null
        UpdateInfo(
            current = current,
            latest = latest.version,
            source = latest.source,
            candidates = candidates,
        )
    }
}
