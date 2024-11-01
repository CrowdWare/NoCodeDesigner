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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedColors
import at.crowdware.nocodedesigner.ui.SMLTokenMakerFactory
import at.crowdware.nocodedesigner.ui.MarkdownTokenMakerFactory
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fife.ui.rsyntaxtextarea.*
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.math.min

fun Color.toAwtColor(): java.awt.Color {
    return java.awt.Color(red, green, blue, alpha)
}

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
                    AbstractTokenMakerFactory.setDefaultInstance(SMLTokenMakerFactory())

                    val textArea = RSyntaxTextArea(20, 60).apply {
                        val scheme = SyntaxScheme(true).apply {
                            styles[Token.RESERVED_WORD] = Style(extendedColors.syntaxColor.toAwtColor())
                            styles[Token.SEPARATOR] = Style(extendedColors.bracketColor.toAwtColor())
                            styles[Token.IDENTIFIER] = Style(extendedColors.attributeNameColor.toAwtColor())
                            styles[Token.LITERAL_STRING_DOUBLE_QUOTE] = Style(extendedColors.attributeValueColor.toAwtColor())
                            styles[Token.MARKUP_TAG_DELIMITER] = Style(extendedColors.syntaxColor.toAwtColor())
                            styles[Token.MARKUP_TAG_ATTRIBUTE] = Style(extendedColors.attributeNameColor.toAwtColor())
                        }
                        syntaxScheme = scheme
                        syntaxEditingStyle = SMLTokenMakerFactory.SYNTAX_STYLE_SML
                        background = colors.surface.toAwtColor()
                        foreground = colors.onSurface.toAwtColor()
                        currentLineHighlightColor = colors.surface.copy(
                            red = colors.surface.red + 0.05f,
                            green = colors.surface.green + 0.05f,
                            blue = colors.surface.blue + 0.05f
                        ).toAwtColor()
                        caretColor = colors.onSurface.toAwtColor()
                        selectionColor = extendedColors.accentColor.toAwtColor()
                        selectedTextColor = java.awt.Color.WHITE
                        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14)
                        text = textFieldValue.text
                        caretPosition = 0
                        isFocusable = true
                        isRequestFocusEnabled = true
                        paintTabLines = true

                        addMouseListener(object : MouseAdapter() {
                            override fun mousePressed(e: MouseEvent) {
                                try {
                                    val pos = viewToModel(e.point)
                                    // Ensure position is within valid range
                                    if (pos >= 0 && pos <= document.length) {
                                        caretPosition = pos
                                        requestFocusInWindow()
                                    }
                                } catch (ex: Exception) {
                                    // Ignore invalid positions
                                }
                            }
                        })

                        document.addDocumentListener(object : DocumentListener {
                            override fun insertUpdate(e: DocumentEvent) = updateValue()
                            override fun removeUpdate(e: DocumentEvent) = updateValue()
                            override fun changedUpdate(e: DocumentEvent) = updateValue()

                            fun updateValue() {
                                // invoke after caretPos has been changed
                                SwingUtilities.invokeLater {
                                    val oldText = textFieldValue.text
                                    val newValue = TextFieldValue(
                                        text = text,
                                        selection = TextRange(min(caretPosition, text.length))
                                    )
                                    currentProject.currentFileContent = newValue

                                    if (oldText != text) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            delay(500)
                                            currentProject.saveFileContent()
                                            when (currentProject.path.substringAfterLast("/")) {
                                                "app.sml" -> {
                                                    currentProject.loadApp()
                                                }

                                                "book.sml" -> {
                                                    currentProject.loadBook()
                                                }

                                                else -> {
                                                    currentProject.reloadPage()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                    textArea to RTextScrollPane(textArea)
                }
                val mdEditor = remember {
                    AbstractTokenMakerFactory.setDefaultInstance(MarkdownTokenMakerFactory())

                    val textArea = RSyntaxTextArea(20, 60).apply {
                        val scheme = SyntaxScheme(true).apply {
                            styles[Token.RESERVED_WORD] = Style(extendedColors.syntaxColor.toAwtColor())
                            styles[Token.SEPARATOR] = Style(extendedColors.bracketColor.toAwtColor())
                            styles[Token.IDENTIFIER] = Style(extendedColors.attributeNameColor.toAwtColor())
                            styles[Token.LITERAL_STRING_DOUBLE_QUOTE] = Style(extendedColors.attributeValueColor.toAwtColor())
                            styles[Token.MARKUP_TAG_DELIMITER] = Style(extendedColors.syntaxColor.toAwtColor())
                            styles[Token.MARKUP_TAG_ATTRIBUTE] = Style(extendedColors.attributeNameColor.toAwtColor())
                        }
                        syntaxScheme = scheme
                        syntaxEditingStyle = MarkdownTokenMakerFactory.SYNTAX_STYLE_MARKDOWN
                        background = colors.surface.toAwtColor()
                        foreground = colors.onSurface.toAwtColor()
                        currentLineHighlightColor = colors.surface.copy(
                            red = colors.surface.red + 0.05f,
                            green = colors.surface.green + 0.05f,
                            blue = colors.surface.blue + 0.05f
                        ).toAwtColor()
                        caretColor = colors.onSurface.toAwtColor()
                        selectionColor = extendedColors.accentColor.toAwtColor()
                        selectedTextColor = java.awt.Color.WHITE
                        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14)
                        text = textFieldValue.text
                        caretPosition = 0
                        isFocusable = true
                        isRequestFocusEnabled = true
                        paintTabLines = true

                        addMouseListener(object : MouseAdapter() {
                            override fun mousePressed(e: MouseEvent) {
                                try {
                                    val pos = viewToModel(e.point)
                                    // Ensure position is within valid range
                                    if (pos >= 0 && pos <= document.length) {
                                        caretPosition = pos
                                        requestFocusInWindow()
                                    }
                                } catch (ex: Exception) {
                                    // Ignore invalid positions
                                }
                            }
                        })

                        document.addDocumentListener(object : DocumentListener {
                            override fun insertUpdate(e: DocumentEvent) = updateValue()
                            override fun removeUpdate(e: DocumentEvent) = updateValue()
                            override fun changedUpdate(e: DocumentEvent) = updateValue()

                            fun updateValue() {
                                // invoke after caretPos has been changed
                                SwingUtilities.invokeLater {
                                    val oldText = textFieldValue.text
                                    val newValue = TextFieldValue(
                                        text = text,
                                        selection = TextRange(min(caretPosition, text.length))
                                    )
                                    currentProject.currentFileContent = newValue

                                    if (oldText != text) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            delay(500)
                                            currentProject.saveFileContent()
                                            currentProject.reloadPage()
                                        }
                                    }
                                }
                            }
                        })
                    }
                    textArea to RTextScrollPane(textArea)
                }

                if (textFieldValue.text.isNotEmpty()) {
                    if (currentProject.fileName.endsWith(".sml")) {
                        SwingPanel(
                            modifier = Modifier.fillMaxSize(),
                            factory = { smlEditor.second }
                        )
                    } else {
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
