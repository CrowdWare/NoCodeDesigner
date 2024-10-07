package at.crowdware.nocodelib

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit

data class QMLNode(
    val name: String,
    val properties: MutableList<Pair<String, String>> = mutableListOf(),
    val children: MutableList<QMLNode> = mutableListOf()
)

class QmlAppParser() {
    fun parse(xmlData: String): App {
        return App(type = "", items = mutableListOf<String>())
    }
}

fun isQmlRootElement(qmlString: String, root: String): Boolean {
    val regex = Regex("""^\s*$root\s*\{""")
    return regex.containsMatchIn(qmlString)
}

class QmlPageParser() {
    fun parse(qmlData: String): Page {
        println("parsing page")
        val parsedTree = parseQML(qmlData)
        println("parsed tree: $parsedTree")
        return deserializePage(parsedTree)
    }
}

fun parseQML(qml: String): QMLNode {
    val root = QMLNode("Root", mutableListOf(), mutableListOf())
    val stack = mutableListOf(root)
    var currentNode: QMLNode = root
    val lines = qml.lines().iterator()
    var readingMultilineContent = false
    var multilinePropertyKey: String? = null
    val multilineContent = StringBuilder()

    while (lines.hasNext()) {
        val line = lines.next().trim()

        when {
            line.endsWith("{") -> {
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }
                stack.add(currentNode)
            }
            line.contains("{") && line.contains("}") -> {
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }

                val properties = line.substringAfter("{").substringBefore("}").trim()
                if (properties.isNotEmpty()) {
                    properties.split(";").forEach { prop ->
                        val (key, value) = prop.split(":", limit = 2).map { it.trim() }
                        currentNode.properties.add(key to processStringLiteral(value))
                    }
                }
                currentNode = stack.removeAt(stack.size - 1)
            }
            line.endsWith("}") -> {
                currentNode = stack.removeAt(stack.size - 1)
            }
            line.contains(":") && !readingMultilineContent -> {
                val (key, value) = line.split(":", limit = 2).map { it.trim() }

                if (value == "\"" || (value.startsWith("\"") && !value.endsWith("\""))) {
                    readingMultilineContent = true
                    multilinePropertyKey = key
                    multilineContent.append(value.drop(1))
                } else {
                    currentNode.properties.add(key to processStringLiteral(value))
                }
            }
            readingMultilineContent -> {

                if (line.endsWith("\"")) {
                    multilineContent.appendLine(line.dropLast(1))
                    currentNode.properties.add(multilinePropertyKey!! to multilineContent.toString().trim())
                    multilineContent.clear()
                    readingMultilineContent = false
                    multilinePropertyKey = null
                } else {
                    multilineContent.appendLine(line)
                }
            }
            else -> {
                if (readingMultilineContent) {
                    multilineContent.appendLine(line)
                }
            }
        }
    }
    // we only need root temporarily
    return root.children.first()
}

fun processStringLiteral(value: String): String {
    return if (value.startsWith("\"") && value.endsWith("\"")) {
        value.trim().removeSurrounding("\"").replace("\\n", "\n")
    } else {
        value.trim()
    }
}

fun deserializeApp(properties: Map<String, String>): App {
    val type = properties["type"] ?: "default"
    val items = properties["items"]?.split(",")?.toMutableList() ?: mutableListOf()

    return App(type, items)
}

fun deserializePage(node: QMLNode?): Page {
    if (node == null) {
        val color = "#FFFFFF"
        val backgroundColor = "#000000"
        val padding = Padding(0,0,0,0)
        return Page(color, backgroundColor, padding, mutableListOf<UIElement>())
    }

    val properties = node.properties.toMap()
    val color = properties["color"] ?: "#FFFFFF"
    val backgroundColor = properties["backgroundColor"] ?: "#000000"
    val padding = parsePadding(properties["padding"].toString())
    val elements = node.children.map { deserializeUIElement(it) }

    return Page(color, backgroundColor, padding, elements)
}

fun deserializeUIElement(node: QMLNode): UIElement {
    println("deserializeElement: ${node.name}")
    val properties = node.properties.toMap()
    return when(node.name) {
        "Text" -> {
            TextElement(
                text = properties["content"] ?: "",
                color = properties["color"] ?: "#000000",
                fontSize = properties["fontSize"]?.toFloat()?.sp ?: 12f.sp,
                fontWeight = FontWeight.Normal, //FontWeight(properties["fontWeight"] ?: "normal"),
                textAlign = TextAlign.Left      //TextAlign(properties["textAlign"] ?: "left")
            )
        }
        "Button" -> {
            ButtonElement(
                label = properties["label"] ?: "",
                link = properties["link"] ?: ""
            )
        }
        "Column" -> {
            ColumnElement(
                padding = parsePadding(properties["padding"].toString()),
                uiElements = node.children.map { deserializeUIElement(it) }.toMutableList()
            )
        } else -> throw IllegalArgumentException("Unbekannter Elementtyp: $node.name")
    }
    //return TextElement(text = "", color = "#000000", fontSize = 12.sp, fontWeight = FontWeight.Normal, textAlign = TextAlign.Left)
}
/*
fun deserializeUIElement(properties: Map<String, String>): UIElement {
    return when (properties["type"]) {
        "Text" -> TextElement(
            text = properties["text"] ?: "",
            color = properties["color"] ?: "#000000",
            fontSize = 12.sp,               //properties["fontSize"]?.toFloat()?.let { TextUnit(it) } ?: TextUnit(12f),
            fontWeight = FontWeight.Normal, //FontWeight(properties["fontWeight"] ?: "normal"),
            textAlign = TextAlign.Left      //TextAlign(properties["textAlign"] ?: "left")
        )
        "Button" -> ButtonElement(
            label = properties["label"] ?: "",
            link = properties["link"] ?: ""
        )
        "Image" -> ImageElement(
            src = properties["src"] ?: "",
            scale = properties["scale"] ?: "fit",
            link = properties["link"] ?: ""
        )
        // Weitere Elemente hier hinzufügen...
        else -> throw IllegalArgumentException("Unbekannter Elementtyp")
    }
}
*/
/*
fun deserializePadding(paddingString: String?): Padding {
    return paddingString?.split(" ")?.let {
        Padding(
            top = it.getOrNull(0)?.toInt() ?: 0,
            right = it.getOrNull(1)?.toInt() ?: 0,
            bottom = it.getOrNull(2)?.toInt() ?: 0,
            left = it.getOrNull(3)?.toInt() ?: 0
        )
    } ?: Padding(0, 0, 0, 0)
}
*/
/*
fun testQML() {
    val parsedTree = parseQML(qmlString)
    printParsedTree(parsedTree)
}*/

/*
fun printParsedTree(node: QMLNode?, indent: Int = 0) {
    node?.let {
        println("  ".repeat(indent) + "Element: ${it.name}")
        for (prop in it.properties) {
            println("  ".repeat(indent + 1) + "Property: ${prop.first} = ${prop.second}")
        }
        for (child in it.children) {
            printParsedTree(child, indent + 1)
        }
    }
}*/