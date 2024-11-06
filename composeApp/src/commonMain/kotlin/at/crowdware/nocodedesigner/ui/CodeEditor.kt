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

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.theme.ExtendedColors
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import java.util.Timer
import javax.swing.*
import javax.swing.border.LineBorder
import javax.swing.border.MatteBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicScrollBarUI
import kotlin.math.min

fun Color.toAwtColor(): java.awt.Color {
    return java.awt.Color(red, green, blue, alpha)
}

private const val DEBOUNCE_DELAY = 300L
private var debounceTimer: Timer? = null

fun createEditor(
    textFieldValue: TextFieldValue,
    colors: Colors,
    extendedColors: ExtendedColors,
    syntaxStyle: String,
    configureScheme: (SyntaxScheme) -> Unit,
    setFactory: () -> Unit,
    currentProject: ProjectState
): Pair<RSyntaxTextArea, RTextScrollPane> {
    setFactory()

    val textArea = RSyntaxTextArea(20, 60).apply {
        val scheme = SyntaxScheme(true).apply(configureScheme)
        syntaxScheme = scheme
        syntaxEditingStyle = syntaxStyle
        background = colors.surface.toAwtColor()
        foreground = colors.onSurface.toAwtColor()
        currentLineHighlightColor = colors.surface.copy(
            red = min(colors.surface.red + 0.05f, 1f),
            green = min(colors.surface.green + 0.05f, 1f),
            blue = min(colors.surface.blue + 0.05f, 1f)
        ).toAwtColor()
        caretColor = colors.onSurface.toAwtColor()
        selectionColor = extendedColors.selectionColor.toAwtColor()
        selectedTextColor = extendedColors.onSelectionColor.toAwtColor()
        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14)
        text = textFieldValue.text
        caretPosition = 0
        isFocusable = true
        isRequestFocusEnabled = true
        paintTabLines = true
        border = BorderFactory.createEmptyBorder(5, 10, 10, 10)

        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                try {
                    val pos = viewToModel(e.point)
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
            private var lastText: String = text

            override fun insertUpdate(e: DocumentEvent) = scheduleUpdate()
            override fun removeUpdate(e: DocumentEvent) = scheduleUpdate()
            override fun changedUpdate(e: DocumentEvent) = scheduleUpdate()

            private fun scheduleUpdate() {
                debounceTimer?.cancel() // Cancel any existing timer
                debounceTimer = Timer()

                // Schedule a new timer task
                debounceTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        SwingUtilities.invokeLater {
                            updateValue() // This will run after the user stops typing
                        }
                    }
                }, DEBOUNCE_DELAY)
            }

            // Update function for handling document changes
            private fun updateValue() {
                val currentText = text
                val currentCaretPosition = caretPosition
                val currentTextLength = currentText.length

                // Only update if the text has actually changed
                if (lastText != currentText) {
                    // Create a new TextFieldValue with the current text and caret position
                    val newValue = TextFieldValue(
                        text = currentText,
                        selection = TextRange(min(currentCaretPosition, currentTextLength))
                    )

                    currentProject.currentFileContent = newValue
                    lastText = currentText // Update lastText to the current one

                    currentProject.saveFileContent()
                    currentProject.reloadPage()
                }
            }
        })
    }

    val scrollPane = RTextScrollPane(textArea).apply {
        background = colors.surface.toAwtColor()
        gutter.background = colors.surface.toAwtColor()
        viewport.background = colors.surface.toAwtColor()
        border = MatteBorder(5, 5, 5, 5, colors.surface.toAwtColor())
        horizontalScrollBar.background = colors.surface.toAwtColor()
        verticalScrollBar.background = colors.surface.toAwtColor()
        horizontalScrollBar.ui = createCustomScrollbarUI(colors)
        verticalScrollBar.ui = createCustomScrollbarUI(colors)
        setCorner(JScrollPane.UPPER_RIGHT_CORNER, JPanel().apply { background = colors.surface.toAwtColor() })
        setCorner(JScrollPane.LOWER_LEFT_CORNER, JPanel().apply { background = colors.surface.toAwtColor() })
        setCorner(JScrollPane.LOWER_RIGHT_CORNER, JPanel().apply { background = colors.surface.toAwtColor() })

    }
    return textArea to scrollPane
}

fun createCustomScrollbarUI(colors: Colors) = object : BasicScrollBarUI() {
    override fun configureScrollBarColors() {
        thumbColor = colors.primary.toAwtColor()
        trackColor = colors.surface.toAwtColor()
    }

    override fun createIncreaseButton(direction: Int) = object : JButton() {
        init { isVisible = false; preferredSize = Dimension(0, 0) }
    }

    override fun createDecreaseButton(direction: Int) = object : JButton() {
        init { isVisible = false; preferredSize = Dimension(0, 0) }
    }
}