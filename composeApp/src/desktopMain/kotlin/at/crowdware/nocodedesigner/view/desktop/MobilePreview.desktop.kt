package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import org.jcodec.api.FrameGrab
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.Desktop
import java.awt.image.BufferedImage
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
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Text(text = "Image not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
    }
}

@Composable
actual fun dynamicSoundfromAssets(filename: String) {
    //Text(text="Sound not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

@Composable
actual fun dynamicVideofromAssets(filename: String, height: Int) {
    val ps = GlobalProjectState.projectState
    val path = "${ps?.folder}/assets/$filename"
    var bitmap: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    try {
        val picture: Picture = FrameGrab.getFrameFromFile(File(path), 0)
        println("${picture}, ${picture.size.width}, ${picture.size.height}")
        bitmap = AWTUtil.toBufferedImage(picture)
    } catch (e: Exception) {
        println("${e.message}")
        return
    }
    Image(
        bitmap = bitmap.toComposeImageBitmap(),
        contentDescription = "Video Thumbnail",
        modifier = Modifier.fillMaxWidth().height(height.dp)
    )
}

actual fun loadPage(pageId: String) {
    val ps = GlobalProjectState.projectState
    println("Loading page: $pageId")
    ps?.LoadFile(ps.folder + "/pages/" + pageId)
}

actual fun openWebPage(url: String) {
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        println("Error opening webpage: ${e.message}")
    }
}

@Composable
actual fun dynamicYoutube(height: Int) {
    Image(
        painter = painterResource("icons/youtube.png"),
        contentDescription = "Description of the image",
        modifier = Modifier.fillMaxWidth().height(height.dp)
    )
}