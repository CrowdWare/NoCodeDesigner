package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

@Composable
actual fun dynamicImageFromAssets(filename: String, scale: String, link: String) {
    Text(text="Image not implemented yet: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

@Composable
actual fun dynamicSoundfromAssets(filename: String) {
    Text(text="Sound not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

actual fun loadPage(pageId: String) {

}
actual fun openWebPage(url: String) {

}

@Composable
actual fun dynamicVideofromAssets(filename: String, height: Int) {
    TODO("Not yet implemented")
}