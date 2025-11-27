package ru.lisss79.gremlins_recognizer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.lisss79.gremlins_recognizer.manager.JvmImageClassifier
import ru.lisss79.gremlins_recognizer.manager.JvmPictureManager
import ru.lisss79.gremlins_recognizer.manager.PictureManager

fun main() = application {
    val pictureManager = JvmPictureManager()
    val imageClassifier = JvmImageClassifier()
    Window(
        onCloseRequest = ::exitApplication,
        title = "GremlinsRecognizer",
    ) {
        App(pictureManager, imageClassifier)
    }
}