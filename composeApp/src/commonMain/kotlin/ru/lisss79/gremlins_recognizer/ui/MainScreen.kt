package ru.lisss79.gremlins_recognizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import gremlinsrecognizer.composeapp.generated.resources.Res
import gremlinsrecognizer.composeapp.generated.resources.ic_close
import gremlinsrecognizer.composeapp.generated.resources.no_picture
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.lisss79.gremlins_recognizer.manager.ImageClassifierImpl
import ru.lisss79.gremlins_recognizer.manager.PictureManager
import ru.lisss79.gremlins_recognizer.state.MainScreen
import ru.lisss79.gremlins_recognizer.state.Platform
import ru.lisss79.gremlins_recognizer.viewModel.MainViewModel
import ru.lisss79.gremlins_recognizer.viewModel.platform

@Composable
fun MainScreen(
    pictureManager: PictureManager
) {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val imageClassifier = remember { ImageClassifierImpl() }

    if (state.currentScreen == MainScreen.MAIN_SCREEN) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val image = state.image
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
            ) {
                if (image != null) {
                    Image(
                        bitmap = image,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        contentDescription = null
                    )
                    IconButton(
                        onClick = { viewModel.clearData() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            tint = Color.Red,
                            contentDescription = null
                        )
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.no_picture),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 2.dp
            )
            Text(
                text = state.info,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        if (platform == Platform.ANDROID) {
                            scope.launch {
                                val imageBitmap = pictureManager.takePicture()
                                viewModel.setImage(imageBitmap)
                                val result = imageClassifier.classifyImage(imageBitmap)
                                viewModel.setResult(result)
                            }
                        } else {
                            viewModel.setScreen(MainScreen.CAMERA_PREVIEW)
                        }
                    }
                ) {
                    Text(
                        text = "Photo"
                    )
                }
                ElevatedButton(
                    modifier = Modifier,
                    onClick = {
                        scope.launch {
                            val imageBitmap = pictureManager.selectImage()
                            viewModel.setImage(imageBitmap)
                            val result = imageClassifier.classifyImage(imageBitmap)
                            viewModel.setResult(result)
                        }

                    }
                ) {
                    Text(
                        text = "Gallery"
                    )
                }
            }
        }
    } else {
        CameraControlScreen(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp),
            pictureManager = pictureManager,
            imageClassifier = imageClassifier,
            viewModel = viewModel
        )
    }
}