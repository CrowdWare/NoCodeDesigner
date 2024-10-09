package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.io.File
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.model.extensionToNodeType
import at.crowdware.nocodelib.parseApp
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

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

        fun mapFileToTreeNode(file: File): TreeNode {
            val nodeType = getNodeType(file)
            val children = if (file.isDirectory) {
                file.listFiles()
                    ?.filter { it.name != ".DS_Store" }
                    ?.map { mapFileToTreeNode(it) } ?: emptyList()
            } else {
                emptyList()
            }
            val statefulChildren = SnapshotStateList<TreeNode>().apply {
                addAll(children)
            }
            return TreeNode(
                title = mutableStateOf( file.name),
                path = file.path,
                type = nodeType,
                children = statefulChildren
            )
        }

        val nodes = file.listFiles()
            ?.filter { it.name != ".DS_Store" }
            ?.map { mapFileToTreeNode(it) }
            ?: emptyList()
        val sortedNodes = nodes.sortedWith(compareBy<TreeNode> { it.type != NodeType.DIRECTORY }.thenBy { it.title.value })
        treeData = sortedNodes.toList()

        // app.xml load and parse
        try {
            val uiQml = File("$path/app.qml").readText()
            app = parseApp(uiQml)
            LoadFile("$path/pages/home.qml")
        } catch (e: Exception) {
            println("Error parsing app.qml: ${e.message}")
        }
    }

    override suspend fun createProjectFiles(path: String, uuid: String, pid: String, name: String, appId:String) {
        // TODO: copy default icon.png into assets
        // TODO: create qml instead of xml
        val dir = File("$path/$name")
        dir.mkdirs()
        val app = File("$path/$name/app.qml")
        val pages = File("$path/$name/pages")
        pages.mkdirs()
        val assets = File("$path/$name/assets")
        assets.mkdirs()
        val home = File("$path/$name/pages/home.qml")
        app.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<app name=\"$name\" id=\"$appId.$name\" icon=\"icon.png\">\n    <navigation type=\"HorizontalPager\">\n        <item page=\"home\"/>\n    </navigation>\n</app>\n")
        home.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<page>\n    <text>Home</text>\n</page>\n")
        copyResourceToFile("icons/default.icon.png", "$path/$name/assets/icon.png")
        LoadProject("$path/$name", uuid, pid)
    }
}

actual fun createProjectState(): ProjectState {
    return DesktopProjectState()
}

fun copyResourceToFile(resourcePath: String, outputPath: String) {
    // Lade die Datei aus den Ressourcen (im Klassenpfad)
    val classLoader = Thread.currentThread().contextClassLoader
    val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)

    if (inputStream != null) {
        // Zielpfad, wo die Datei gespeichert werden soll
        val targetPath = Paths.get(outputPath)

        // Stelle sicher, dass das Zielverzeichnis existiert
        Files.createDirectories(targetPath.parent)

        // Kopiere die Datei von inputStream in das Zielverzeichnis
        Files.copy(inputStream, targetPath)

        println("Datei wurde nach $outputPath kopiert.")
    } else {
        println("Ressource $resourcePath konnte nicht gefunden werden.")
    }
}

actual fun fileExists(path: String): Boolean {
    return File(path).exists()
}

actual fun deleteFile(path: String) {
    File(path).delete()
}

actual fun createPage(path: String) {
    println("addpage: $path")
    val file = File(path)
    file.createNewFile()
    file.writeText("Page {\n\n}")
}

actual fun renameFile(pathBefore: String, pathAfter: String) {
    File(pathBefore).renameTo(File(pathAfter))
}