package at.crowdware.nocodedesigner.utils


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

fun testQML() {
    val parsedTree = parseQML(qmlString)
    printParsedTree(parsedTree)
    //println(parsedTree.toString())
}