package com.freetime.design

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.IntBuffer

/**
 * Samples a recorded backdrop using the same lightweight strategy as SimpMusic:
 * a 5x5 thumbnail, Rec.709 luminance and a smooth 500 ms transition.
 */
@Composable
fun rememberFreetimeBackdropLuminance(
    layer: GraphicsLayer,
    sampleIntervalMillis: Long = 1_000L,
): State<Float> {
    val animation = remember { Animatable(0.5f) }

    LaunchedEffect(layer, sampleIntervalMillis) {
        val buffer = IntBuffer.allocate(25)
        while (isActive) {
            val sampled = runCatching {
                withContext(Dispatchers.Default) {
                    val bitmap = layer.toImageBitmap().asAndroidBitmap()
                    if (bitmap.width <= 0 || bitmap.height <= 0) return@withContext null
                    val thumbnail = Bitmap.createScaledBitmap(bitmap, 5, 5, false)
                        .copy(Bitmap.Config.ARGB_8888, false)
                    buffer.rewind()
                    thumbnail.copyPixelsToBuffer(buffer)
                    thumbnail.recycle()

                    var total = 0.0
                    repeat(25) { index ->
                        val color = buffer.get(index)
                        val r = (color shr 16 and 0xFF) / 255.0
                        val g = (color shr 8 and 0xFF) / 255.0
                        val b = (color and 0xFF) / 255.0
                        total += 0.2126 * r + 0.7152 * g + 0.0722 * b
                    }
                    (total / 25.0).toFloat().coerceIn(0.3f, 0.8f)
                }
            }.getOrNull()

            if (sampled != null) animation.animateTo(sampled, tween(500))
            delay(sampleIntervalMillis.coerceAtLeast(250L))
        }
    }

    return remember(animation) { derivedStateOf { animation.value } }
}
