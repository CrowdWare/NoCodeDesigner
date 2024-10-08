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


val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("\\s+")
val integerLiteral: Token = regexToken("\\d+")

object QmlGrammar : Grammar<List<Any>>() {
    val whitespaceParser = zeroOrMore(whitespace)
    val propertyValue = stringLiteral.map { it.text.removeSurrounding("\"") } or integerLiteral.map { it.text }
    val property by (whitespaceParser and identifier and whitespaceParser and colon and whitespaceParser and propertyValue).map { (_, id, _, _, _, value) ->
        id.text to value
    }
    val elementContent: Parser<List<Any>> = oneOrMore(property or parser { element })
    val element: Parser<Any> by whitespaceParser and identifier and whitespaceParser and lBrace and elementContent and whitespaceParser and rBrace

    override val tokens: List<Token> = listOf(identifier, lBrace, rBrace, colon, stringLiteral, integerLiteral, whitespace)
    override val rootParser: Parser<List<Any>> = oneOrMore(element)
}

fun isQmlRootElement(qmlString: String, root: String): Boolean {
    val regex = Regex("""^\s*$root\s*\{""")
    return regex.containsMatchIn(qmlString)
}

fun deserializeApp(parsedResult: List<Any>): App {
    val app = App(type = "", items = mutableListOf())
    // TODO: Implement deserialization logic
    return app
}

fun deserializePage(parsedResult: List<Any>): Page {
    val page = Page(color = "", backgroundColor = "", padding = Padding(0, 0, 0, 0), elements = mutableListOf())
    
    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = (tuple.t5 as? List<*>)?.filterIsInstance<Pair<String, String>>()?.toMap()

                when (elementName) {
                    "Page" -> {

                        page.color = properties?.get("color") ?: ""
                        page.backgroundColor = properties?.get("backgroundColor") ?: ""
                        page.padding = parsePadding(properties?.get("padding").toString())
                        parseNestedElements(tuple.t5 as? List<*>, page.elements as MutableList<UIElement>)
                    }
                }
            }
        }
    }
    
    return page
}

fun parseNestedElements(nestedElements: List<*>?, elements: MutableList<UIElement>) {
    nestedElements?.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = (element.t5 as? List<*>)?.filterIsInstance<Pair<String, String>>()?.toMap()

                when (elementName) {
                    "Text" -> {
                        elements.add(TextElement(
                            text = properties?.get("text") ?: "def",
                            color = properties?.get("color") ?: "",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Left))
                    }
                    "Column" -> {
                        val col = ColumnElement(padding = parsePadding(properties?.get("padding") ?: "0"))
                        parseNestedElements(element.t5 as? List<*>, col.uiElements as MutableList<UIElement>)
                        elements.add(col)
                    }
                    "Row" -> {
                        val row = RowElement(padding = parsePadding(properties?.get("padding") ?: "0"))
                        parseNestedElements(element.t5 as? List<*>, row.uiElements as MutableList<UIElement>)
                        elements.add(row)
                    }
                    "Markdown" -> {
                        val md = (properties?.get("text") ?: "").split("\n").joinToString("\n") { it.trim() }
                        val ele = MarkdownElement(text = md, color = properties?.get("color") ?: "#FFFFFF")
                            elements.add(ele)
                        }
                    "Button" -> {
                        val btn = ButtonElement(label = properties?.get("label") ?: "", link = properties?.get("link") ?: "")
                        elements.add(btn)
                    }
                    "Sound" -> {
                        val snd = SoundElement(src = properties?.get("src") ?: "")
                        elements.add(snd)
                    }
                    "Image" -> {
                        val img = ImageElement(src = properties?.get("src") ?: "", scale = properties?.get("scale") ?: "1", link = properties?.get("link") ?: "")
                        elements.add(img)
                    }
                    "Spacer" -> {
                        val sp = SpacerElement(height = properties?.get("height")?.toInt() ?: 0)
                        elements.add(sp)
                    }
                    "Video" -> {
                        val vid = VideoElement(
                            src = properties?.get("src") ?: "",
                            height = properties?.get("height")?.toInt() ?: 100,
                        )
                        elements.add(vid)
                    }
                    "Youtube" -> {
                        val yt = YoutubeElement(
                            id = properties?.get("id") ?: "",
                            height = properties?.get("height")?.toInt() ?: 100,
                        )
                        elements.add(yt)
                    }
                }
            }
        }
    }
}

fun parsePage(qml: String): Page {
    val result = QmlGrammar.parseToEnd(qml)
    return deserializePage(result)
}

fun parseApp(qml: String): App {
    val result = QmlGrammar.parseToEnd(qml)
    return deserializeApp(result)
}
