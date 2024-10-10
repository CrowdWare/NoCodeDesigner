package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.utils.uiStates
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
}

sealed class ParsedElement {
    data class Whitespace(val content: String) : ParsedElement()
    data class Comment(val content: String) : ParsedElement()
    data class Identifier(val name: String) : ParsedElement()
    data class Property(val name: String, val value: PropertyValue) : ParsedElement()
    data class Element(val name: String, val content: List<ParsedElement>) : ParsedElement()
}

val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("[ \t\r\n]+")
val integerLiteral: Token = regexToken("\\d+")
val floatLiteral = regexToken("\\d+\\.\\d+")
val lineComment: Token = regexToken("//.*")
val blockComment: Token = regexToken("/\\*[\\s\\S]*?\\*/")

object MySmlGrammar : Grammar<List<ParsedElement>>() {
    val whitespaceParser: Parser<ParsedElement> = whitespace.map { ParsedElement.Whitespace(it.text) }
    val commentParser: Parser<ParsedElement> = (lineComment or blockComment).map { ParsedElement.Comment(it.text) }
    val ignoredParser: Parser<List<ParsedElement>> = zeroOrMore(whitespaceParser or commentParser)
    val stringParser: Parser<PropertyValue> = stringLiteral.map { PropertyValue.StringValue(it.text.removeSurrounding("\"")) }
    val integerParser: Parser<PropertyValue> = integerLiteral.map { PropertyValue.IntValue(it.text.toInt()) }
    val floatParser: Parser<PropertyValue> = floatLiteral.map { PropertyValue.FloatValue(it.text.toFloat()) }
    val propertyValue: Parser<PropertyValue> = floatParser or integerParser or stringParser
    val identifierParser: Parser<ParsedElement.Identifier> = identifier.map { ParsedElement.Identifier(it.text) }
    val property: Parser<List<ParsedElement>> = (
            ignoredParser and
                    identifierParser and
                    ignoredParser and
                    colon and
                    ignoredParser and
                    propertyValue
            ).map { (pre, id, mid1, _, mid2, value) ->
            pre + ParsedElement.Property(id.name, value) + mid1 + mid2
        }

    val elementContent: Parser<List<ParsedElement>> = zeroOrMore(
        property or
                parser { element } or
                whitespaceParser.map { listOf(it) } or
                commentParser.map { listOf(it) }
    ).map { it.flatten() }

    val element: Parser<List<ParsedElement>> = (
            ignoredParser and
                    identifierParser and
                    ignoredParser and
                    lBrace and
                    elementContent and
                    ignoredParser and
                    rBrace
            ).map { (pre, id, mid1, _, content, mid2, _) ->
            pre + ParsedElement.Element(id.name, content) + mid2
        }

    override val tokens: List<Token> = listOf(
        identifier, lBrace, rBrace, colon, stringLiteral, floatLiteral, integerLiteral,
        whitespace, lineComment, blockComment
    )

    override val rootParser: Parser<List<ParsedElement>> = (
            oneOrMore(
                element or
                        whitespaceParser.map { listOf(it) } or
                        commentParser.map { listOf(it) }
            ).map { it.flatten() } and
                    ignoredParser
            ).map { (elements, trailing) -> elements + trailing }
}


object MySmlSyntaxHighlighter {
    // Define colors for different syntax elements
    private val commentColor = Color.Gray
    private val stringColor = Color(0xFFBE896F)
    private val keywordColor = Color(0xFF61BEA6)
    private val propertyColor = Color(0xFFA0D4FC)
    private val numberColor = Color(0xFFA0B592)
    private const val TAB_SIZE = 4

    fun parseSml(input: String): List<ParsedElement> {
        return try {
            MySmlGrammar.parseToEnd(input)
        } catch (e: Exception) {
            // If parsing fails, return the entire input as a single text element
            listOf(ParsedElement.Whitespace(input))
        }
    }

    fun highlightSyntax(parsedElements: List<ParsedElement>): AnnotatedString {
        return buildAnnotatedString {
            for (element in parsedElements) {
                when (element) {
                    is ParsedElement.Whitespace -> {
                        // Ersetze Tabs durch 4 Leerzeichen
                        append(element.content.replace("\t", " ".repeat(TAB_SIZE)))
                    }
                    is ParsedElement.Comment -> withStyle(style = SpanStyle(color = commentColor)) {
                        append(element.content)
                    }
                    is ParsedElement.Identifier -> withStyle(style = SpanStyle(color = keywordColor)) {
                        append(element.name)
                    }
                    is ParsedElement.Property -> {
                        withStyle(style = SpanStyle(color = propertyColor)) {
                            append(element.name)
                        }
                        append(":")
                        when (val value = element.value) {
                            is PropertyValue.StringValue -> withStyle(style = SpanStyle(color = stringColor)) {
                                append('"' + value.value + '"')
                            }
                            is PropertyValue.IntValue -> withStyle(style = SpanStyle(color = numberColor)) {
                                append(value.value.toString())
                            }
                            is PropertyValue.FloatValue -> withStyle(style = SpanStyle(color = numberColor)) {
                                append(value.value.toString())
                            }
                        }
                    }
                    is ParsedElement.Element -> {
                        withStyle(style = SpanStyle(color = keywordColor)) {
                            append(element.name)
                        }
                        append("{")
                        append(highlightSyntax(element.content))
                        append("}")
                    }
                }
            }
        }
    }

}

// Usage in a BasicTextField:
// BasicTextField(
//     value = textState,
//     onValueChange = { textState = it },
//     visualTransformation = SmlSyntaxHighlighter.visualTransformation
// )

val sml = "Page{/*comment*/}"

fun parsePage() {
    val result = MySmlGrammar.parseToEnd(sml)
    println(result)
}

/*
"Page{Button{label:\"click\"}}"
[Element(name=Page, content=[Element(name=Button, content=[Property(name=label, value=StringValue(value=click))])])]

"\tPage{Button{label:\"click\"}}"
[Whitespace(content=	), Element(name=Page, content=[Element(name=Button, content=[Property(name=label, value=StringValue(value=click))])])]

"\tPage{Button{label:\"click\"} }"
[Whitespace(content=	), Element(name=Page, content=[Element(name=Button, content=[Property(name=label, value=StringValue(value=click))]), Whitespace(content= )])]

"Page{/*comment*/}}"
[Element(name=Page, content=[Comment(content=/*comment*/)])]
*/

class SmlOffsetMapping(private val originalText: String, private val parsedElements: List<ParsedElement>) : OffsetMapping {
    private val TAB_SIZE = 4

    private fun expandTabs(text: String): String {
        return text.replace("\t", " ".repeat(TAB_SIZE))
    }

    override fun originalToTransformed(offset: Int): Int {
        var currentOriginalOffset = 0
        var currentTransformedOffset = 0

        for (element in parsedElements) {
            when (element) {
                is ParsedElement.Whitespace -> {
                    val expandedContent = expandTabs(element.content)
                    val originalLength = element.content.length
                    val expandedLength = expandedContent.length
                    if (offset <= currentOriginalOffset + originalLength) {
                        val relativeOffset = offset - currentOriginalOffset
                        return currentTransformedOffset + expandTabs(element.content.substring(0, relativeOffset)).length
                    }
                    currentOriginalOffset += originalLength
                    currentTransformedOffset += expandedLength
                }
                is ParsedElement.Comment -> {
                    val length = element.content.length
                    if (offset <= currentOriginalOffset + length) {
                        return currentTransformedOffset + (offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += length
                    currentTransformedOffset += length
                }
                is ParsedElement.Identifier -> {
                    if (offset <= currentOriginalOffset + element.name.length) {
                        return currentTransformedOffset + (offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += element.name.length
                    currentTransformedOffset += element.name.length
                }
                is ParsedElement.Property -> {
                    val nameLength = element.name.length
                    if (offset <= currentOriginalOffset + nameLength) {
                        return currentTransformedOffset + (offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += nameLength
                    currentTransformedOffset += nameLength

                    // Add 1 for the colon
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1

                    val valueLength = when (val value = element.value) {
                        is PropertyValue.StringValue -> value.value.length + 2 // +2 for quotes
                        is PropertyValue.IntValue -> value.value.toString().length
                        is PropertyValue.FloatValue -> value.value.toString().length
                    }
                    if (offset <= currentOriginalOffset + valueLength) {
                        return currentTransformedOffset + (offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += valueLength
                    currentTransformedOffset += valueLength
                }
                is ParsedElement.Element -> {
                    if (offset <= currentOriginalOffset + element.name.length) {
                        return currentTransformedOffset + (offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += element.name.length
                    currentTransformedOffset += element.name.length

                    // Add 1 for the opening brace
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1

                    // Recursively handle the content
                    val contentMapping = SmlOffsetMapping(originalText.substring(currentOriginalOffset), element.content)
                    if (offset <= currentOriginalOffset + contentMapping.originalToTransformed(element.content.sumOf { it.totalLength() })) {
                        return currentTransformedOffset + contentMapping.originalToTransformed(offset - currentOriginalOffset)
                    }
                    currentOriginalOffset += contentMapping.originalToTransformed(element.content.sumOf { it.totalLength() })
                    currentTransformedOffset += contentMapping.originalToTransformed(element.content.sumOf { it.totalLength() })

                    // Add 1 for the closing brace
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1
                }
            }
        }

        return currentTransformedOffset
    }

    override fun transformedToOriginal(offset: Int): Int {
        var currentOriginalOffset = 0
        var currentTransformedOffset = 0

        for (element in parsedElements) {
            when (element) {
                is ParsedElement.Whitespace -> {
                    val expandedContent = expandTabs(element.content)
                    val originalLength = element.content.length
                    val expandedLength = expandedContent.length
                    if (offset <= currentTransformedOffset + expandedLength) {
                        val relativeOffset = offset - currentTransformedOffset
                        return currentOriginalOffset + element.content.indexOfFirst {
                            expandTabs(element.content.substring(0, (it + 1).code)).length > relativeOffset
                        }.coerceAtLeast(0)
                    }
                    currentOriginalOffset += originalLength
                    currentTransformedOffset += expandedLength
                }
                is ParsedElement.Comment -> {
                    val length = element.content.length
                    if (offset <= currentTransformedOffset + length) {
                        return currentOriginalOffset + (offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += length
                    currentTransformedOffset += length
                }
                is ParsedElement.Identifier -> {
                    if (offset <= currentTransformedOffset + element.name.length) {
                        return currentOriginalOffset + (offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += element.name.length
                    currentTransformedOffset += element.name.length
                }
                is ParsedElement.Property -> {
                    val nameLength = element.name.length
                    if (offset <= currentTransformedOffset + nameLength) {
                        return currentOriginalOffset + (offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += nameLength
                    currentTransformedOffset += nameLength

                    // Add 1 for the colon
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1

                    val valueLength = when (val value = element.value) {
                        is PropertyValue.StringValue -> value.value.length + 2 // +2 for quotes
                        is PropertyValue.IntValue -> value.value.toString().length
                        is PropertyValue.FloatValue -> value.value.toString().length
                    }
                    if (offset <= currentTransformedOffset + valueLength) {
                        return currentOriginalOffset + (offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += valueLength
                    currentTransformedOffset += valueLength
                }
                is ParsedElement.Element -> {
                    if (offset <= currentTransformedOffset + element.name.length) {
                        return currentOriginalOffset + (offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += element.name.length
                    currentTransformedOffset += element.name.length

                    // Add 1 for the opening brace
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1

                    // Recursively handle the content
                    val contentMapping = SmlOffsetMapping(originalText.substring(currentOriginalOffset), element.content)
                    val contentLength = contentMapping.originalToTransformed(element.content.sumOf { it.totalLength() })
                    if (offset <= currentTransformedOffset + contentLength) {
                        return currentOriginalOffset + contentMapping.transformedToOriginal(offset - currentTransformedOffset)
                    }
                    currentOriginalOffset += contentLength
                    currentTransformedOffset += contentLength

                    // Add 1 for the closing brace
                    currentOriginalOffset += 1
                    currentTransformedOffset += 1
                }
            }
        }

        return currentOriginalOffset
    }
}

// Helper extension function to calculate the total length of a ParsedElement
fun ParsedElement.totalLength(): Int = when (this) {
    is ParsedElement.Whitespace -> content.length
    is ParsedElement.Comment -> content.length
    is ParsedElement.Identifier -> name.length
    is ParsedElement.Property -> name.length + 1 + when (value) {
        is PropertyValue.StringValue -> value.value.length + 2
        is PropertyValue.IntValue -> value.value.toString().length
        is PropertyValue.FloatValue -> value.value.toString().length
    }
    is ParsedElement.Element -> name.length + 2 + content.sumOf { it.totalLength() }
}


@Composable
fun MySyntaxTextField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    extension: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentState = uiStates.current
    val extendedColors = ExtendedTheme.colors
    var pos by remember { mutableStateOf(Offset(0f, 0f)) }

    var isFocused by remember { mutableStateOf(false) }
    var isHovered = currentState.hasCollided.value

    val backgroundColor = MaterialTheme.colors.surface
    val cursorColor = MaterialTheme.colors.onSurface
    val focusedBorderColor = MaterialTheme.colors.primary
    val unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)

    val borderColor = when {
        isHovered -> MaterialTheme.colors.secondary
        isFocused -> focusedBorderColor
        else -> unfocusedBorderColor
    }

    val highlightTransformation = remember {
        VisualTransformation { text ->
            val parsedElements = MySmlSyntaxHighlighter.parseSml(text.text)
            val highlightedText = MySmlSyntaxHighlighter.highlightSyntax(parsedElements)
            val offsetMapping = SmlOffsetMapping(text.text, parsedElements)
            TransformedText(highlightedText, offsetMapping)
        }
    }

    CustomSelectionColors {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .border(BorderStroke(1.dp, borderColor))
                .padding(start = 6.dp, top = 4.dp)
                .onGloballyPositioned {
                    pos = it.localToWindow(Offset(0f, 0f))
                    currentState.targetLocalPosition = it.localToWindow(Offset(0f, 0f))
                    currentState.targetSize = it.size.toSize()
                }
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(6.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        val offsetMapping = (highlightTransformation.filter(AnnotatedString(newValue.text)) as TransformedText).offsetMapping
                        val newStart = offsetMapping.originalToTransformed(newValue.selection.start)
                        val newEnd = offsetMapping.originalToTransformed(newValue.selection.end)
                        val newSelection = TextRange(newStart, newEnd)
                        onValueChange(newValue.copy(selection = newSelection))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .heightIn(min = 640.dp)
                        .background(Color.Transparent)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(fontSize = 14.sp, color = extendedColors.defaultTextColor, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(cursorColor),
                    visualTransformation = when(extension) {
                        "sml" -> highlightTransformation
                        else -> VisualTransformation.None
                    },
                    maxLines = Int.MAX_VALUE
                )
            }

            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(12.dp)
                    .padding(vertical = 4.dp),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }
}