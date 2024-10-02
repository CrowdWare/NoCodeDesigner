package at.crowdware.nocodedesigner

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import androidx.compose.ui.ExperimentalComposeUiApi
import at.crowdware.nocodedesigner.viewmodel.projectState

@OptIn(ExperimentalComposeUiApi::class)
fun main() {


    ComposeViewport(document.body!!) {
        App()
    }
}