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

expect val platform: Platform

class MainViewModel: ViewModel() {

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
        val text = result.firstOrNull()?.first ?: "Can't get info"
        _state.value = _state.value.copy(info = text)
    }

    fun setScreen(screen: MainScreen) {
        _state.value = _state.value.copy(currentScreen = screen)
    }
}