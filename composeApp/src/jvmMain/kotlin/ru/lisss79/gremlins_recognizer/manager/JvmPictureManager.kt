package ru.lisss79.gremlins_recognizer.manager

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.cvtColor
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import org.jetbrains.skia.Image
import org.opencv.videoio.Videoio
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class JvmPictureManager : PictureManager {
    private var capture: VideoCapture? = null
    private var previewThread: Thread? = null
    private var running = false

    fun startPreview(onFrame: (ImageBitmap?) -> Unit): Boolean {
        if (running) return true
        running = true

        capture = VideoCapture(0) // 0 = первая камера

        if (!capture!!.isOpened) {
            running = false
            return false
        }

        previewThread = Thread {
            val mat = Mat()
            while (running) {
                if (capture!!.read(mat)) {
                    val img = matToImageBitmap(mat)
                    onFrame(img)
                }
            }
            mat.release()
        }

        previewThread!!.start()
        return true
    }

    fun stopPreview() {
        running = false
        previewThread?.join()
        capture?.release()
    }


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

    override suspend fun takePicture(): ImageBitmap? = suspendCoroutine { cont ->
        val cap = capture ?: VideoCapture(0)
        if (!cap.isOpened) {
            cont.resume(null)
            return@suspendCoroutine
        }

        val mat = Mat()
        if (cap.read(mat)) {
            cont.resume(matToImageBitmap(mat))
        } else {
            cont.resume(null)
        }
        mat.release()
    }

    private fun matToImageBitmap(mat: Mat): ImageBitmap? {
        // OpenCV дает BGR, переводим в RGB
        val rgb = Mat()
        cvtColor(mat, rgb, opencv_imgproc.COLOR_BGR2RGB)

        val bytes = ByteArray(rgb.rows() * rgb.cols() * rgb.channels())
        rgb.data().get(bytes)

        val image = BufferedImage(
            rgb.cols(),
            rgb.rows(),
            BufferedImage.TYPE_3BYTE_BGR
        )

        image.raster.setDataElements(0, 0, rgb.cols(), rgb.rows(), bytes)

        rgb.release()

        return image.toComposeImageBitmap()
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

    override fun getCamerasList(): List<Camera> {
        var index = 0
        val camerasList = mutableListOf<Camera>()
        while (index < 10) {
            val currentCapture = VideoCapture(index)
            if (currentCapture.isOpened) {
                currentCapture.release()
                break
            }
            camerasList.add(
                Camera(
                    width = currentCapture.get(Videoio.CAP_PROP_FRAME_WIDTH).toInt(),
                    height = currentCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT).toInt(),
                    fps = currentCapture.get(Videoio.CAP_PROP_FPS).toInt(),
                )
            )
            currentCapture.release()
            index++
        }
        return camerasList
    }
}