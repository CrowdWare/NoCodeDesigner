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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.theme.ExtendedColors
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxScheme
import org.fife.ui.rtextarea.RTextScrollPane
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.plaf.basic.BasicScrollBarUI
import kotlin.math.min

fun Color.toAwtColor(): java.awt.Color {
    return java.awt.Color(red, green, blue, alpha)
}

fun createEditor(
    textFieldValue: TextFieldValue,
    colors: Colors,
    extendedColors: ExtendedColors,
    syntaxStyle: String,
    configureScheme: (SyntaxScheme) -> Unit,
    setFactory: () -> Unit // Funktion zum Setzen der TokenMakerFactory
): Pair<RSyntaxTextArea, RTextScrollPane> {
    setFactory() // TokenMakerFactory initialisieren

    val textArea = RSyntaxTextArea(20, 60).apply {
        val scheme = SyntaxScheme(true).apply(configureScheme)
        syntaxScheme = scheme
        syntaxEditingStyle = syntaxStyle
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
                SwingUtilities.invokeLater {
                    val oldText = textFieldValue.text
                    val newValue = TextFieldValue(
                        text = text,
                        selection = TextRange(min(caretPosition, text.length))
                    )
                    // aktuelle Datei-Text-Inhalte updaten
                }
            }
        })
    }

    val scrollPane = RTextScrollPane(textArea).apply {
        background = colors.surface.toAwtColor()
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        horizontalScrollBar.ui = createCustomScrollbarUI(colors)
        verticalScrollBar.ui = createCustomScrollbarUI(colors)
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