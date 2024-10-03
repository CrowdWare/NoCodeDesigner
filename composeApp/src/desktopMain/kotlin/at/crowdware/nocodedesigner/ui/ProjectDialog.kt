
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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import at.crowdware.nocodedesigner.theme.AppTheme
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ProjectDialog( onDismissRequest: () -> Unit, onCreateRequest: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    DialogWindow(
        onCloseRequest = onDismissRequest,
        visible = true,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false
    ) {
        AppTheme() {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(0.5.dp, Color.Gray, RoundedCornerShape(10.dp)),
                color = MaterialTheme.colors.primary,
                shape = RoundedCornerShape(10.dp) //window has round corners now
            ) {
                Column {
                    WindowDraggableArea {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(ExtendedTheme.colors.captionColor)
                                .padding(start = 12.dp, top = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WindowControlButton(color = Color(255, 92, 92), onClick = onDismissRequest)
                            }

                            Text(
                                text = "New Project",
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column ( modifier = Modifier.fillMaxWidth().padding(16.dp)){
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
                                label = {Text("Location")}
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(onClick = onDismissRequest) {
                            Text(
                                "Cancel",
                                style = MaterialTheme.typography.button,
                                color = MaterialTheme.colors.onSecondary
                            )
                        }
                        Button(
                            onClick = onCreateRequest,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = ExtendedTheme.colors.accentColor,
                                contentColor = ExtendedTheme.colors.onAccentColor
                            )
                        ) {
                            Text(
                                "Create",
                                style = MaterialTheme.typography.button
                            )
                        }
                    }
                }
            }
        }
    }
}



/*

                            try {
                                // Call the native Swift method
                                val folderPathPointer = macLib.selectFolder(darkMode)

                                // Check if the pointer is null
                                if (folderPathPointer == null) {
                                    println("No folder selected or an error occurred in the Swift function.")
                                } else {
                                    // Convert the result from a Pointer to a String
                                    val folderPath = folderPathPointer.getString(0)
                                    println("Selected folder: $folderPath")
                                    projectState.CreateProject(folderPath, "", "")
                                }
                            } catch (e: Exception) {
                                // Catch any exceptions that happen during the JNA call
                                println("Error calling native function: ${e.message}")
                                e.printStackTrace()

 */
