package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import java.io.InputStream

@Composable
fun aboutDialog(appName: String, version: String,
    onDismissRequest: () -> Unit,
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "About NoCodeDesigner")
        },
        text = {
            /*
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Name:", modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                    Spacer(modifier = Modifier.width(16.dp))

                }
            }*/
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Column{
                    val icnsIcon = loadPngIcon("/icons/icon.png")
                    IconDisplay(icnsIcon)
                }
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    //horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$appName $version",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Copyright © 2024 CrowdWare", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("All rights reserved.", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ExtendedTheme.colors.accentColor,
                    contentColor = ExtendedTheme.colors.onAccentColor
                )
            ) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun loadPngIcon(resourcePath: String): ImageBitmap? {
    return try {
        val inputStream: InputStream = object {}.javaClass.getResourceAsStream(resourcePath)
        loadImageBitmap(inputStream)  // Load .png image instead
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}