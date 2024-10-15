package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker


@Composable
fun TextInput(text: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, hasIcon: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var showDirectoryPicker by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) {
        ExtendedTheme.colors.accentColor  // Farbe bei Fokus
    } else {
        MaterialTheme.colors.onPrimary    // Standardfarbe
    }

    val customSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        Row(
            modifier = modifier.border(
                width = 1.dp,
                color = borderColor,  // Dynamische Farbe je nach Fokus
                shape = RoundedCornerShape(4.dp)
            )
        ) {
            BasicTextField(
                value = text,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.primary)
                    .padding(4.dp)
                    .focusable(interactionSource = interactionSource),
                interactionSource = interactionSource
            )
            if (hasIcon) {
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Folder, contentDescription = "Folder", modifier = Modifier.clickable {
                        showDirectoryPicker = true
                    })
                }
            }
        }
    }

    DirectoryPicker(showDirectoryPicker, title = "Pick a folder") { path ->
        if (!path.isNullOrEmpty()) {
            showDirectoryPicker = false
            onValueChange(path)
        }
    }
}

