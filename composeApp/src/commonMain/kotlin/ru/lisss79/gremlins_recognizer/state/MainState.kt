package ru.lisss79.gremlins_recognizer.state

import androidx.compose.ui.graphics.ImageBitmap

data class MainState(
    val image: ImageBitmap? = null,
    val info: String = ""
)
