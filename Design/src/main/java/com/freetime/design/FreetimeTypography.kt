package com.freetime.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class FreetimeTypography(
    val displayLarge: TextStyle = TextStyle(fontSize = 48.sp, lineHeight = 54.sp, fontWeight = FontWeight.SemiBold),
    val displayMedium: TextStyle = TextStyle(fontSize = 40.sp, lineHeight = 46.sp, fontWeight = FontWeight.SemiBold),
    val headlineLarge: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    val headlineMedium: TextStyle = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    val titleLarge: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    val titleMedium: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    val bodyLarge: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 24.sp),
    val bodyMedium: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 21.sp),
    val bodySmall: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    val labelLarge: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    val labelMedium: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    val labelSmall: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium),
)

@Immutable
data class FreetimePalette(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val contentStrong: Color,
    val contentMuted: Color,
    val outline: Color,
)

val LocalFreetimeTypography = staticCompositionLocalOf { FreetimeTypography() }
val LocalFreetimePalette = staticCompositionLocalOf {
    FreetimePalette(Color(0xFF090909), Color(0xFF101010), Color.White, Color.Black, Color.White, Color.LightGray, Color.White.copy(alpha=.16f))
}
