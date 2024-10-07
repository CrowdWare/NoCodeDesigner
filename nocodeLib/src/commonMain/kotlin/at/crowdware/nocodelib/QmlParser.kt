/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    return Page(color, backgroundColor, padding, elements as MutableList<UIElement>)
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
            // Neuer Block ohne Eigenschaften erkannt
            line.endsWith("{") && !line.contains(":") -> {
                println("node without properties")
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }
                stack.add(currentNode)
            }
            // Knoten mit Eigenschaften in derselben Zeile (z.B. Text { content: "Zeile 1" })
            line.contains("{") && line.contains("}") -> {
                println("node with property and close")
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }

                // Verarbeite die Eigenschaften
                val propertiesPart = line.substringAfter("{").substringBefore("}").trim()
                if (propertiesPart.isNotEmpty()) {
                    val regex = """(\w+):\s*("[^"]*"|[^"\s]+)""".toRegex()
                    regex.findAll(propertiesPart).forEach { matchResult ->
                        val (key, value) = matchResult.destructured
                        currentNode.properties.add(key to processStringLiteral(value))
                    }
                }

                // Füge den aktuellen Knoten zum Stack hinzu, da er Kinder haben könnte
                stack.add(currentNode)
            }
            // Neuer Block mit Eigenschaften erkannt, der nicht direkt geschlossen wird
            line.endsWith("{") && line.contains(":") -> {
                println("node with properties opening")
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }

                // Verarbeite die Eigenschaften, falls vorhanden
                val propertiesPart = line.substringAfter("{").trim()
                if (propertiesPart.isNotEmpty()) {
                    val regex = """(\w+):\s*("[^"]*"|[^"\s]+)""".toRegex()
                    regex.findAll(propertiesPart).forEach { matchResult ->
                        val (key, value) = matchResult.destructured
                        currentNode.properties.add(key to processStringLiteral(value))
                    }
                }

                // Füge den Knoten in den Stack ein
                stack.add(currentNode)
            }

            line.contains("{") -> {
                println("Node with properties opening")
                val name = line.substringBefore("{").trim()
                currentNode = QMLNode(name, mutableListOf(), mutableListOf())

                if (stack.isNotEmpty()) {
                    stack.last().children.add(currentNode)
                }

                // Verarbeite die Eigenschaften, falls vorhanden
                val propertiesPart = line.substringAfter("{").substringBefore("}").trim()
                if (propertiesPart.isNotEmpty()) {
                    val regex = """(\w+):\s*("[^"]*"|[^"\s]+)""".toRegex()
                    regex.findAll(propertiesPart).forEach { matchResult ->
                        val (key, value) = matchResult.destructured
                        currentNode.properties.add(key to processStringLiteral(value))
                    }
                }

                // Füge den Knoten in den Stack ein
                stack.add(currentNode)
            }
            // Block-Ende erkannt
            line.endsWith("}") -> {
                println("node closed")
                currentNode = stack.removeAt(stack.size - 1)
            }
            // Eigenschaft erkannt
            line.contains(":") && !readingMultilineContent -> {
                println("property found")
                val (key, value) = line.split(":", limit = 2).map { it.trim() }

                if (value == "\"" || (value.startsWith("\"") && !value.endsWith("\""))) {
                    readingMultilineContent = true
                    multilinePropertyKey = key
                    multilineContent.append(value.drop(1))  // Erstes Anführungszeichen entfernen
                } else {
                    currentNode.properties.add(key to processStringLiteral(value))
                }
            }
            // Mehrzeiliger Inhalt erkannt
            readingMultilineContent -> {
                if (line.endsWith("\"")) {
                    multilineContent.appendLine(line.dropLast(1))  // Abschließendes Anführungszeichen entfernen
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

    return root.children.firstOrNull() ?: root // Falls keine Knoten geparst wurden
}