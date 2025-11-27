package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap

class JvmImageClassifier(): ImageClassifier {
    override fun classifyImage(image: ImageBitmap?): List<Pair<String, Float>> {
        if (image == null) return listOf()
        TODO("Not yet implemented")
    }
}