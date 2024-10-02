package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import java.awt.Desktop
import java.io.File
import java.net.URI


@Composable
actual fun dynamicImageFromAssets(filename: String, scale: String, link: String) {

    val ps = GlobalProjectState.projectState
    val path = "${ps?.folder}/assets/$filename"
    println("path: $path")

    val imageFile = File(path)
    var bitmap: ImageBitmap = ImageBitmap(1, 1)
    if (imageFile.exists()) {
        try {
            bitmap = loadImageBitmap(imageFile.inputStream())
        } catch (e: Exception) {
            return
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth() // Passe die Modifikatoren an das Layout an
        )
    } else {
        Text(text = "Image not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
    }
}

@Composable
actual fun dynamicSoundfromAssets(filename: String) {
    Text(text="Sound not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

@Composable
actual fun dynamicVideofromAssets(filename: String) {
    Text(text="Video not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

actual fun loadPage(pageId: String) {
    val ps = GlobalProjectState.projectState
    println("Loading page: $pageId")
    ps?.LoadFile(ps.folder + "/pages/" + pageId + ".xml")
}

actual fun openWebPage(url: String) {
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        println("Error opening webpage: ${e.message}")
    }
}