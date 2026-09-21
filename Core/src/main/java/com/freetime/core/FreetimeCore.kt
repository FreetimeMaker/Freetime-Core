package com.freetime.core

import android.content.Context

/**
 * Small, dependency-light entry point shared by all Freetime Android libraries.
 * Apps stay independent: no Luma Store installation or Freetime account is required.
 */
object FreetimeCore {
    const val SDK_NAME = "Freetime Core"
    const val SDK_VERSION = "1.0.0"

    fun appName(context: Context): String =
        context.applicationInfo.loadLabel(context.packageManager).toString()
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
