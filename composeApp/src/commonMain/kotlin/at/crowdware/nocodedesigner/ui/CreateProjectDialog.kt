/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeDesigner.
 *
 *  NoCodeDesigner is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeDesigner is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeDesigner.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.theme.ExtendedTheme

@Composable
fun createProjectDialog(
    name: String,
    onNameChange: (String) -> Unit,
    folder: String,
    onFolderChange: (String) -> Unit,
    id: String,
    onIdChange: (String) -> Unit,
    theme: String,
    onThemeChanged: (String) -> Unit,
    onCheckBookChanged: (Boolean) -> Unit,
    onCheckAppChanged: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit,
    app: Boolean,
    book: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create Project")
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Type:", modifier = Modifier.align(Alignment.CenterVertically).weight(1f))
                    CheckboxItem(
                        modifier = Modifier.weight(1f),
                        checked = book,
                        onCheckedChange = onCheckBookChanged,
                        label = "Book")
                    CheckboxItem(modifier = Modifier.weight(1f),
                        checked = app,
                        onCheckedChange = onCheckAppChanged,
                        label = "App")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Name:", modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(name, onNameChange, modifier = Modifier.weight(3F))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if(app) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "AppId:", modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                        Spacer(modifier = Modifier.width(16.dp))
                        TextInput(id, onIdChange, modifier = Modifier.weight(3F))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Folder:", modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(folder, onFolderChange, modifier = Modifier.weight(3F), hasIcon = true)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (app) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Theme:", modifier = Modifier.align(Alignment.CenterVertically).weight(1f))
                        RadioButtonItem(modifier = Modifier.weight(1f),
                            label = "Light",
                            selected = theme == "Light",
                            onClick = { onThemeChanged("Light") }
                        )
                        RadioButtonItem(modifier = Modifier.weight(1f),
                            label = "Dark",
                            selected = theme == "Dark",
                            onClick = { onThemeChanged("Dark") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = (book || app) && name.isNotEmpty() && folder.isNotEmpty() && (id.isNotEmpty() || !app),
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



