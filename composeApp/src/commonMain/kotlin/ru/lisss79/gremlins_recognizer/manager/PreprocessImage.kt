package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import java.nio.FloatBuffer

expect fun preprocessImage(image: ImageBitmap): FloatBuffer