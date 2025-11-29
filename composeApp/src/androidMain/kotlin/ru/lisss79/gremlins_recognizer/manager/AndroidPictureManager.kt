package ru.lisss79.gremlins_recognizer.manager

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AndroidPictureManager(activity: ComponentActivity?) : PictureManager {

    private val cameraManager: CameraManager = activity
        ?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var photoUri: Uri? = null
    private val contentResolver = activity?.contentResolver
    private var pickerContinuation: Continuation<ImageBitmap?>? = null
    private var photoContinuation: Continuation<ImageBitmap?>? = null
    private val pickerLauncher = activity?.registerForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pickerContinuation?.resume(uriToImageBitmap(uri, activity))
    }
    private val photoLauncher = activity?.registerForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val currentPhotoUri = photoUri
        if (success && (currentPhotoUri != null)) {
            val bitmap = try {
                contentResolver?.openInputStream(currentPhotoUri).use { `is` ->
                    BitmapFactory.decodeStream(`is`)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            photoContinuation?.resume(bitmap?.asImageBitmap())
        } else photoContinuation?.resume(null)
    }

    override suspend fun selectImage(): ImageBitmap? = suspendCoroutine { continuation ->
        pickerContinuation = continuation
        pickerLauncher?.launch("image/*")
    }

    override suspend fun takePicture(): ImageBitmap? = suspendCoroutine { continuation ->
        photoContinuation = continuation
        photoUri = createImageUri()
        photoUri?.let { uri ->
            photoLauncher?.launch(uri)
        }
    }

    override fun getCamerasList(): List<Camera> {
        val cameraIds = cameraManager.cameraIdList
        return emptyList()
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

    private fun createImageUri(): Uri? {
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"))
        val filename = "IMG_$time.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/GremlinsRecognizer"
            )
        }
        val tempUri = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return tempUri
    }
}