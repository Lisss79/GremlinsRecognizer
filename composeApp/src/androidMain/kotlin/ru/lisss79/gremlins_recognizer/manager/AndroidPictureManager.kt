package ru.lisss79.gremlins_recognizer.manager

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidPictureManager(activity: ComponentActivity?): PictureManager {
    companion object {
        val empty = AndroidPictureManager(null)
    }

    private var imageSelectContinuation: Continuation<ImageBitmap?>? = null
    private val launcher = activity?.registerForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageSelectContinuation?.resume(uriToImageBitmap(uri, activity))
    }

    override suspend fun selectImage(): ImageBitmap? = suspendCoroutine { continuation ->
        imageSelectContinuation = continuation
        launcher?.launch("image/*")
    }

    override suspend fun takePicture(): ImageBitmap? {
        TODO("Not yet implemented")
    }

    private fun uriToImageBitmap(uri: Uri?, context: Context): ImageBitmap? {
        if (uri == null) return null
        val bitmap = try {
            context.contentResolver.openInputStream(uri).use { `is` ->
                BitmapFactory.decodeStream(`is`)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        return bitmap?.asImageBitmap()
    }
}