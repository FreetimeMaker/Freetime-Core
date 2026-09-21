package me.free_time.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class FreetimeShapes(
    val compact: Shape = RoundedCornerShape(20.dp),
    val control: Shape = RoundedCornerShape(24.dp),
    val surface: Shape = RoundedCornerShape(30.dp),
    val dialog: Shape = RoundedCornerShape(34.dp),
)

val LocalFreetimeShapes = staticCompositionLocalOf { FreetimeShapes() }
