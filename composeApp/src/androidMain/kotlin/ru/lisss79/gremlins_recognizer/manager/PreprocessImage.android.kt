package ru.lisss79.gremlins_recognizer.manager

import android.graphics.Canvas
import android.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import java.nio.FloatBuffer
import kotlin.math.min

actual fun preprocessImage(image: ImageBitmap): FloatBuffer {
    val inputWidth = 224
    val inputHeight = 224

    // 1. ImageBitmap → Bitmap
    val bitmap = image.asAndroidBitmap()

    val srcW = bitmap.width
    val srcH = bitmap.height

    // 2. Scale с сохранением пропорций
    val scale = min(
        inputWidth.toFloat() / srcW,
        inputHeight.toFloat() / srcH
    )

    val newW = (srcW * scale).toInt()
    val newH = (srcH * scale).toInt()

    val scaled = bitmap.scale(newW, newH)

    // 3. Создаём padded bitmap 224x224
    val padded = createBitmap(inputWidth, inputHeight)
    val canvas = Canvas(padded)

    // Заливаем фон чёрным (как TF)
    canvas.drawColor(Color.BLACK)

    // Центрирование
    val offsetX = (inputWidth - newW) / 2
    val offsetY = (inputHeight - newH) / 2

    canvas.drawBitmap(scaled, offsetX.toFloat(), offsetY.toFloat(), null)

    // 4. Преобразуем в float buffer с нормализацией MobileNetV2
    val floats = FloatArray(inputWidth * inputHeight * 3)
    var idx = 0

    val pixels = IntArray(inputWidth * inputHeight)
    padded.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

    for (pixel in pixels) {
        val r = ((pixel shr 16) and 0xFF) / 127.5f - 1f
        val g = ((pixel shr 8) and 0xFF) / 127.5f - 1f
        val b = (pixel and 0xFF) / 127.5f - 1f

        floats[idx++] = r
        floats[idx++] = g
        floats[idx++] = b
    }

    return FloatBuffer.wrap(floats)
}
