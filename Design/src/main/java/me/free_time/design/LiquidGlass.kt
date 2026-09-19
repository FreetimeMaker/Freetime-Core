package me.free_time.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object FreetimeGlassDefaults {
    val shape = RoundedCornerShape(28.dp)
    val compactShape = RoundedCornerShape(22.dp)
}

fun Modifier.freetimeGlass(): Modifier = this
    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f), FreetimeGlassDefaults.shape)
    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f), FreetimeGlassDefaults.shape)

@Composable
fun FreetimeGlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = FreetimeGlassDefaults.shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)),
    ) { Surface(color = Color.Transparent, modifier = Modifier.padding(16.dp)) { content() } }
}

@Composable
fun FreetimeGlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = FreetimeGlassDefaults.compactShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) { Text(text) }
}

@Composable
fun FreetimeGlassNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    NavigationBar(
        modifier = modifier.freetimeGlass(),
        containerColor = Color.Transparent,
        content = content,
    )
}
