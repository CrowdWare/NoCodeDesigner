package at.crowdware.nocodedesigner.ui

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker
import org.fife.ui.rsyntaxtextarea.Token
import org.fife.ui.rsyntaxtextarea.TokenMap
import org.fife.ui.rsyntaxtextarea.TokenTypes
import javax.swing.text.Segment

class MarkdownTokenMaker : AbstractTokenMaker() {
    
    companion object {
        private val KEYWORDS = arrayOf(
            "#", "##", "###", "####", "#####", "######",  // Headers
            "*", "**", "_", "__",                         // Emphasis
            "-", "+", ">",                                // Lists and blockquotes
            "`", "```",                                   // Code blocks
            "[", "]", "(", ")",                          // Links
            "!", "---", "***"                            // Images and horizontal rules
        )
    }

    override fun getWordsToHighlight(): TokenMap {
        val words = TokenMap()
        for (keyword in KEYWORDS) {
            words.put(keyword, Token.RESERVED_WORD)
        }
        return words
    }

    override fun getTokenList(text: Segment?, initialTokenType: Int, startOffset: Int): Token {
        resetTokenList()
        
        if (text == null) {
            addNullToken()
            return firstToken
        }

        var offset = text.offset
        val count = text.count
        val end = offset + count

        var start = offset
        var current = offset

        while (current < end) {
            val c = text.array[current]

            when {
                c == '#' -> {
                    // Handle headers
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    start = current
                    while (current < end && text.array[current] == '#') {
                        current++
                    }
                    addToken(text, start, current - 1, Token.RESERVED_WORD, startOffset + start)
                    start = current
                }

                c == '*' || c == '_' -> {
                    // Handle emphasis
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    start = current
                    if (current + 1 < end && text.array[current + 1] == c) {
                        current += 2
                    } else {
                        current++
                    }
                    addToken(text, start, current - 1, Token.MARKUP_TAG_DELIMITER, startOffset + start)
                    start = current
                }

                c == '`' -> {
                    // Handle code blocks
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    start = current
                    if (current + 2 < end && 
                        text.array[current + 1] == '`' && 
                        text.array[current + 2] == '`') {
                        current += 3
                        while (current + 2 < end) {
                            if (text.array[current] == '`' && 
                                text.array[current + 1] == '`' && 
                                text.array[current + 2] == '`') {
                                current += 3
                                break
                            }
                            current++
                        }
                    } else {
                        current++
                        while (current < end && text.array[current] != '`') {
                            current++
                        }
                        if (current < end) current++
                    }
                    addToken(text, start, current - 1, Token.LITERAL_STRING_DOUBLE_QUOTE, startOffset + start)
                    start = current
                }

                c == '[' -> {
                    // Handle opening bracket
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    addToken(text, current, current, Token.SEPARATOR, startOffset + current)
                    current++
                    start = current
                }

                c == ']' -> {
                    // Handle closing bracket
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    addToken(text, current, current, Token.SEPARATOR, startOffset + current)
                    current++
                    start = current
                }

                c == '(' -> {
                    // Handle opening parenthesis
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    addToken(text, current, current, Token.SEPARATOR, startOffset + current)
                    current++
                    start = current
                }

                c == ')' -> {
                    // Handle closing parenthesis
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    addToken(text, current, current, Token.SEPARATOR, startOffset + current)
                    current++
                    start = current
                }

                c == '-' && current + 2 < end && 
                text.array[current + 1] == '-' && 
                text.array[current + 2] == '-' -> {
                    // Handle horizontal rules
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    current += 3
                    addToken(text, start, current - 1, Token.RESERVED_WORD, startOffset + start)
                    start = current
                }

                Character.isWhitespace(c) -> {
                    if (start < current) {
                        addToken(text, start, current - 1, Token.IDENTIFIER, startOffset + start)
                    }
                    addToken(text, current, current, Token.WHITESPACE, startOffset + current)
                    current++
                    start = current
                }

                else -> current++
            }
        }

        if (start < end) {
            addToken(text, start, end - 1, Token.IDENTIFIER, startOffset + start)
        }

        addNullToken()
        return firstToken
    }
}
