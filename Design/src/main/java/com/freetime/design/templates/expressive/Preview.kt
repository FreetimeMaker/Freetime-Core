package com.freetime.design.templates.expressive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Expressive · Light", showBackground = true)
@Composable
private fun LightPreview() {
    AppTheme(darkTheme = false, dynamicColor = false) { ThemeSample() }
}

@Preview(name = "Expressive · Dark", showBackground = true)
@Composable
private fun DarkPreview() {
    AppTheme(darkTheme = true, dynamicColor = false) { ThemeSample() }
}

@Composable
private fun ThemeSample() {
    Surface {
        Column(Modifier.padding(24.dp)) {
            Text("Expressive", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Card {
                Text("App-owned colors, typography and shapes", Modifier.padding(16.dp))
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {}) { Text("Continue") }
            Spacer(Modifier.height(16.dp))
            TextField(value = "Example", onValueChange = {}, label = { Text("Name") })
        }
    }
}
