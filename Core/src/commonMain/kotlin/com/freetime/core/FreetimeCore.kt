package com.freetime.core


/**
 * Small, dependency-light entry point shared by all Freetime multiplatform libraries.
 * Apps stay independent: no Luma Store installation or Freetime account is required.
 */
expect object FreetimeCore {
    val SDK_NAME: String
    val SDK_VERSION: String
}

data class FreetimeAppInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
)

sealed interface FreetimeResult<out T> {
    data class Success<T>(val value: T) : FreetimeResult<T>
    data class Error(val throwable: Throwable) : FreetimeResult<Nothing>
}
