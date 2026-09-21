package com.freetime.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Immutable
data class FreetimeMessage(
    val text: String,
    val actionLabel: String? = null,
    val durationMillis: Long = 3_500L,
    val onAction: (() -> Unit)? = null,
)

@Stable
class FreetimeMessageHostState {
    private val queue = mutableStateListOf<FreetimeMessage>()
    val currentMessage: FreetimeMessage? get() = queue.firstOrNull()

    fun show(message: FreetimeMessage) {
        queue.add(message)
    }

    fun show(
        text: String,
        actionLabel: String? = null,
        durationMillis: Long = 3_500L,
        onAction: (() -> Unit)? = null,
    ) = show(FreetimeMessage(text, actionLabel, durationMillis, onAction))

    fun dismiss() {
        if (queue.isNotEmpty()) queue.removeAt(0)
    }
}

@Composable
fun rememberFreetimeMessageHostState(): FreetimeMessageHostState =
    remember { FreetimeMessageHostState() }

@Composable
fun FreetimeSnackbarHost(
    state: FreetimeMessageHostState,
    modifier: Modifier = Modifier,
) {
    val message = state.currentMessage
    LaunchedEffect(message) {
        if (message != null && message.durationMillis > 0) {
            delay(message.durationMillis)
            if (state.currentMessage === message) state.dismiss()
        }
    }
    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        if (message != null) {
            FreetimeGlassSnackbar(
                message = message.text,
                actionLabel = message.actionLabel,
                onAction = message.onAction?.let { action ->
                    {
                        action()
                        state.dismiss()
                    }
                },
            )
        }
    }
}

@Composable
fun FreetimeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
    maxSuggestions: Int = 5,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FreetimeGlassSearchField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
        )
        val visible = remember(value, suggestions, maxSuggestions) {
            if (value.isBlank()) emptyList()
            else suggestions
                .filter { it.contains(value, ignoreCase = true) }
                .take(maxSuggestions.coerceAtLeast(0))
        }
        AnimatedVisibility(visible.isNotEmpty()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .freetimeWideGlass(interactive = false)
                    .padding(6.dp),
            ) {
                visible.forEach { suggestion ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onValueChange(suggestion)
                                onSuggestionSelected(suggestion)
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(
                            suggestion,
                            style = FreetimeDesign.typography.bodyMedium.copy(
                                color = FreetimeDesign.colors.contentStrong
                            ),
                        )
                    }
                }
            }
        }
    }
}
