package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable

@Serializable
data class State(
    val windowHeight: Int,
    val windowWidth: Int,
    val windowX: Int,
    val windowY: Int,
    val lastProject: String,
    val theme: String
)

class AppState {
    var windowWidth by mutableStateOf(0)
    var windowHeight by mutableStateOf(0)
    var windowX by mutableStateOf(0)
    var windowY by mutableStateOf(0)
    var lastProject by mutableStateOf("")
    var theme by mutableStateOf("")
}

fun createAppState(): AppState {
    return AppState()
}

object GlobalAppState {
    var appState: AppState? = null
}