package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.awt.image.BufferedImage
import java.nio.FloatBuffer

actual fun preprocessImage(image: ImageBitmap): FloatBuffer {
    val inputWidth = 224
    val inputHeight = 224

    // 1. ImageBitmap → BufferedImage
    val buffered = image.toAwtImage()

    // 2. Resize
    val resized = BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB)
    val g = resized.createGraphics()
    g.drawImage(buffered, 0, 0, inputWidth, inputHeight, null)
    g.dispose()

    val floats = FloatArray(inputHeight * inputWidth * 3)
    var idx = 0
    for (y in 0 until inputHeight) {
        for (x in 0 until inputWidth) {
            val rgb = resized.getRGB(x, y)
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