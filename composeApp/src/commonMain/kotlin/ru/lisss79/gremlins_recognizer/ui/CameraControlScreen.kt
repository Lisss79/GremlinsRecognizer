package ru.lisss79.gremlins_recognizer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.lisss79.gremlins_recognizer.manager.ImageClassifier
import ru.lisss79.gremlins_recognizer.manager.PictureManager
import ru.lisss79.gremlins_recognizer.viewModel.MainViewModel

@Composable
expect fun CameraControlScreen(
    modifier: Modifier,
    pictureManager: PictureManager,
    imageClassifier: ImageClassifier,
    viewModel: MainViewModel
)