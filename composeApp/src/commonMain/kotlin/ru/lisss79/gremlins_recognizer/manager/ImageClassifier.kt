package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap

interface ImageClassifier {
    fun classifyImage(image: ImageBitmap?): List<Pair<String, Float>>
}