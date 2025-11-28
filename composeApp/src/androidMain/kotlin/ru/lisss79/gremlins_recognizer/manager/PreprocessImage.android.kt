package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.graphics.get
import androidx.core.graphics.scale
import java.nio.FloatBuffer

actual fun preprocessImage(image: ImageBitmap): FloatBuffer {
    val inputWidth = 224
    val inputHeight = 224

    // 1. ImageBitmap → Android Bitmap
    val bitmap = image.asAndroidBitmap()

    // 2. Resize до 224x224
    val resized = bitmap.scale(inputWidth, inputHeight)

    val floats = FloatArray(inputHeight * inputWidth * 3)
    var idx = 0
    for (y in 0 until inputHeight) {
        for (x in 0 until inputWidth) {
            val pixel = resized[x, y]
            val r = ((pixel shr 16) and 0xFF) / 127.5f - 1f
            val g = ((pixel shr 8) and 0xFF) / 127.5f - 1f
            val b = (pixel and 0xFF) / 127.5f - 1f

            floats[idx++] = r
            floats[idx++] = g
            floats[idx++] = b
        }
    }
    return FloatBuffer.wrap(floats)
}