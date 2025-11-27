package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.IOException

class JvmPictureManager : PictureManager {
    override suspend fun selectImage(): ImageBitmap? {
        val file = withContext(Dispatchers.Default) {
            val dialog = FileDialog(
                Frame(), "Select picture", FileDialog.LOAD
            ).apply {
                isMultipleMode = false
                isVisible = true
            }
            val fileName = dialog.file
            val directory = dialog.directory
            dialog.dispose()
            if (fileName != null && directory != null) File("$directory$fileName")
            else null
        }
        return fileToImageBitmap(file)
    }

    override suspend fun takePicture(): ImageBitmap? {
        TODO("Not yet implemented")
    }

    private fun fileToImageBitmap(file: File?): ImageBitmap? {
        if (file == null) return null
        val bitmap = try {
            val bytes = file.readBytes()
            Image.makeFromEncoded(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        return bitmap?.toComposeImageBitmap()
    }
}