package me.free_time.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FreetimeSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

@Immutable
data class FreetimeSizes(
    val touchTarget: Dp = 48.dp,
    val buttonHeight: Dp = 50.dp,
    val iconButton: Dp = 48.dp,
    val navigationIcon: Dp = 23.dp,
    val floatingEdge: Dp = 18.dp,
)

@Immutable
data class FreetimeMotion(
    val fastMillis: Int = 120,
    val normalMillis: Int = 180,
    val slowMillis: Int = 280,
    val springDamping: Float = 0.72f,
    val springStiffness: Float = 300f,
)

enum class FreetimeDepth { Subtle, Standard, Elevated }

val LocalFreetimeSpacing = staticCompositionLocalOf { FreetimeSpacing() }
val LocalFreetimeSizes = staticCompositionLocalOf { FreetimeSizes() }
val LocalFreetimeMotion = staticCompositionLocalOf { FreetimeMotion() }
