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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedColors
import at.crowdware.nocodedesigner.ui.MarkdownTokenMakerFactory
import at.crowdware.nocodedesigner.ui.SMLTokenMakerFactory
import at.crowdware.nocodedesigner.ui.createEditor
import at.crowdware.nocodedesigner.ui.toAwtColor
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import kotlinx.coroutines.delay
import org.fife.ui.rsyntaxtextarea.*


@Composable
fun RowScope.syntaxEditor(
    currentProject: ProjectState?,
    textFieldValue: TextFieldValue,
    colors: Colors,
    extendedColors: ExtendedColors
) {
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

                val smlEditor = remember {
                    createEditor(
                        textFieldValue,
                        colors,
                        extendedColors,
                        SMLTokenMakerFactory.SYNTAX_STYLE_SML,
                        { scheme ->
                            scheme.styles[Token.RESERVED_WORD] = Style(extendedColors.syntaxColor.toAwtColor())
                            scheme.styles[Token.SEPARATOR] = Style(extendedColors.bracketColor.toAwtColor())
                            scheme.styles[Token.IDENTIFIER] = Style(extendedColors.attributeNameColor.toAwtColor())
                            scheme.styles[Token.LITERAL_STRING_DOUBLE_QUOTE] = Style(extendedColors.attributeValueColor.toAwtColor())
                        },
                        { AbstractTokenMakerFactory.setDefaultInstance(SMLTokenMakerFactory()) },
                        currentProject
                    )
                }

                val mdEditor = remember {
                    createEditor(
                        textFieldValue,
                        colors,
                        extendedColors,
                        MarkdownTokenMakerFactory.SYNTAX_STYLE_MARKDOWN,
                        { scheme ->
                            scheme.styles[Token.RESERVED_WORD] = Style(extendedColors.syntaxColor.toAwtColor())
                            scheme.styles[Token.SEPARATOR] = Style(extendedColors.bracketColor.toAwtColor())
                            scheme.styles[Token.IDENTIFIER] = Style(extendedColors.attributeNameColor.toAwtColor())
                            scheme.styles[Token.LITERAL_STRING_DOUBLE_QUOTE] = Style(extendedColors.attributeValueColor.toAwtColor())
                            scheme.styles[Token.MARKUP_TAG_DELIMITER] = Style(extendedColors.attributeValueColor.toAwtColor())
                            scheme.styles[Token.MARKUP_TAG_ATTRIBUTE] = Style(extendedColors.linkColor.toAwtColor())
                            scheme.styles[Token.MARKUP_TAG_NAME] = Style(extendedColors.attributeNameColor.toAwtColor())
                        },
                        { AbstractTokenMakerFactory.setDefaultInstance(MarkdownTokenMakerFactory()) },
                        currentProject
                    )
                }

                if (textFieldValue.text.isNotEmpty()) {
                    if (currentProject.fileName.endsWith(".sml")) {
                        currentProject.editor = smlEditor.first
                        SwingPanel(
                            modifier = Modifier.fillMaxSize(),
                            factory = { smlEditor.second }
                        )
                    } else {
                        currentProject.editor = mdEditor.first
                        SwingPanel(
                            modifier = Modifier.fillMaxSize(),
                            factory = { mdEditor.second }
                        )
                    }
                }

                LaunchedEffect(textFieldValue.text) {
                    if (smlEditor.first.text != textFieldValue.text && currentProject.fileName.endsWith(".sml")) {
                        smlEditor.first.text = textFieldValue.text
                    }
                    if (mdEditor.first.text != textFieldValue.text && currentProject.fileName.endsWith(".md")) {
                        mdEditor.first.text = textFieldValue.text
                    }
                }
            }
            if (currentProject.parseError != null) {
                CustomSelectionColors {
                    val scrollState = rememberScrollState()
                    Row (modifier = Modifier.weight(.5f)){
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(scrollState)
                        ) {
                            BasicTextField(
                                value = currentProject.parseError!!,
                                modifier = Modifier
                                    .fillMaxWidth().padding(8.dp)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(8.dp),
                                onValueChange = {},
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface,
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 200
                            )
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(8.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                    LaunchedEffect(currentProject.parseError) {
                        delay(1000)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().weight(1F), contentAlignment = Alignment.Center) {
            Text(text = "No file open")
        }
    }
}

@Composable
fun CustomSelectionColors(content: @Composable () -> Unit) {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.Magenta,
        backgroundColor = Color.LightGray.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        content()
    }
}