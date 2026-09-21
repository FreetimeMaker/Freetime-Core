package me.free_time.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class FreetimeShapes(
    val compact: CornerBasedShape = RoundedCornerShape(20.dp),
    val control: CornerBasedShape = RoundedCornerShape(24.dp),
    val surface: CornerBasedShape = RoundedCornerShape(30.dp),
    val dialog: CornerBasedShape = RoundedCornerShape(34.dp),
)

val LocalFreetimeShapes = androidx.compose.runtime.staticCompositionLocalOf { FreetimeShapes() }
