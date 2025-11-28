package ru.lisss79.gremlins_recognizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.lisss79.gremlins_recognizer.manager.AndroidPictureManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val pictureManager = AndroidPictureManager(this)

        setContent {
            App(pictureManager)
        }
    }
}