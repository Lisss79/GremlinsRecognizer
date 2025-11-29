package ru.lisss79.gremlins_recognizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import ru.lisss79.gremlins_recognizer.manager.ImageClassifier
import ru.lisss79.gremlins_recognizer.manager.JvmPictureManager
import ru.lisss79.gremlins_recognizer.manager.PictureManager
import ru.lisss79.gremlins_recognizer.state.MainScreen
import ru.lisss79.gremlins_recognizer.viewModel.MainViewModel

@Composable
actual fun CameraControlScreen(
    modifier: Modifier,
    pictureManager: PictureManager,
    imageClassifier: ImageClassifier,
    viewModel: MainViewModel
) {
    val scope = rememberCoroutineScope()
    val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }
    val cameraIsAvailable = remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        val result = (pictureManager as JvmPictureManager).startPreview {
            imageBitmap.value = it
        }
        cameraIsAvailable.value = result
    }
    Box(modifier = modifier) {
        when (cameraIsAvailable.value) {
            true -> {
                imageBitmap.value?.let {
                    Image(
                        bitmap = it,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center),
                        contentDescription = null,
                        contentScale = ContentScale.Fit
                    )
                }
            }

            false -> {
                Text(
                    text = "Camera is not available",
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }

            else -> {
                Text(
                    text = "Camera is initializing",
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ElevatedButton(
                modifier = Modifier,
                onClick = {
                    scope.launch {
                        val imageBitmap = pictureManager.takePicture()
                        viewModel.setImage(imageBitmap)
                        val result = imageClassifier.classifyImage(imageBitmap)
                        viewModel.setResult(result)
                        (pictureManager as JvmPictureManager).stopPreview()
                        viewModel.setScreen(MainScreen.MAIN_SCREEN)
                    }
                }
            ) {
                Text(
                    text = "Take"
                )
            }
            ElevatedButton(
                modifier = Modifier,
                onClick = {
                    (pictureManager as JvmPictureManager).stopPreview()
                    viewModel.setScreen(MainScreen.MAIN_SCREEN)
                }
            ) {
                Text(
                    text = "Cancel"
                )
            }
        }
    }
}