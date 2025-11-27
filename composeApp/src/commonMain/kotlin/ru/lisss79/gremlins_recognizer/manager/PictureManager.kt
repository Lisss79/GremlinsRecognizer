package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap

interface PictureManager {
    suspend fun selectImage(): ImageBitmap?
    suspend fun takePicture(): ImageBitmap?
}