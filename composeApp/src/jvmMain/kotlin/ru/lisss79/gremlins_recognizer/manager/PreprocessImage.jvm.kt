package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.image.BufferedImage
import java.nio.FloatBuffer

actual fun preprocessImage(image: ImageBitmap): FloatBuffer {
    val inputWidth = 224
    val inputHeight = 224

    val buffered = image.toAwtImage()
    val srcW = buffered.width
    val srcH = buffered.height

    // 1. Масштабируем с сохранением пропорций
    val scale = minOf(
        inputWidth.toFloat() / srcW,
        inputHeight.toFloat() / srcH
    )

    val newW = (srcW * scale).toInt()
    val newH = (srcH * scale).toInt()

    val scaled = BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB)
    val g1 = scaled.createGraphics()
    g1.drawImage(buffered, 0, 0, newW, newH, null)
    g1.dispose()

    // 2. Центрируем на чёрном фоне 224x224
    val padded = BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB)
    val g2 = padded.createGraphics()

    g2.color = java.awt.Color.BLACK  // цвет паддинга (аналог TF)
    g2.fillRect(0, 0, inputWidth, inputHeight)

    val offsetX = (inputWidth - newW) / 2
    val offsetY = (inputHeight - newH) / 2
    g2.drawImage(scaled, offsetX, offsetY, null)
    g2.dispose()

    // 3. Нормализация под MobileNetV2: [-1, 1]
    val floats = FloatArray(inputWidth * inputHeight * 3)
    var idx = 0
    for (y in 0 until inputHeight) {
        for (x in 0 until inputWidth) {
            val rgb = padded.getRGB(x, y)
            val r = ((rgb shr 16) and 0xFF) / 127.5f - 1f
            val g = ((rgb shr 8) and 0xFF) / 127.5f - 1f
            val b = (rgb and 0xFF) / 127.5f - 1f

            floats[idx++] = r
            floats[idx++] = g
            floats[idx++] = b
        }
    }

    return FloatBuffer.wrap(floats)
}