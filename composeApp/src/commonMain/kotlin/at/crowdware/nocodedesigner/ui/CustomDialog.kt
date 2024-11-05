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
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow
import at.crowdware.nocodedesigner.theme.AppTheme
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.theme.darkenColor
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState.projectState
import javax.swing.JFrame

@Composable
fun CustomDialog(
    title: String,
    height: Int,
    onDismissRequest: () -> Unit,
    onConfirmRequest: () -> Unit,
    confirmButtonText: String = "Confirm",
    cancelButtonText: String = "",
    content: @Composable () -> Unit
) {
    val dlgState = remember { mutableStateOf(DialogState(width = 500.dp,height = height.dp)) }
    val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)

    DialogWindow(
        onCloseRequest = onDismissRequest,
        state = dlgState.value,
        undecorated = !isWindows,
        transparent = !isWindows,
        resizable = false
    ) {
        projectState?.let {
            AppTheme(darkTheme = it.darkMode) {
                Surface(
                    color = darkenColor(MaterialTheme.colors.primary, 0.8f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            color = MaterialTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        content()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Spacer(modifier = Modifier.weight(1f))
                            if (cancelButtonText.isNotEmpty()) {
                                Button(onClick = onDismissRequest) {
                                    Text(cancelButtonText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = onConfirmRequest,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = ExtendedTheme.colors.accentColor,
                                    contentColor = ExtendedTheme.colors.onAccentColor
                                )
                            ) {
                                Text(confirmButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun InputRow(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    hasIcon: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colors.onPrimary,
            modifier = Modifier.align(Alignment.CenterVertically).weight(1F)
        )
        Spacer(modifier = Modifier.width(16.dp))
        TextInput(value, onValueChange, modifier = Modifier.weight(3F), hasIcon = hasIcon)
    }
}