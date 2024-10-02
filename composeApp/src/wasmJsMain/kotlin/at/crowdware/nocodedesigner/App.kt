package at.crowdware.nocodedesigner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.AppTheme
import at.crowdware.nocodedesigner.view.desktop.desktop
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import at.crowdware.nocodedesigner.viewmodel.createProjectState
import at.crowdware.nocodedesigner.viewmodel.projectState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import nocodedesigner.composeapp.generated.resources.Res
import nocodedesigner.composeapp.generated.resources.compose_multiplatform
import kotlinx.browser.window
import at.crowdware.nocodedesigner.viewmodel.ProjectState

val LocalProjectState = compositionLocalOf<ProjectState> { error("No ProjectState provided") }

@Composable
fun App() {
    val darkMode = androidx.compose.foundation.isSystemInDarkTheme()
    //val projectState = projectState.current
    val myProjectState = createProjectState()
    GlobalProjectState.projectState = myProjectState
    CompositionLocalProvider(LocalProjectState provides myProjectState) {

        myProjectState.LoadProject("TestProject", "4634f957-8048-4a1d-bf1f-614047b7c621", "4634f957-8048-4a1d-bf1f-614047b7c622")

        AppTheme(darkTheme = darkMode) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(0.5.dp, Color.Gray, RoundedCornerShape(10.dp)),
                color = Color(55, 55, 55),
                shape = RoundedCornerShape(10.dp) //window has round corners now

            ) {
                desktop()
            }
        }
    }
}
