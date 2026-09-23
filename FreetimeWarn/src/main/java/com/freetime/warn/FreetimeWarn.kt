package com.freetime.warn

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.freetime.design.FreetimeButton
import com.freetime.design.FreetimeDesign
import com.freetime.design.FreetimeDialog
import com.freetime.design.FreetimeText

/**
 * Controls how often a warning is shown.
 */
enum class FreetimeWarnFrequency {
    /** Show until the user acknowledges the warning. */
    ONCE,

    /** Show once for every app version code. */
    ONCE_PER_VERSION,

    /** Let the host decide when to show it; no acknowledgement is persisted. */
    ALWAYS,
}

/**
 * Content displayed by [FreetimeWarnDialog].
 *
 * The app name is intentionally not part of this model. It is a mandatory
 * argument of the public API so integrations cannot accidentally omit it.
 */
data class FreetimeWarnContent(
    val title: String = "Important information",
    val message: (appName: String) -> String = { appName ->
        "Android platform requirements may affect how $appName can be installed or kept working on some devices in the future. " +
            "The developer is showing this notice so you can stay informed about changes that may affect app distribution."
    },
    val detailsLabel: String = "Learn more",
    val acknowledgeLabel: String = "I understand",
)

/**
 * Persistent state for a Freetime warning.
 *
 * [appName] is mandatory and has no default value by design.
 */
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
        FreetimeWarnFrequency.ONCE_PER_VERSION -> preferences.getLong(key, Long.MIN_VALUE) != versionCode
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
            FreetimeWarnFrequency.ONCE -> preferences.edit().putBoolean(key, true).apply()
            FreetimeWarnFrequency.ONCE_PER_VERSION -> preferences.edit().putLong(key, versionCode).apply()
        }
        visible = false
    }

    fun reset() {
        preferences.edit().remove(key).apply()
        visible = true
    }
}

/**
 * Creates persistent warning state.
 *
 * There is deliberately no default for [appName]. Every integrating developer
 * must provide the real user-facing application name.
 */
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

/**
 * Freetime Design based warning dialog.
 *
 * [appName] is mandatory even when a custom [content] object is supplied.
 * The host owns navigation for [onLearnMore], keeping this library independent
 * from a particular browser or website.
 */
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

    FreetimeDialog(
        title = content.title,
        onDismissRequest = onDismissRequest,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FreetimeText(
                    text = content.message(appName.trim()),
                    style = FreetimeDesign.typography.bodyMedium,
                    color = FreetimeDesign.colors.contentMuted,
                )
                FreetimeText(
                    text = "App: ${appName.trim()}",
                    style = FreetimeDesign.typography.labelMedium,
                    color = FreetimeDesign.colors.contentStrong,
                )
            }
        },
        actions = {
            if (onLearnMore != null) {
                FreetimeButton(
                    text = content.detailsLabel,
                    onClick = onLearnMore,
                )
            }
            FreetimeButton(
                text = content.acknowledgeLabel,
                onClick = onAcknowledge,
            )
        },
    )
}

/**
 * Convenience wrapper that wires [FreetimeWarnState] to [FreetimeWarnDialog].
 */
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
