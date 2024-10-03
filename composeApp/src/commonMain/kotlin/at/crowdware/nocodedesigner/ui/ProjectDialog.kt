package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun projectDialog(
    name: String,
    onNameChange: (String) -> Unit,
    folder: String,
    onFolderChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create Project")
        },
        text = {
            Column(modifier = Modifier.width(400.dp).padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Name:", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(name, onNameChange, modifier = Modifier.width(350.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Projektordner
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Folder:", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(folder, onFolderChange, modifier = Modifier.width(350.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ExtendedTheme.colors.accentColor,
                    contentColor = ExtendedTheme.colors.onAccentColor
                )
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TextInput(text: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
                .border(
                    width = 1.dp,
                    color = borderColor,  // Dynamische Farbe je nach Fokus
                    shape = RoundedCornerShape(4.dp)
                )
                .fillMaxWidth()
                .background(color = MaterialTheme.colors.primary)
                .padding(4.dp)
                .focusable(interactionSource = interactionSource),
            interactionSource = interactionSource
        )
    }
}


/*
@Composable
fun renderProjectDialogContent(onDismissRequest: () -> Unit, onCreateRequest: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Name",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.width(300.dp),
            label = {Text("Name")}
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Location",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface
        )
        TextField(
            value = location,
            onValueChange = { location = it },
            modifier = Modifier.width(300.dp),
            trailingIcon = { Icon(Icons.Default.Folder, contentDescription = "Folder", modifier = Modifier.clickable {
                openFolder()
            }) },
            label = {Text("Location")}
        )
    }
}

@Composable
expect fun renderProjectDialog(onDismissRequest: () -> Unit, onCreateRequest: () -> Unit)

expect fun openFolder()

 */