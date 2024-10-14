package at.crowdware.nocodelib

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.Token
import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.utils.Tuple7


sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
}

val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("\\s+")
val integerLiteral: Token = regexToken("\\d+")
val floatLiteral = regexToken("\\d+\\.\\d+")

val lineComment: Token = regexToken("//.*")
val blockComment: Token = regexToken(Regex("/\\*[\\s\\S]*?\\*/", RegexOption.DOT_MATCHES_ALL))

object SmlGrammar : Grammar<List<Any>>() {
    val whitespaceParser = zeroOrMore(whitespace)

    val commentParser = lineComment or blockComment

    val ignoredParser = zeroOrMore(whitespace or commentParser)

    val stringParser = stringLiteral.map { PropertyValue.StringValue(it.text.removeSurrounding("\"")) }
    val integerParser = integerLiteral.map { PropertyValue.IntValue(it.text.toInt()) }
    val floatParser = floatLiteral.map { PropertyValue.FloatValue(it.text.toFloat()) }

    val propertyValue = floatParser or integerParser or stringParser

    val property by (ignoredParser and identifier and ignoredParser and colon and ignoredParser and propertyValue).map { (_, id, _, _, _, value) ->
        id.text to value
    }
    val elementContent: Parser<List<Any>> = zeroOrMore(property or parser { element })
    val element: Parser<Any> by ignoredParser and identifier and ignoredParser and lBrace and elementContent and ignoredParser and rBrace

    override val tokens: List<Token> = listOf(
        identifier, lBrace, rBrace, colon, stringLiteral, floatLiteral, integerLiteral,
        whitespace, lineComment, blockComment
    )
    override val rootParser: Parser<List<Any>> = (oneOrMore(element) and ignoredParser).map { (elements, _) -> elements }
}

fun isSmlRootElement(smlString: String, root: String): Boolean {
    val regex = Regex("""^\s*$root\s*\{""")
    return regex.containsMatchIn(smlString)
}

fun deserializeApp(parsedResult: List<Any>): App {
    val app = App()
    // TODO: Implement deserialization logic
    return app
}

fun extractProperties(element: Any): Map<String, PropertyValue> {
    if (element is Tuple7<*, *, *, *, *, *, *>) {
        return (element.t5 as? List<*>)?.filterIsInstance<Pair<String, PropertyValue>>()?.toMap() ?: emptyMap()
    }
    return emptyMap()
}

fun extractChildElements(element: Any): List<Any> {
    if (element is Tuple7<*, *, *, *, *, *, *>) {
        return (element.t5 as? List<*>)?.filterIsInstance<Tuple7<*, *, *, *, *, *, *>>() ?: emptyList()
    }
    return emptyList()
}

fun deserializePage(parsedResult: List<Any>): Page {
    val page = Page(color = "", backgroundColor = "", padding = Padding(0, 0, 0, 0), elements = mutableListOf())

    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = extractProperties(tuple)

                when (elementName) {
                    "Page" -> {
                        page.color = (properties["color"] as? PropertyValue.StringValue)?.value ?: ""
                        page.backgroundColor = (properties["backgroundColor"] as? PropertyValue.StringValue)?.value ?: ""
                        page.padding = parsePadding((properties["padding"] as? PropertyValue.StringValue)?.value ?: "0")
                        parseNestedElements(extractChildElements(tuple), page.elements as MutableList<UIElement>)
                    }
                }
            }
        }
    }

    return page
}

val fontWeightMap = mapOf(
    "bold" to FontWeight.Bold,
    "black" to FontWeight.Black,
    "thin" to FontWeight.Thin,
    "extrabold" to FontWeight.ExtraBold,
    "extralight" to FontWeight.ExtraLight,
    "light" to FontWeight.Light,
    "medium" to FontWeight.Medium,
    "semibold" to FontWeight.SemiBold,
    "" to FontWeight.Normal
)

val textAlignMap = mapOf(
    "left" to TextAlign.Start,
    "center" to TextAlign.Center,
    "right" to TextAlign.End,
    "" to TextAlign.Unspecified
)

fun parseNestedElements(nestedElements: List<Any>, elements: MutableList<UIElement>) {
    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "Text" -> {
                        elements.add(
                            UIElement.TextElement(
                                text = (properties["text"] as? PropertyValue.StringValue)?.value ?: "",
                                color = hexToColor((properties["color"] as? PropertyValue.StringValue)?.value ?: ""),
                                fontSize = ((properties["fontSize"] as? PropertyValue.IntValue)?.value ?: 14).sp,
                                fontWeight = fontWeightMap[(properties["fontWeight"] as? PropertyValue.StringValue)?.value
                                    ?: ""] ?: FontWeight.Normal,
                                textAlign = textAlignMap[(properties["textAlign"] as? PropertyValue.StringValue)?.value
                                    ?: ""] ?: TextAlign.Unspecified
                            )
                        )
                    }
                    "Column" -> {
                        val col = UIElement.ColumnElement(
                            padding = parsePadding(
                                (properties["padding"] as? PropertyValue.StringValue)?.value ?: "0"
                            )
                        )
                        parseNestedElements(extractChildElements(element), col.uiElements as MutableList<UIElement>)
                        elements.add(col)
                    }
                    "Row" -> {
                        val row = UIElement.RowElement(
                            padding = parsePadding(
                                (properties["padding"] as? PropertyValue.StringValue)?.value ?: "0"
                            )
                        )
                        parseNestedElements(extractChildElements(element), row.uiElements as MutableList<UIElement>)
                        elements.add(row)
                    }
                    "Markdown" -> {
                        val md = ((properties["text"] as? PropertyValue.StringValue)?.value ?: "").split("\n").joinToString("\n") { it.trim() }
                        val ele = UIElement.MarkdownElement(
                            text = md,
                            color = (properties["color"] as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
                        )
                        elements.add(ele)
                    }
                    "Button" -> {
                        val btn = UIElement.ButtonElement(
                            label = (properties["label"] as? PropertyValue.StringValue)?.value ?: "",
                            link = (properties["link"] as? PropertyValue.StringValue)?.value ?: ""
                        )
                        elements.add(btn)
                    }
                    "Sound" -> {
                        val snd =
                            UIElement.SoundElement(src = (properties["src"] as? PropertyValue.StringValue)?.value ?: "")
                        elements.add(snd)
                    }
                    "Image" -> {
                        val img = UIElement.ImageElement(
                            src = (properties["src"] as? PropertyValue.StringValue)?.value ?: "",
                            scale = (properties["scale"] as? PropertyValue.StringValue)?.value ?: "1",
                            link = (properties["link"] as? PropertyValue.StringValue)?.value ?: ""
                        )
                        elements.add(img)
                    }
                    "Spacer" -> {
                        val sp = UIElement.SpacerElement(
                            amount = (properties["amount"] as? PropertyValue.IntValue)?.value ?: 0,
                            weight = (properties["weight"] as? PropertyValue.IntValue)?.value ?: 0
                        )
                        elements.add(sp)
                    }
                    "Video" -> {
                        val vid = UIElement.VideoElement(
                            src = (properties["src"] as? PropertyValue.StringValue)?.value ?: "",
                            //height = (properties["height"] as? PropertyValue.IntValue)?.value ?: 100
                        )
                        elements.add(vid)
                    }
                    "Youtube" -> {
                        val yt = UIElement.YoutubeElement(
                            id = (properties["id"] as? PropertyValue.StringValue)?.value ?: "",
                            //height = (properties["height"] as? PropertyValue.IntValue)?.value ?: 100
                        )
                        elements.add(yt)
                    }
                }
            }
        }
    }
}

fun parsePage(sml: String): Pair<Page?, String?> {
    try {
        val result = SmlGrammar.parseToEnd(sml)
        return Pair(deserializePage(result), null)
    } catch(e: Exception) {
        return Pair(null, e.message)
    }
}

fun parseApp(sml: String, ): Pair<App?, String?> {
    try {
        val result = SmlGrammar.parseToEnd(sml)
        return Pair(deserializeApp(result), null)
    } catch(e: Exception) {
        return Pair(null, e.message)
    }
}
