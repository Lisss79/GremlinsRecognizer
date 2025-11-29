package ru.lisss79.gremlins_recognizer.state

import androidx.compose.ui.graphics.ImageBitmap

data class MainState(
    val image: ImageBitmap? = null,
    val info: String = "",
    val currentScreen: MainScreen = MainScreen.MAIN_SCREEN,
    val platform: Platform = Platform.NONE
)

enum class MainScreen {
    MAIN_SCREEN, CAMERA_PREVIEW
}

enum class Platform {
    JVM, ANDROID, NONE
}