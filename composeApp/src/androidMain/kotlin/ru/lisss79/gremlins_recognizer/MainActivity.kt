package ru.lisss79.gremlins_recognizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.lisss79.gremlins_recognizer.manager.AndroidImageClassifier
import ru.lisss79.gremlins_recognizer.manager.AndroidPictureManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val pictureManager = AndroidPictureManager(this)
        val imageClassifier = AndroidImageClassifier(assetManager = this.assets)

        setContent {
            App(pictureManager, imageClassifier)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(AndroidPictureManager.empty, AndroidImageClassifier.empty)
}