package me.free_time.updater

data class AppVersion(
    val versionName: String,
    val versionCode: Long,
    val downloadUrl: String? = null,
    val changelog: String? = null,
)

data class UpdateInfo(val current: AppVersion, val latest: AppVersion) {
    val updateAvailable: Boolean get() = latest.versionCode > current.versionCode
}

fun interface UpdateSource {
    suspend fun latest(packageName: String): AppVersion?
}

class FreetimeUpdater(private val source: UpdateSource) {
    suspend fun check(packageName: String, current: AppVersion): Result<UpdateInfo?> = runCatching {
        source.latest(packageName)?.let { UpdateInfo(current, it) }
    }
}
