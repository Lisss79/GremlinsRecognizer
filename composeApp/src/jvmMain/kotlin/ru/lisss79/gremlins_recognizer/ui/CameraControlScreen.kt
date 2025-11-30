package ru.lisss79.gremlins_recognizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    val state = viewModel.state.collectAsState()
    val nativePictureManager = pictureManager as? JvmPictureManager

    LaunchedEffect(state.value.currentCamera, state.value.numberOfCameras) {
        val cameras = pictureManager.getCamerasList()
        viewModel.setCameras(
            currentCameraNumber = state.value.currentCamera,
            cameras = cameras
        )
        if (cameras.isNotEmpty()) {
            nativePictureManager?.stopPreview()
            val result = nativePictureManager?.startPreview(state.value.currentCamera) {
                imageBitmap.value = it
            }
            cameraIsAvailable.value = result
        }
    }

    Box(modifier = modifier) {
        when {
            state.value.numberOfCameras != null && cameraIsAvailable.value == true -> {
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

            state.value.numberOfCameras == null -> {
                Text(
                    text = "Camera is initializing",
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }

            else -> {
                Text(
                    text = "Camera is not available",
                    modifier = Modifier
                        .align(Alignment.Center),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (state.value.numberOfCameras != null) {
                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        IconButton(
                            onClick = { viewModel.selectPreviousCamera() }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, "Previous"
                            )
                        }

                        Text(
                            modifier = Modifier,
                            text = nativePictureManager?.getCameraName(state.value.currentCamera)
                                ?: ""
                        )
                        IconButton(
                            onClick = { viewModel.selectNextCamera() }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward, "Next"
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
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
                            nativePictureManager?.stopPreview()
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
                        nativePictureManager?.stopPreview()
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
}