package com.freetime.core

import android.content.Context

actual object FreetimeCore {
    actual const val SDK_NAME = "Freetime Core"
    actual const val SDK_VERSION = "4.0.0"
    fun appName(context: Context): String =
        context.applicationInfo.loadLabel(context.packageManager).toString()
}
