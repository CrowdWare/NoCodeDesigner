package at.crowdware.nocodedesigner.viewmodel

import at.crowdware.nocodelib.AppParser
import at.crowdware.nocodelib.PageParser
import java.io.File
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.model.extensionToNodeType
import java.io.IOException

actual fun getNodeType(path: String): NodeType {
    val file = File(path)
    return when {
        file.isDirectory -> NodeType.DIRECTORY
        else -> extensionToNodeType[file.extension.lowercase()] ?: NodeType.OTHER
    }
}

actual fun getDisplayName(path: String): String {
    val file = File(path)
    return if (file.isDirectory) file.name else file.nameWithoutExtension
}

actual suspend fun loadFileContent(path: String, uuid: String, pid: String): String {
    val file = File(path)
    return try {
        file.readText()
    } catch (e: IOException) {
        throw IOException("Error reading file: ${e.message}", e)
    }
}

actual fun saveFileContent(path: String, uuid: String, pid: String, content: String) {
    val file = File(path)
    try {
        file.writeText(content)
    } catch (e: IOException) {
        throw IOException("Error writing to file: ${e.message}", e)
    }
}

class DesktopProjectState : ProjectState() {
    override suspend fun loadProjectFiles(path: String, uuid: String, pid: String) {
        val file = File(path)

        // Funktion zur Ermittlung des NodeType basierend auf der Dateiendung
        fun getNodeType(file: File): NodeType {
            return if (file.isDirectory) {
                NodeType.DIRECTORY
            } else {
                val extension = file.extension.lowercase()
                extensionToNodeType[extension] ?: NodeType.OTHER
            }
        }

        // Rekursive Funktion zur Verarbeitung von Dateien und Verzeichnissen
        fun mapFileToTreeNode(file: File): TreeNode {
            val nodeType = getNodeType(file)
            val children = if (file.isDirectory) {
                file.listFiles()
                    ?.filter { it.name != ".DS_Store" }
                    ?.map { mapFileToTreeNode(it) } ?: emptyList()
            } else {
                emptyList()
            }

            return TreeNode(
                title = file.name,
                path = file.path,
                type = nodeType,
                children = children
            )
        }

        //val nodes = file.listFiles()?.map { mapFileToTreeNode(it) } ?: emptyList()
        val nodes = file.listFiles()
            ?.filter { it.name != ".DS_Store" } // Filtert .DS_Store Dateien heraus
            ?.map { mapFileToTreeNode(it) }
            ?: emptyList()
        val sortedNodes = nodes.sortedWith(compareBy<TreeNode> { it.type != NodeType.DIRECTORY }.thenBy { it.title })
        treeData = sortedNodes.toList()

        // app.xml load and parse
        try {
            val uiXml = File("$path/app.xml").readText()
            val appParser = AppParser()
            app = appParser.parse(uiXml)
        } catch (e: Exception) {
            println("Error parsing app.xml: ${e.message}")
        }
    }
}

actual fun createProjectState(): ProjectState {
    return DesktopProjectState()
}