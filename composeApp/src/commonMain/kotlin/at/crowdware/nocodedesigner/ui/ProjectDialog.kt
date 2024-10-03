package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun projectDialog(onDismissRequest: () -> Unit, onCreateRequest: () -> Unit) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create Project")
        },
        text = {
            Column {
                Text(text = "Please enter the project name:")
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Project Name") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateRequest },
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