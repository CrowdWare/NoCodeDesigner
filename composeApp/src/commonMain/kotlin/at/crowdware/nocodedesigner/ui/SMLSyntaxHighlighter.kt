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

import at.crowdware.nocodedesigner.utils.Page
import at.crowdware.nocodedesigner.utils.UIElement
import org.fife.ui.rsyntaxtextarea.*
import javax.swing.text.Segment

class PageSyntax {
    private val validElements = setOf(
        "Page",
        "Markdown",
        "Column",
        "Row",
        "Text",
        "Button",
        "Image",
        "Spacer",
        "Video",
        "Youtube",
        "Sound",
        "Scene"
    )

    fun isValidElement(word: String): Boolean {
        return word in validElements
    }

    fun getValidChildElements(parentElement: String): Set<String> {
        return when(parentElement) {
            "Page" -> validElements
            "Row", "Column" -> validElements
            else -> emptySet()
        }
    }

    fun getValidProperties(elementName: String): Map<String, Class<*>> {
        val fields = when(elementName) {
            "Page" -> Page::class.java.declaredFields
            "Text" -> UIElement.TextElement::class.java.declaredFields
            "Button" -> UIElement.ButtonElement::class.java.declaredFields
            else -> emptyArray()
        }

        return fields.filter { !it.isSynthetic }
            .map { it.name to it.type }
            .toMap()
    }
}

class SMLTokenMakerFactory : AbstractTokenMakerFactory() {
    companion object {
        const val SYNTAX_STYLE_SML = "text/sml"
    }

    override fun getTokenMakerImpl(key: String): TokenMaker {
        return when (key) {
            SYNTAX_STYLE_SML -> SMLTokenMaker()
            else -> SMLTokenMaker()
        }
    }

    override fun initTokenMakerMap() {
        putMapping(SYNTAX_STYLE_SML, "at.crowdware.nocodedesigner.syntax.SMLTokenMaker")
    }
}

class SMLTokenMaker : AbstractTokenMaker() {
    override fun getWordsToHighlight(): TokenMap {
        val tokenMap = TokenMap()

        // Füge Keywords hinzu
        val keywords = arrayOf(
            "Page", "Text", "Column", "Row", "Button", "Image",
            "Spacer", "Video", "Youtube", "Sound", "Scene",
            "Markdown", "App", "Theme", "Deployment",
            "Item", "File", "Ebook", "Part", "App"
        )

        for (word in keywords) {
            tokenMap.put(word, Token.RESERVED_WORD)
        }

        // Füge Theme-Properties hinzu
        val themeProperties = arrayOf(
            "error", "scrim", "onError", "background", "errorContainer",
            "inverseOnSurface", "inversePrimary", "inverseSurface",
            "onBackground", "onErrorContainer", "onPrimary",
            "onPrimaryContainer", "onSecondary", "onSecondaryContainer",
            "onSurface", "onSurfaceVariant", "onTertiary",
            "onTertiaryContainer", "outline", "outlineVariant",
            "primary", "surface", "secondary", "surfaceTint",
            "surfaceVariant", "tertiary", "tertiaryContainer"
        )

        for (prop in themeProperties) {
            tokenMap.put(prop, Token.VARIABLE)
        }

        // Füge allgemeine Properties hinzu
        val commonProperties = arrayOf(
            "text", "color", "fontSize", "fontWeight", "textAlign",
            "weight", "height", "width", "padding", "label", "link",
            "backgroundColor", "src", "scale", "amount", "id",
            "ibl", "skybox", "glb", "gltf", "type", "path", "time",
            "name", "smlVersion", "icon", "creator", "language"
        )

        for (prop in commonProperties) {
            tokenMap.put(prop, Token.VARIABLE)
        }

        return tokenMap
    }

    override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
        resetTokenList()

        if (text == null) return firstToken

        val array = text.array
        val offset = text.offset
        val count = text.count
        var current = offset
        val end = offset + count

        var start = current

        while (current < end) {
            val c = array[current]

            when {
                c == '/' && (current + 1) < end && array[current + 1] == '/' -> {
                    // Zeilenkommentar
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current = endOfLineComment(array, current, end)
                    addToken(array, start, current - 1, Token.COMMENT_DOCUMENTATION, startOffset + start - offset)
                    start = current
                }

                c == '/' && (current + 1) < end && array[current + 1] == '*' -> {
                    // Blockkommentar
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current = endOfBlockComment(array, current, end)
                    addToken(array, start, current - 1, Token.COMMENT_MULTILINE, startOffset + start - offset)
                    start = current
                }

                c == '"' -> {
                    // String literal
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current = endOfString(array, current, end)
                    addToken(array, start, current - 1, Token.LITERAL_STRING_DOUBLE_QUOTE, startOffset + start - offset)
                    start = current
                }

                c == '{' || c == '}' || c == ':' -> {
                    // Separatoren
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    addToken(array, current, current, Token.SEPARATOR, startOffset + current - offset)
                    current++
                    start = current
                }

                Character.isWhitespace(c) -> {
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    addToken(array, current, current, Token.WHITESPACE, startOffset + current - offset)
                    current++
                    start = current
                }

                else -> current++
            }
        }

        if (start < end) {
            addToken(array, start, end - 1, Token.IDENTIFIER, startOffset + start - offset)
        }

        addNullToken()
        return firstToken
    }

    private fun endOfLineComment(array: CharArray, start: Int, end: Int): Int {
        var current = start
        while (current < end) {
            if (array[current] == '\n') break
            current++
        }
        return current
    }

    private fun endOfBlockComment(array: CharArray, start: Int, end: Int): Int {
        var current = start + 2
        while (current < end - 1) {
            if (array[current] == '*' && array[current + 1] == '/') {
                return current + 2
            }
            current++
        }
        return end
    }

    private fun endOfString(array: CharArray, start: Int, end: Int): Int {
        var current = start + 1
        while (current < end) {
            if (array[current] == '"' && array[current - 1] != '\\') {
                return current + 1
            }
            current++
        }
        return current
    }
}