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
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class JvmPictureManager : PictureManager {
    private var capture: VideoCapture? = null
    private var previewThread: Thread? = null
    private var running = false
    private var cameraNames: List<String> = listOf()

    fun startPreview(
        currentCamera: Int?,
        onFrame: (ImageBitmap?) -> Unit
    ): Boolean {
        if (currentCamera == null) return false
        if (running) return true
        running = true

        capture = VideoCapture(currentCamera)

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

    override fun getCamerasList(): List<String> {
        val cameras = MutableList(getNumberOfCamerasFromOpenCv()) { "Unknown camera" }
        val cmd =
            "powershell -Command \"Get-PnpDevice -Class Image | Where-Object Status -eq 'OK' | Select-Object -ExpandProperty FriendlyName\""

        try {
            val process = Runtime.getRuntime().exec(cmd)

            // Ждем завершения процесса
            process.waitFor(5, TimeUnit.SECONDS)

            // Читаем UTF-8 вывод
            process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
                reader.lineSequence()
                    .filter { it.trim().isNotBlank() }
                    .forEachIndexed { index, name ->
                        if (index < cameras.size) cameras[index] = name.trim()
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cameraNames = cameras
        return cameras
    }

    fun getCameraName(index: Int?) =
        if (index != null && index < cameraNames.size) cameraNames[index] else ""

    private fun getNumberOfCamerasFromOpenCv(max: Int = 10): Int {
        var count = 0
        for (i in 0 until max) {
            val camera = VideoCapture(i)
            if (camera.isOpened) {
                count++
                camera.release()
            } else {
                break
            }
        }
        return count
    }
}