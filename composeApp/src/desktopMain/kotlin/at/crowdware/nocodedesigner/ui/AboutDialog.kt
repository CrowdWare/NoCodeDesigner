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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import at.crowdware.nocodedesigner.theme.AppTheme
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import java.io.InputStream

@Composable
fun AboutDialog(appName: String, version: String, onDismissRequest: () -> Unit) {
    DialogWindow(
        onCloseRequest = onDismissRequest,
        visible = true,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false) {
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
                                text = "About " + appName,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
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
                    Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
                        Button( onClick = onDismissRequest) {
                            Text("Close",style = MaterialTheme.typography.button, color = MaterialTheme.colors.onSecondary)
                        }
                    }
                }
            }
        }
    }
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