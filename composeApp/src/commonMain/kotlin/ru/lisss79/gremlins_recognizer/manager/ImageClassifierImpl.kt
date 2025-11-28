package ru.lisss79.gremlins_recognizer.manager

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import androidx.compose.ui.graphics.ImageBitmap
import gremlinsrecognizer.composeapp.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageClassifierImpl : ImageClassifier {

    private val env = OrtEnvironment.getEnvironment()
    private lateinit var session: OrtSession
    private lateinit var inputName: String
    private lateinit var labels: List<String>

    private val inputWidth = 224
    private val inputHeight = 224

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Загружаем ONNX модель из ресурсов
            val modelBytes = Res.readBytes("files/my_model.onnx")
            session = env.createSession(modelBytes)
            inputName = session.inputNames.first()
            labels = loadLabels("files/image_labels.txt")
        }
    }

    override fun classifyImage(image: ImageBitmap?): List<Pair<String, Float>> {
        if (image == null) return emptyList()

        // Preprocess: FloatBuffer NCHW
        val inputBuffer = preprocessImage(image)

        // Создание тензора
        val shape = longArrayOf(1, inputHeight.toLong(), inputWidth.toLong(), 3)
        val tensor = OnnxTensor.createTensor(env, inputBuffer, shape)

        // Инференс
        val output = session.run(mapOf(inputName to tensor))
        val raw = output[0].value as Array<FloatArray>  // [1, 1000]
        val probs = raw[0]

        // Top-5
        return labels.mapIndexed { i, label -> label to probs[i] }
            .sortedByDescending { it.second }
            .take(5)
    }

    private suspend fun loadLabels(path: String): List<String> {
        val lines = Res.readBytes(path).toString(Charsets.UTF_8).lines()
        return lines.drop(1).take(1000)
    }

}