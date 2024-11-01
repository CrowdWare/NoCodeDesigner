package at.crowdware.nocodedesigner.ui

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenMap
import org.fife.ui.rsyntaxtextarea.TokenTypes
import java.util.stream.IntStream.range
import javax.swing.text.Segment

class MarkdownTokenMaker : AbstractTokenMaker() {
    override fun getWordsToHighlight(): TokenMap {
        val tokenMap = TokenMap()

        val keywords = arrayOf("**", "*", "#", "`", "[]()", "[]")

        for (word in keywords) {
            tokenMap.put(word, Token.RESERVED_WORD)
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
                c == '#' && (current == offset || array[current - 1] == '\n') -> {
                    // Header (#)
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current = endOfHeader(array, current, end)
                    addToken(array, start, current - 1, Token.RESERVED_WORD, startOffset + start - offset)
                    start = current
                }

                c == '*' -> {
                    // Bold or Italic
                    if (current + 1 < end && array[current + 1] == '*') {
                        addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                        start = current
                        current += 2
                        current = endOfBoldText(array, current, end)
                        addToken(array, start, current - 1, Token.MARKUP_TAG_DELIMITER, startOffset + start - offset)
                        start = current
                    } else {
                        addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                        start = current
                        current++
                        current = endOfItalicText(array, current, end)
                        addToken(array, start, current - 1, Token.MARKUP_TAG_DELIMITER, startOffset + start - offset)
                        start = current
                    }
                }

                c == '`' -> {
                    // Code block
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current = endOfCodeBlock(array, current, end)
                    addToken(array, start, current - 1, Token.LITERAL_BACKQUOTE, startOffset + start - offset)
                    start = current
                }

                c == '[' -> {
                    // Start of a link
                    addToken(array, start, current - 1, Token.IDENTIFIER, startOffset + start - offset)
                    start = current
                    current++ // Move past the '['

                    // Capture the link name until we find the closing bracket
                    while (current < end && array[current] != ']') {
                        current++
                    }

                    if (current < end && array[current] == ']') {
                        // Found the closing bracket
                        // Now, we need to style the link name
                        addToken(array, start, current, Token.MARKUP_TAG_NAME, startOffset + start - offset) // This token could represent the link name styled in green
                        start = current + 1 // Move past the ']'
                        current++ // Move to the next character after ']'

                        // Check for the URL after the closing bracket
                        if (current < end && array[current] == '(') {
                            start = current // Remember where the URL starts
                            current++ // Move past the '('

                            // Capture the URL until we find the closing parenthesis
                            while (current < end && array[current] != ')') {
                                current++
                            }

                            if (current < end && array[current] == ')') {
                                // Found the closing parenthesis for the URL
                                addToken(array, start, current, Token.MARKUP_TAG_ATTRIBUTE, startOffset + start - offset) // Token for the URL
                                start = current + 1 // Move past the ')'
                            }
                        }
                    }
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

    private fun endOfHeader(array: CharArray, start: Int, end: Int): Int {
        var current = start
        while (current < end && array[current] != '\n') {
            current++
        }
        return current
    }

    private fun endOfBoldText(array: CharArray, start: Int, end: Int): Int {
        var current = start
        while (current < end - 1) {
            if (array[current] == '*' && array[current + 1] == '*') {
                return current + 2
            }
            current++
        }
        return end
    }

    private fun endOfItalicText(array: CharArray, start: Int, end: Int): Int {
        var current = start
        while (current < end) {
            if (array[current] == '*') {
                return current + 1
            }
            current++
        }
        return end
    }

    private fun endOfCodeBlock(array: CharArray, start: Int, end: Int): Int {
        var current = start + 1
        while (current < end) {
            if (array[current] == '`') {
                return current + 1
            }
            current++
        }
        return end
    }

    private fun endOfLink(array: CharArray, start: Int, end: Int): Int {
        var current = start
        while (current < end) {
            if (array[current] == ']') {
                current++
                if (current < end && array[current] == '(') {
                    while (current < end && array[current] != ')') {
                        current++
                    }
                    return current + 1
                }
            }
            current++
        }
        return end
    }
}