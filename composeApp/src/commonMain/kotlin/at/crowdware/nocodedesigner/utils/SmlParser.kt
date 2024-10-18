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
package at.crowdware.nocodedesigner.utils

import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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

fun deserializeApp(parsedResult: List<Any>): App {
    val app = App()

    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = extractProperties(tuple)

                when (elementName) {
                    "App" -> {
                        app.id = (properties["id"] as? PropertyValue.StringValue)?.value ?: ""
                        app.icon = (properties["icon"] as? PropertyValue.StringValue)?.value ?: ""
                        app.name = (properties["name"] as? PropertyValue.StringValue)?.value ?: ""
                        app.smlVersion = (properties["smlVersion"] as? PropertyValue.StringValue)?.value ?: ""
                        parseNestedAppElements(extractChildElements(tuple), app)
                    }
                }
            }
        }
    }
    return app
}

fun deserializeBook(parsedResult: List<Any>): Book {
    val book = Book()

    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = extractProperties(tuple)

                when (elementName) {
                    "Ebook" -> {
                        book.name = (properties["name"] as? PropertyValue.StringValue)?.value ?: ""
                        book.smlVersion = (properties["smlVersion"] as? PropertyValue.StringValue)?.value ?: ""
                        book.theme = (properties["theme"] as? PropertyValue.StringValue)?.value ?: ""
                        book.creator = (properties["creator"] as? PropertyValue.StringValue)?.value ?: ""
                        book.language = (properties["language"] as? PropertyValue.StringValue)?.value ?: ""
                        parseNestedBookElements(extractChildElements(tuple), book)
                    }
                }
            }
        }
    }
    return book
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
    val page = Page(color = "", backgroundColor = "", padding = Padding(0, 0, 0, 0), "false", elements = mutableListOf())
    val currentProject = GlobalProjectState.projectState
    val theme = currentProject?.app?.theme

    parsedResult.forEach { tuple ->
        when (tuple) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (tuple.t2 as? TokenMatch)?.text
                val properties = extractProperties(tuple)

                when (elementName) {
                    "Page" -> {

                        page.color = (properties["color"] as? PropertyValue.StringValue)?.value ?: (theme?.onBackground ?: "no")
                        page.backgroundColor = (properties["backgroundColor"] as? PropertyValue.StringValue)?.value ?: (theme?.background ?: "n0")
                        page.padding = parsePadding((properties["padding"] as? PropertyValue.StringValue)?.value ?: "0")
                        page.scrollable = (properties["scrollable"] as? PropertyValue.StringValue)?.value ?: "false"
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
    val currentProject = GlobalProjectState.projectState
    val theme = currentProject?.app?.theme

    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "Text" -> {
                        if (currentProject != null) {
                            if (theme != null) {
                                elements.add(
                                    UIElement.TextElement(
                                        text = (properties["text"] as? PropertyValue.StringValue)?.value ?: "",
                                        color = (properties["color"] as? PropertyValue.StringValue)?.value ?: theme.onBackground,
                                        fontSize = ((properties["fontSize"] as? PropertyValue.IntValue)?.value ?: 14).sp,
                                        fontWeight = fontWeightMap[(properties["fontWeight"] as? PropertyValue.StringValue)?.value ?: ""] ?: FontWeight.Normal,
                                        textAlign = textAlignMap[(properties["textAlign"] as? PropertyValue.StringValue)?.value ?: ""] ?: TextAlign.Unspecified
                                    )
                                )
                            }
                        }
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
                        if (theme != null) {
                            val md = ((properties["text"] as? PropertyValue.StringValue)?.value ?: "").split("\n")
                                .joinToString("\n") { it.trim() }
                            val ele = UIElement.MarkdownElement(
                                text = md,
                                color = (properties["color"] as? PropertyValue.StringValue)?.value
                                    ?: theme.onBackground,
                                fontSize = ((properties["fontSize"] as? PropertyValue.IntValue)?.value ?: 14).sp,
                                fontWeight = fontWeightMap[(properties["fontWeight"] as? PropertyValue.StringValue)?.value
                                    ?: ""] ?: FontWeight.Normal,
                                textAlign = textAlignMap[(properties["textAlign"] as? PropertyValue.StringValue)?.value
                                    ?: ""] ?: TextAlign.Unspecified
                            )
                            elements.add(ele)
                        }
                    }
                    "Button" -> {
                        val btn = UIElement.ButtonElement(
                            label = (properties["label"] as? PropertyValue.StringValue)?.value ?: "",
                            link = (properties["link"] as? PropertyValue.StringValue)?.value ?: "",
                            color = (properties["color"] as? PropertyValue.StringValue)?.value ?: "",
                            backgroundColor = (properties["backgroundColor"] as? PropertyValue.StringValue)?.value ?: ""
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
                            link = (properties["link"] as? PropertyValue.StringValue)?.value ?: "",
                            weight = (properties["weight"] as? PropertyValue.IntValue)?.value ?: 0
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
                            weight = (properties["weight"] as? PropertyValue.IntValue)?.value ?: 0
                        )
                        elements.add(vid)
                    }
                    "Youtube" -> {
                        val yt = UIElement.YoutubeElement(
                            id = (properties["id"] as? PropertyValue.StringValue)?.value ?: "",
                            weight = (properties["weight"] as? PropertyValue.IntValue)?.value ?: 0
                        )
                        elements.add(yt)
                    }
                }
            }
        }
    }
}

fun parsePadding(padding: String): Padding {
    val paddingValues = padding.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // Alle Seiten gleich
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertikal und Horizontal gleich
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Oben, Rechts, Unten, Links
        else -> Padding(0, 0, 0, 0)
    }
}

fun parseNestedBookElements(nestedElements: List<Any>, book: Book) {
    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "Part" -> {
                        book.parts.add(PartElement(
                            src = (properties["src"] as? PropertyValue.StringValue)?.value ?: "",
                            name = (properties["name"] as? PropertyValue.StringValue)?.value ?: ""))
                    }
                }
            }
        }
    }
}



fun parseNestedAppElements(nestedElements: List<Any>, app: App) {
    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "Navigation" -> {
                        val type = (properties["type"] as? PropertyValue.StringValue)?.value ?: ""
                        app.navigation.type = type
                        parseNestedNavElements(extractChildElements(element), app.navigation)
                    }
                    "Deployment" -> {
                        parseNestedDeployElements(extractChildElements(element), app.deployment)
                    }
                    "Theme" -> {
                        app.theme.error = (properties["error"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.scrim = (properties["scrim"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onError = (properties["onError"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.background = (properties["background"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.errorContainer = (properties["errorContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.inverseOnSurface = (properties["inverseOnSurface"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.inversePrimary = (properties["inversePrimary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.inverseSurface = (properties["inverseSurface"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onBackground = (properties["onBackground"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onErrorContainer = (properties["onErrorContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onPrimary = (properties["onPrimary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onPrimaryContainer = (properties["onPrimaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onSecondary = (properties["onSecondary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onSecondaryContainer = (properties["onSecondaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onSurface = (properties["onSurface"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onSurfaceVariant = (properties["onSurfaceVariant"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onTertiary = (properties["onTertiary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onTertiaryContainer = (properties["onTertiaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.outline = (properties["outline"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.outlineVariant = (properties["outlineVariant"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.primary = (properties["primary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.surface = (properties["surface"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onPrimaryContainer = (properties["error"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.secondary = (properties["onPrimaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.onSecondaryContainer = (properties["error"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.surfaceTint = (properties["onSecondaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.surfaceVariant = (properties["surfaceVariant"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.tertiary = (properties["tertiary"] as? PropertyValue.StringValue)?.value ?: ""
                        app.theme.tertiaryContainer = (properties["tertiaryContainer"] as? PropertyValue.StringValue)?.value ?: ""
                    }
                }
            }
        }
    }
}

fun parseNestedNavElements(nestedElements: List<Any>, navigation: NavigationElement) {
    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "Item" -> {
                        val page = (properties["page"] as? PropertyValue.StringValue)?.value ?: ""
                        navigation.items.add(ItemElement(page))
                    }
                }
            }
        }
    }
}


fun parseNestedDeployElements(nestedElements: List<Any>, deployment: DeploymentElement) {
    nestedElements.forEach { element ->
        when (element) {
            is Tuple7<*, *, *, *, *, *, *> -> {
                val elementName = (element.t2 as? TokenMatch)?.text
                val properties = extractProperties(element)

                when (elementName) {
                    "File" -> {
                        val path = (properties["path"] as? PropertyValue.StringValue)?.value ?: ""
                        val date = (properties["time"] as? PropertyValue.StringValue)?.value ?: ""
                        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH.mm.ss")
                        val dateTime = LocalDateTime.parse(date, formatter)
                        deployment.files.add(FileElement(path, dateTime))
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

fun parseBook(sml: String, ): Pair<Book?, String?> {
    try {
        val result = SmlGrammar.parseToEnd(sml)
        return Pair(deserializeBook(result), null)
    } catch(e: Exception) {
        return Pair(null, e.message)
    }
}

