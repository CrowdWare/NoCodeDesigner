package at.crowdware.nocodelib

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

data class QMLNode(
    val name: String,
    val properties: MutableList<Pair<String, String>> = mutableListOf(),
    val children: MutableList<QMLNode> = mutableListOf()
)

val qmlString = """
    Column {
        padding: "0 3 45 2"
        Markdown {
            color: "#FFFFFF"
            content: "
            # header 1
            ## header 2
            "
        }
        Markdown { content: "# header 1\n## header 2" }
        Button {
            label: "Click" 
            link: "web:http://bla.de"
            Icon {
                width: 78
            }
        }
    }
"""

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

    return root
}

fun processStringLiteral(value: String): String {
    return if (value.startsWith("\"") && value.endsWith("\"")) {
        value.trim().removeSurrounding("\"").replace("\\n", "\n")
    } else {
        value.trim()
    }
}

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
}

fun deserializeApp(properties: Map<String, String>): App {
    val type = properties["type"] ?: "default"
    val items = properties["items"]?.split(",")?.toMutableList() ?: mutableListOf()

    return App(type, items)
}

fun deserializePage(properties: Map<String, String>, elements: List<UIElement>): Page {
    val color = properties["color"] ?: "#FFFFFF"
    val backgroundColor = properties["backgroundColor"] ?: "#000000"
    val padding = deserializePadding(properties["padding"])

    return Page(color, backgroundColor, padding, elements)
}

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


fun deserializePadding(paddingString: String?): Padding {
    return paddingString?.split(",")?.let {
        Padding(
            top = it.getOrNull(0)?.toInt() ?: 0,
            right = it.getOrNull(1)?.toInt() ?: 0,
            bottom = it.getOrNull(2)?.toInt() ?: 0,
            left = it.getOrNull(3)?.toInt() ?: 0
        )
    } ?: Padding(0, 0, 0, 0)
}

val elements = mutableListOf<UIElement>()

// Beispielhafte Nutzung
//elements.add(deserializeUIElement(elementProperties)) // Hier `elementProperties` sollte ein Map<String, String> sein

val properties = mapOf("color" to "#FFFFFF", "backgroundColor" to "#000000", "padding" to "10,10,10,10")
val page = deserializePage(properties, elements)


fun testQML() {
    val parsedTree = parseQML(qmlString)
    printParsedTree(parsedTree)
}