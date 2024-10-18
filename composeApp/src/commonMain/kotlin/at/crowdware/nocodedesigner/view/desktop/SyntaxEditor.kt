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

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.ui.CustomSelectionColors
import at.crowdware.nocodedesigner.ui.SyntaxTextField
import at.crowdware.nocodedesigner.ui.ioDispatcher
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RowScope.syntaxEditor(
    currentProject: ProjectState?,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    if (currentProject != null && currentProject.isEditorVisible) {
        Column(modifier = Modifier.weight(1F).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1F).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
                BasicText(
                    text = currentProject.fileName + "",
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1,
                    style = TextStyle(color = MaterialTheme.colors.onPrimary),
                    overflow = TextOverflow.Ellipsis
                )
                SyntaxTextField(
                    onValueChange = { newValue ->
                            val oldText = textFieldValue.text
                            onTextFieldValueChange(newValue)
                            currentProject.currentFileContent = newValue
                            // don't save if only the cursor has moved (no text has changed)
                            if (oldText != newValue.text) {
                                // Automatically save the content to disk after each change
                                coroutineScope.launch(ioDispatcher()) {
                                    delay(500)
                                    currentProject.saveFileContent()
                                    when (currentProject.path.substringAfterLast("/")) {
                                        "app.sml" -> {currentProject.loadApp()}
                                        "book.sml" -> {currentProject.loadBook()}
                                        else -> {currentProject.reloadPage()}
                                    }
                                }
                        }
                    },
                    extension = currentProject.extension ?: "",
                    textFieldValue = textFieldValue
                )
                BasicTextField(
                    "Test", onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    maxLines = 30,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = ExtendedTheme.colors.attributeNameColor,
                        fontFamily = FontFamily.Monospace
                    ),
                )
            }
            if (currentProject.parseError != null) {
                CustomSelectionColors {
                    BasicTextField(
                        value = currentProject.parseError!!,
                        modifier = Modifier.fillMaxWidth().weight(.1f).padding(8.dp)
                            .background(MaterialTheme.colors.surface),
                        onValueChange = {},
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface,
                            fontFamily = FontFamily.Monospace
                        ),
                        //fontSize = 12.sp,
                        //color = MaterialTheme.colors.onSurface
                        maxLines = 200
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().weight(1F), contentAlignment = Alignment.Center) {
            Text(text = "No file open")
        }
    }
}
