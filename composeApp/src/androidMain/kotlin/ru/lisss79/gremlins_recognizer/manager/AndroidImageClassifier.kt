package ru.lisss79.gremlins_recognizer.manager

import android.content.res.AssetManager
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class AndroidImageClassifier(val assetManager: AssetManager?) : ImageClassifier {
    companion object {
        val empty = AndroidImageClassifier(null)
    }

    private var inputImageWidth = 0
    private var inputImageHeight = 0
    private val interpreter = loadModelFile("image_model.tflite")?.let {
        val interpreter = Interpreter(it)
        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        interpreter
    }
    private val labels = loadLabels()

    override fun classifyImage(image: ImageBitmap?): List<Pair<String, Float>> {
        if (image == null) return listOf()

        // 1. Преобразуем Bitmap → TensorImage
        val tensorImage = TensorImage.fromBitmap(image.asAndroidBitmap())

        // 2. Предобработка: resize + normalize
        val processor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    inputImageHeight.takeIf { it > 0 } ?: 224,
                    inputImageWidth.takeIf { it > 0 } ?: 224,
                    ResizeOp.ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(0f, 255f)) // MobileNet ожидает [0, 1], но некоторые модели — [0, 255]
            .build()

        val processedImage = processor.process(tensorImage)

        // 3. Запуск инференса
        val outputBuffer = Array(1) { FloatArray(labels.size) }
        interpreter?.run(processedImage.buffer, outputBuffer)

        // 4. Получаем топ-5 результатов
        val probabilities = outputBuffer[0]
        val results = labels.mapIndexed { index, label ->
            label to probabilities[index]
        }.sortedByDescending { it.second }.take(5)
        return results
    }

    fun resizeImage(image: ImageBitmap): Bitmap {
        val resizedImage = Bitmap.createScaledBitmap(
            image.asAndroidBitmap(),
            inputImageWidth.takeIf { it > 0 } ?: 224,
            inputImageHeight.takeIf { it > 0 } ?: 224,
            true
        )
        return resizedImage
    }

    private fun loadModelFile(fileName: String): MappedByteBuffer? {
        return try {
            assetManager?.openFd(fileName)?.let { assetFileDescriptor ->
                val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
                val fileChannel = inputStream.channel
                val startOffset = assetFileDescriptor.startOffset
                val declaredLength = assetFileDescriptor.declaredLength
                fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    startOffset,
                    declaredLength
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun loadLabels(): List<String> {
        return try {
            assetManager?.open("image_labels.txt")?.let { `is` ->
                val labelsText = `is`.readAllBytes().toString(Charsets.UTF_8)
                labelsText.split("\n").drop(1).take(1000)
            } ?: listOf()
        } catch (e: IOException) {
            e.printStackTrace()
            listOf()
        }
    }
}