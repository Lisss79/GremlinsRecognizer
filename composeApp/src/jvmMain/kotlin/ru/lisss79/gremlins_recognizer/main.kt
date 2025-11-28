package ru.lisss79.gremlins_recognizer

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.lisss79.gremlins_recognizer.manager.JvmPictureManager

fun main() = application {
    val pictureManager = JvmPictureManager()
    Window(
        onCloseRequest = ::exitApplication,
        title = "GremlinsRecognizer",
    ) {
        App(pictureManager)
    }
}