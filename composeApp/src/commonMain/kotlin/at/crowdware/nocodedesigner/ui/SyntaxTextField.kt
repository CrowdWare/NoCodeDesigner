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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import at.crowdware.nocodedesigner.theme.ExtendedColors
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.utils.uiStates


@Composable
fun SyntaxTextField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    extension: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentState = uiStates.current
    val extendedColors = ExtendedTheme.colors
    var pos by remember { mutableStateOf(Offset(0f,0f)) }

    var isFocused by remember { mutableStateOf(false) }
    var isHovered = currentState.hasCollided.value

    // Default colors
    val backgroundColor = MaterialTheme.colors.surface
    val cursorColor = MaterialTheme.colors.onSurface
    val focusedBorderColor = MaterialTheme.colors.primary
    val unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)

    // Border color changes based on hover, focus, or default state
    val borderColor = when {
        isHovered -> MaterialTheme.colors.secondary // Use hover border color when hovered
        isFocused -> focusedBorderColor
        else -> unfocusedBorderColor
    }

    CustomSelectionColors {
        Row(
            modifier = modifier
                .fillMaxSize() // Ensures the whole available area is filled
                .background(backgroundColor)
                .border(BorderStroke(1.dp, borderColor)/*, RoundedCornerShape(8.dp)*/)
                .padding(start = 6.dp, top = 4.dp)
                .onGloballyPositioned{
                    pos = it.localToWindow(Offset(0f,0f))
                    currentState.targetLocalPosition = it.localToWindow(Offset(0f,0f))
                    currentState.targetSize = it.size.toSize()
                }
        ) {
            // Scrollable Box for the text field
            Box(
                modifier = Modifier
                    .weight(1f) // Take up remaining space for the text field
                    .fillMaxHeight() // Fill the height of the parent
                    .verticalScroll(scrollState)
                    .padding(6.dp)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth() // Fill the available width
                        .fillMaxHeight() // Fill the available height of the box
                        .heightIn(min = 640.dp)
                        .background(Color.Transparent), // Make the background transparent for the text field
                    textStyle = TextStyle(fontSize = 14.sp, color = extendedColors.attributeNameColor, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(cursorColor),
                          visualTransformation = when(extension) {
                        "qml" -> QmlSyntaxHighlighter(extendedColors)
                        else -> VisualTransformation.None
                    },
                    maxLines = Int.MAX_VALUE
                )
            }

            // Vertical Scrollbar positioned next to the text field
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

class QmlSyntaxHighlighter(val colors: ExtendedColors) : VisualTransformation {
    private val tabWidth = 4 // Anzahl der Leerzeichen für einen Tab
/*
    override fun filter(text: AnnotatedString): TransformedText {
        // Ersetzen Sie zuerst alle Tabs durch Leerzeichen
        val textWithExpandedTabs = expandTabs(text.text)

        val builder = AnnotatedString.Builder(textWithExpandedTabs)

        // Übertragen Sie die ursprünglichen Stile auf den neuen Text
        text.spanStyles.forEach { span ->
            val newStart = mapIndexForward(text.text, span.start)
            val newEnd = mapIndexForward(text.text, span.end)
            builder.addStyle(span.item, newStart, newEnd)
        }

        // Highlight QML elements
        val elementRegex = Regex("(\\w+)\\s*\\{")
        elementRegex.findAll(textWithExpandedTabs).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.syntaxColor), match.range.first, match.range.last + 1)
        }

        // Highlight properties
        val propertyRegex = Regex("(\\w+)\\s*:")
        propertyRegex.findAll(textWithExpandedTabs).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.attributeNameColor), match.range.first, match.range.last)
        }

        // Highlight string values and embedded Markdown
        val stringRegex = Regex("(text:\\s*)?\"([^\"]+)\"")
        stringRegex.findAll(textWithExpandedTabs).forEach { match ->
            val isTextProperty = match.groups[1] != null
            val content = match.groups[2]?.value ?: ""
            val start = match.range.first
            val end = match.range.last + 1

            // Highlight the entire string in green
            builder.addStyle(SpanStyle(color = colors.attributeValueColor), start, end)

            if (isTextProperty) {
                val markdownHighlighter = MarkdownSyntaxHighlighter(colors)
                val highlightedMarkdown = markdownHighlighter.filter(AnnotatedString(content))
                highlightedMarkdown.text.spanStyles.forEach { spanStyle ->
                    val textPropertyLength = match.groups[1]?.value?.length ?: 0
                    builder.addStyle(spanStyle.item, start + textPropertyLength + 1 + spanStyle.start, start + textPropertyLength + 1 + spanStyle.end)
                }
            }
        }

        return TransformedText(builder.toAnnotatedString(), object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = mapIndexForward(text.text, offset)
            override fun transformedToOriginal(offset: Int): Int = mapIndexBackward(text.text, offset)
        })
    }
*/

    override fun filter(text: AnnotatedString): TransformedText {
        // Replace all tabs with spaces first
        val textWithExpandedTabs = expandTabs(text.text)

        val builder = AnnotatedString.Builder(textWithExpandedTabs)

        // Transfer original styles to the new text
        text.spanStyles.forEach { span ->
            val newStart = mapIndexForward(text.text, span.start)
            val newEnd = mapIndexForward(text.text, span.end)
            builder.addStyle(span.item, newStart, newEnd)
        }

        // Highlight string values first, including those with colons
        val stringRegex = Regex("\"[^\"]*\"")
        stringRegex.findAll(textWithExpandedTabs).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.attributeValueColor), match.range.first, match.range.last + 1)
        }

        // Highlight QML elements
        val elementRegex = Regex("(\\w+)\\s*\\{")
        elementRegex.findAll(textWithExpandedTabs).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.syntaxColor), match.range.first, match.range.last + 1)
        }

        // Highlight properties (excluding the colon)
        val propertyRegex = Regex("(\\w+)(?=\\s*:)")
        propertyRegex.findAll(textWithExpandedTabs).forEach { match ->
            // Check if this property name is not within a string
            if (!isWithinString(textWithExpandedTabs, match.range.first)) {
                builder.addStyle(SpanStyle(color = colors.attributeNameColor), match.range.first, match.range.last + 1)
            }
        }

        // Highlight colons and closing brackets
        val colonAndBracketRegex = Regex(":|[}]")
        colonAndBracketRegex.findAll(textWithExpandedTabs).forEach { match ->
            // Check if this colon or bracket is not within a string
            if (!isWithinString(textWithExpandedTabs, match.range.first)) {
                builder.addStyle(SpanStyle(color = colors.syntaxColor), match.range.first, match.range.last + 1)
            }
        }

        return TransformedText(builder.toAnnotatedString(), object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = mapIndexForward(text.text, offset)
            override fun transformedToOriginal(offset: Int): Int = mapIndexBackward(text.text, offset)
        })
    }

    // Helper function to check if a given index is within a string
    private fun isWithinString(text: String, index: Int): Boolean {
        var inString = false
        for (i in 0 until index) {
            if (text[i] == '"') {
                inString = !inString
            }
        }
        return inString
    }

    private fun expandTabs(text: String): String {
        return text.replace("\t", " ".repeat(tabWidth))
    }

    private fun mapIndexForward(original: String, index: Int): Int {
        var newIndex = 0
        for (i in 0 until index) {
            if (original[i] == '\t') {
                newIndex += tabWidth
            } else {
                newIndex++
            }
        }
        return newIndex
    }

    private fun mapIndexBackward(original: String, transformedIndex: Int): Int {
        var originalIndex = 0
        var currentTransformedIndex = 0
        while (currentTransformedIndex < transformedIndex && originalIndex < original.length) {
            if (original[originalIndex] == '\t') {
                currentTransformedIndex += tabWidth
            } else {
                currentTransformedIndex++
            }
            originalIndex++
        }
        return originalIndex
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

class MarkdownSyntaxHighlighter(val colors: ExtendedColors) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(applyMarkdownHighlighting(text), OffsetMapping.Identity)
    }

    private fun applyMarkdownHighlighting(text: AnnotatedString): AnnotatedString {
        val builder = AnnotatedString.Builder(text)

        // Apply all the highlight rules
        highlightHeaders(builder, text)
        highlightBold(builder, text)
        highlightItalic(builder, text)
        highlightLinks(builder, text)
        highlightCodeBlocks(builder, text)
        highlightListItems(builder, text)
        highlightInlineHtml(builder, text)
        return builder.toAnnotatedString()
    }

    // Header highlighting: "# Title" and "## Subtitle"
    private fun highlightHeaders(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val headerRegex = Regex("(^#+)\\s(.+)", RegexOption.MULTILINE)
        headerRegex.findAll(text.text).forEach { match ->
            // Berechnung des Bereichs für headerMarks
            val headerMarks = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val headerMarksStart = matchStart + match.value.indexOf(headerMarks)
            val headerMarksEnd = headerMarksStart + headerMarks.length
            val headerMarksRange = headerMarksStart until headerMarksEnd

            // Berechnung des Bereichs für headerText
            val headerText = match.groups[2]?.value ?: ""
            val headerTextStart = matchStart + match.value.indexOf(headerText)
            val headerTextEnd = headerTextStart + headerText.length
            val headerTextRange = headerTextStart until headerTextEnd

            // "#" in syntax color
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = headerMarksRange.first,
                end = headerMarksRange.last + 1
            )
            // Text in magenta
            builder.addStyle(
                style = SpanStyle(color = colors.mdHeader),
                start = headerTextRange.first,
                end = headerTextRange.last + 1
            )
        }
    }

    // Bold highlighting: "**bold**"
    private fun highlightBold(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        boldRegex.findAll(text.text).forEach { match ->
            val boldText = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val boldTextStart = matchStart + match.value.indexOf(boldText)
            val boldTextEnd = boldTextStart + boldText.length
            val boldTextRange = boldTextStart until boldTextEnd

            // "**" in syntax color (white)
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.first + 2
            )
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.last - 1,
                end = match.range.last + 1
            )

            // Bold text in default text color
            builder.addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.defaultTextColor),
                start = boldTextRange.first,
                end = boldTextRange.last + 1
            )
        }
    }

    // Italic highlighting: "*italic*"
    private fun highlightItalic(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val italicRegex = Regex("\\*(.*?)\\*")
        italicRegex.findAll(text.text).forEach { match ->
            val italicText = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val italicTextStart = matchStart + match.value.indexOf(italicText)
            val italicTextEnd = italicTextStart + italicText.length
            val italicTextRange = italicTextStart until italicTextEnd

            // "*" in syntax color (white)
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.first + 1
            )
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.last,
                end = match.range.last + 1
            )

            // Italic text in default text color
            builder.addStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.defaultTextColor),
                start = italicTextRange.first,
                end = italicTextRange.last + 1
            )
        }
    }

    // Link highlighting: "[link](url)"
    private fun highlightLinks(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val linkRegex = Regex("\\[(.+?)\\]\\((.+?)\\)")
        linkRegex.findAll(text.text).forEach { match ->
            val linkText = match.groups[1]?.value ?: ""
            var matchStart = match.range.first
            val linkTextStart = matchStart + match.value.indexOf(linkText)
            val linkTextEnd = linkTextStart + linkText.length
            val linkTextRange = linkTextStart until linkTextEnd

            val urlText = match.groups[2]?.value ?: ""
            val urlTextStart = matchStart + match.value.indexOf(urlText)
            val urlTextEnd = urlTextStart + urlText.length
            val urlRange = urlTextStart until urlTextEnd

            // "[" and "]" in blue (covering the entire link text with brackets)
            builder.addStyle(
                style = SpanStyle(color = colors.linkColor),
                start = match.range.first, // The '['
                end = linkTextRange.last + 2 // The ']'
            )

            // "(" and ")" in green
            builder.addStyle(
                style = SpanStyle(color = colors.attributeValueColor),
                start = linkTextRange.last + 2, // The '('
                end = urlRange.last + 2 // The ')' (include the final parenthesis)
            )

            // URL in blue
            builder.addStyle(
                style = SpanStyle(color = colors.linkColor),
                start = urlRange.first,
                end = urlRange.last + 1
            )
        }
    }

    private fun highlightCodeBlocks(builder: AnnotatedString.Builder, text: AnnotatedString) {
        //val codeBlockRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockRegex = createCodeBlockRegex()
        codeBlockRegex.findAll(text.text).forEach { match ->
            // Render the code block in a different color (e.g., light gray for background, dark gray for text)
            builder.addStyle(
                style = SpanStyle(/*background = Color.LightGray,*/ color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }

    // List item highlighting: "- Item"
    private fun highlightListItems(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val listItemRegex = Regex("^(-)\\s", RegexOption.MULTILINE)
        listItemRegex.findAll(text.text).forEach { match ->
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.last
            )
        }
    }

    private fun highlightInlineHtml(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val htmlTagRegex = Regex("<([a-zA-Z]+)(\\s+[a-zA-Z]+=\"[^\"]*\")*\\s*/?>")
        htmlTagRegex.findAll(text.text).forEach { match ->

            val tagName = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val tagNameStart = matchStart + match.value.indexOf(tagName)
            val tagNameEnd = tagNameStart + tagName.length
            val tagNameRange = tagNameStart until tagNameEnd
            val attributesRegex = Regex("([a-zA-Z]+)=(\"[^\"]*\")")
            val attributesMatch = attributesRegex.findAll(match.value)

            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor), // Purple for HTML tag
                start = match.range.first,
                end = tagNameRange.last + 1
            )

            // HTML attributes in attributeNameColor and values in attributeValueColor
            attributesMatch.forEach { attrMatch ->
                // Berechnung des Bereichs für attrName
                val attrName = attrMatch.groups[1]?.value ?: ""
                val attrMatchStart = attrMatch.range.first
                val attrNameStart = attrMatchStart + attrMatch.value.indexOf(attrName)
                val attrNameEnd = attrNameStart + attrName.length
                val attrNameRange = attrNameStart until attrNameEnd

                // Berechnung des Bereichs für attrValue
                val attrValue = attrMatch.groups[2]?.value ?: ""
                val attrValueStart = attrMatchStart + attrMatch.value.indexOf(attrValue)
                val attrValueEnd = attrValueStart + attrValue.length
                val attrValueRange = attrValueStart until attrValueEnd

                // Attribute name in attributeNameColor (e.g., src, id)
                builder.addStyle(
                    style = SpanStyle(color = colors.attributeNameColor),
                    start = match.range.first + attrNameRange.first,
                    end = match.range.first + attrNameRange.last + 1
                )

                // `=` and value in attributeValueColor (e.g., ="link")
                builder.addStyle(
                    style = SpanStyle(color = colors.attributeValueColor),
                    start = match.range.first + attrValueRange.first - 1, // Include `=`
                    end = match.range.first + attrValueRange.last + 1 // Include `"`
                )
            }

            // Close tag `/>` in purple
            val closeTagIndex = match.range.last - 1
            if (text.text[closeTagIndex] == '/') {
                builder.addStyle(
                    style = SpanStyle(color = colors.syntaxColor),
                    start = closeTagIndex,
                    end = match.range.last + 1 // Cover `/>`
                )
            } else {
                builder.addStyle(
                    style = SpanStyle(color = colors.syntaxColor),
                    start = match.range.last,
                    end = match.range.last + 1
                )
            }
        }
    }
}

