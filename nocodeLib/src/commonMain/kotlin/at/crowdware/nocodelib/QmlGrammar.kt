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


// Definiere die Tokens
val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("\\s+")

object QmlGrammar : Grammar<List<Any>>() {
    val whitespaceParser = zeroOrMore(whitespace)

        val property by (whitespaceParser and identifier and whitespaceParser and colon and whitespaceParser and stringLiteral).map { (_, id, _, _, _, value) ->
        id.text to value.text.removeSurrounding("\"")
    }
    val elementContent: Parser<List<Any>> = oneOrMore(property or parser { element })
    val element: Parser<Any> by whitespaceParser and identifier and whitespaceParser and lBrace and elementContent and whitespaceParser and rBrace

    override val tokens: List<Token> = listOf(identifier, lBrace, rBrace, colon, stringLiteral, whitespace)
    override val rootParser: Parser<List<Any>> = oneOrMore(element)
}

// Beispiel QML Code


fun deserializeQml(parsedResult: List<Any>): Page {
    val page = Page(color = "", backgroundColor = "", padding = Padding(0, 0, 0, 0), elements = mutableListOf())
    
    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = (tuple.t5 as? List<*>)?.filterIsInstance<Pair<String, String>>()?.toMap()

                println("element1: $elementName")
                when (elementName) {
                    "Page" -> {
                        page.color = properties?.get("color") ?: ""
                        page.backgroundColor = properties?.get("backgroundColor") ?: ""
                        page.padding = parsePadding(properties?.get("padding"))
                        parseNestedElements(tuple.t5 as? List<*>, page.elements as MutableList<UIElement>)
                    }
                    "Column" -> {
                      
                        parseNestedElements(tuple.t5 as? List<*>, page.elements as MutableList<UIElement>)
                    }
                    "Text" -> {
                        val text = properties?.get("text") ?: ""
                        val color = properties?.get("color") ?: ""
                        val te = TextElement(text, color, 14.sp, FontWeight.Normal, TextAlign.Left)
                        page.elements.add(te)
                    }
                    // Add more cases for other element types
                }
            }
        }
    }
    
    return page
}

fun parsePadding(paddingString: String?): Padding {
    val parts = paddingString?.split(",")?.map { it.trim().toIntOrNull() ?: 0 } ?: listOf(0, 0, 0, 0)
    return Padding(parts.getOrElse(0) { 0 }, parts.getOrElse(1) { 0 }, parts.getOrElse(2) { 0 }, parts.getOrElse(3) { 0 })
}

fun parseNestedElements(nestedElements: List<*>?, elements: MutableList<UIElement>) {
    nestedElements?.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = (element.t5 as? List<*>)?.filterIsInstance<Pair<String, String>>()?.toMap()

                println("element2: $elementName")
                when (elementName) {
                    "Text" -> {
                        val text = properties?.get("text") ?: ""
                        val color = properties?.get("color") ?: ""
                        elements.add(TextElement(text, color, 14.sp, FontWeight.Normal, TextAlign.Left))
                    }
                    // Add more cases for other element types
                }
            }
        }
    }
}


val qmlCode = """
Page { 
	backgroundColor: "#0000ßß" 
	color: "#ffffFF"
    padding: "8"
			
	Column {
		padding: "8"
	
		Text { content: "#Zeile 1" 
            color:"#ffffff" }
				  
        Text { content: "#Zeile 2" color:"#ffffff" }
		Button { link: "page:about" label: "About" }
		Button { label: "test" link: "page:home" }
	}
}
""".trimIndent()

// Parsen des QML Codes
fun testQML() {
    val result = QmlGrammar.parseToEnd(qmlCode)
    println(result)
    val page = deserializeQml(result)
    println(page)
}
