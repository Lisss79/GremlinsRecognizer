package ru.lisss79.gremlins_recognizer.viewModel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gremlinsrecognizer.composeapp.generated.resources.Res
import gremlinsrecognizer.composeapp.generated.resources.initial_info
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import ru.lisss79.gremlins_recognizer.state.MainScreen
import ru.lisss79.gremlins_recognizer.state.MainState
import ru.lisss79.gremlins_recognizer.state.Platform
import java.util.Locale

expect val platform: Platform

class MainViewModel : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(info = getString(Res.string.initial_info))
        }
    }

    fun setImage(imageBitmap: ImageBitmap?) {
        _state.value = _state.value.copy(image = imageBitmap)
    }

    fun clearData() {
        viewModelScope.launch {
            _state.value = MainState(
                image = null,
                info = getString(Res.string.initial_info)
            )
        }
    }

    fun setResult(result: List<Pair<String, Float>>) {
        val text = if (result.isEmpty()) "Can't get info"
        else result
            .filter { it.second > 0.5f }
            .joinToString("\n") { (label, prob) ->
                val probText = String.format(Locale.getDefault(), "%.2f", prob)
                "$label, probability: $probText"
            }
            .ifEmpty { "Can't recognize the object" }
        _state.value = _state.value.copy(info = text)
    }

    fun setScreen(screen: MainScreen) {
        _state.value = _state.value.copy(currentScreen = screen)
    }

    fun setCameras(
        currentCameraNumber: Int = 0,
        cameras: List<String>? = null
    ) {
        if (cameras != null) _state.value = _state.value.copy(numberOfCameras = cameras.size)
        if (currentCameraNumber < (state.value.numberOfCameras ?: 0)) {
            _state.value = _state.value.copy(currentCamera = currentCameraNumber)
        }
    }

    fun selectPreviousCamera() {
        val newNumber = state.value.currentCamera - 1
        if (newNumber >= 0 && newNumber < (state.value.numberOfCameras ?: 0))
            setCameras(newNumber)
    }

    fun selectNextCamera() {
        val newNumber = state.value.currentCamera + 1
        if (newNumber < (state.value.numberOfCameras ?: 0)) setCameras(newNumber)
    }
}