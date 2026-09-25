package com.freetime.warn

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

enum class FreetimeWarnFrequency {
    ONCE,
    ONCE_PER_VERSION,
    ALWAYS,
}

data class FreetimeWarnContent(
    val title: String = "Important information",
    val message: (appName: String) -> String = { appName ->
        "Android platform requirements may affect how $appName can be installed or kept working on some devices in the future. " +
            "The developer is showing this notice so you can stay informed about changes that may affect app distribution."
    },
    val detailsLabel: String = "Learn more",
    val acknowledgeLabel: String = "I understand",
)

class FreetimeWarnState internal constructor(
    context: Context,
    val appName: String,
    private val versionCode: Long,
    private val warningId: String,
    private val frequency: FreetimeWarnFrequency,
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "freetime_warn",
        Context.MODE_PRIVATE,
    )

    private val key: String
        get() = "ack_${warningId}_${appName.hashCode()}"

    var visible by mutableStateOf(shouldShow())
        private set

    private fun shouldShow(): Boolean = when (frequency) {
        FreetimeWarnFrequency.ALWAYS -> true
        FreetimeWarnFrequency.ONCE -> !preferences.getBoolean(key, false)
        FreetimeWarnFrequency.ONCE_PER_VERSION ->
            preferences.getLong(key, Long.MIN_VALUE) != versionCode
    }

    fun show() {
        visible = true
    }

    fun dismiss() {
        visible = false
    }

    fun acknowledge() {
        when (frequency) {
            FreetimeWarnFrequency.ALWAYS -> Unit
            FreetimeWarnFrequency.ONCE ->
                preferences.edit().putBoolean(key, true).apply()
            FreetimeWarnFrequency.ONCE_PER_VERSION ->
                preferences.edit().putLong(key, versionCode).apply()
        }
        visible = false
    }

    fun reset() {
        preferences.edit().remove(key).apply()
        visible = true
    }
}

@Composable
fun rememberFreetimeWarnState(
    context: Context,
    appName: String,
    versionCode: Long,
    warningId: String = "android-distribution-notice",
    frequency: FreetimeWarnFrequency = FreetimeWarnFrequency.ONCE_PER_VERSION,
): FreetimeWarnState {
    require(appName.isNotBlank()) { "FreetimeWarn requires a non-blank appName." }
    return remember(context.applicationContext, appName, versionCode, warningId, frequency) {
        FreetimeWarnState(
            context = context.applicationContext,
            appName = appName.trim(),
            versionCode = versionCode,
            warningId = warningId,
            frequency = frequency,
        )
    }
}

@Composable
fun FreetimeWarnDialog(
    appName: String,
    visible: Boolean,
    onAcknowledge: () -> Unit,
    onDismissRequest: () -> Unit,
    onLearnMore: (() -> Unit)? = null,
    content: FreetimeWarnContent = FreetimeWarnContent(),
) {
    require(appName.isNotBlank()) { "FreetimeWarn requires a non-blank appName." }
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(content.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = content.message(appName.trim()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "App: ${appName.trim()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        dismissButton = if (onLearnMore != null) {
            {
                TextButton(onClick = onLearnMore) {
                    Text(content.detailsLabel)
                }
            }
        } else {
            null
        },
        confirmButton = {
            TextButton(onClick = onAcknowledge) {
                Text(content.acknowledgeLabel)
            }
        },
    )
}

@Composable
fun FreetimeWarn(
    state: FreetimeWarnState,
    onLearnMore: (() -> Unit)? = null,
    content: FreetimeWarnContent = FreetimeWarnContent(),
) {
    FreetimeWarnDialog(
        appName = state.appName,
        visible = state.visible,
        onAcknowledge = state::acknowledge,
        onDismissRequest = state::dismiss,
        onLearnMore = onLearnMore,
        content = content,
    )
}
