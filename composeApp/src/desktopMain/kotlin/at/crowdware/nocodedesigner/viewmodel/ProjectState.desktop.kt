package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import at.crowdware.nocodedesigner.model.*
import java.io.File
import at.crowdware.nocodedesigner.utils.parseApp
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

actual fun getNodeType(path: String): NodeType {
    val file = File(path)
    return when {
        file.isDirectory -> NodeType.DIRECTORY
        else -> extensionToNodeType[file.extension.lowercase()] ?: NodeType.OTHER
    }
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
        file.setLastModified(System.currentTimeMillis())
    } catch (e: IOException) {
        throw IOException("Error writing to file: ${e.message}", e)
    }
}

class DesktopProjectState : ProjectState() {
    override suspend fun loadProjectFiles(path: String, uuid: String, pid: String) {
        val file = File(path)

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
            val node = TreeNode(
                title = mutableStateOf( file.name),
                path = file.path,
                type = nodeType,
                children = statefulChildren
            )
            if (node.title.value == "pages") {
                pageNode = node
            } else if (node.title.value == "assets") {
                assetsNode = node
            }
            return node
        }

        val nodes = file.listFiles()
            // python3 server.py runs the webserver for NoCodeBrowser testing
            ?.filter { it.name != ".DS_Store" && !it.name.endsWith(".py")}
            ?.map { mapFileToTreeNode(it) }
            ?: emptyList()
        val sortedNodes = nodes.sortedWith(compareBy<TreeNode> { it.type != NodeType.DIRECTORY }.thenBy { it.title.value })
        treeData = sortedNodes.toList()

        // app.sml load and parse
        try {
            val uiSml = File("$path/app.sml").readText()
            val result = parseApp(uiSml)
            app = result.first
            LoadFile("$path/pages/home.sml")
        } catch (e: Exception) {
            println("Error parsing app.sml: ${e.message}")
        }
    }

    override suspend fun createProjectFiles(path: String, uuid: String, pid: String, name: String, appId:String) {
        // TODO: copy default icon.png into assets
        val dir = File("$path/$name")
        dir.mkdirs()
        val app = File("$path/$name/app.sml")
        val pages = File("$path/$name/pages")
        pages.mkdirs()
        val assets = File("$path/$name/assets")
        assets.mkdirs()
        val home = File("$path/$name/pages/home.sml")
        app.writeText("App {\n  smlVersion: \"1.0\"\n  name: \"$name\"\n  version: \"1.0\"\n  id: \"$appId.$name\"\n  icon: \"icon.png\"\n\n  Navigation {\n    type: \"HorizontalPager\"\n\n    Item { page: \"home\" }  \n  }\n// deployment start - don't edit here\n\n// deployment end\n}\n")
        home.writeText("Page {\n  backgroundColor: \"#FFFFFF\"\n  padding: \"8\"\n\n  Column {\n    padding: \"8\"\n\n    Text { text: \"Home\" }\n  }\n}\n")
        copyResourceToFile("icons/default.icon.png", "$path/$name/assets/icon.png")
        copyResourceToFile("python/server.py", "$path/$name/server.py")
        copyResourceToFile("python/upd_deploy.py", "$path/$name/upd_deploy.py")
        LoadProject("$path/$name", uuid, pid)
    }
}

actual fun createProjectState(): ProjectState {
    return DesktopProjectState()
}

fun copyResourceToFile(resourcePath: String, outputPath: String) {
    val classLoader = Thread.currentThread().contextClassLoader
    val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)

    if (inputStream != null) {
        val targetPath = Paths.get(outputPath)

        Files.createDirectories(targetPath.parent)

        Files.copy(inputStream, targetPath)
    } else {
        println("Ressource $resourcePath could not be found.")
    }
}

actual fun fileExists(path: String): Boolean {
    return File(path).exists()
}

actual fun deleteFile(path: String) {
    File(path).delete()
}

actual fun createPage(path: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("Page {\n\n}")
}

actual fun renameFile(pathBefore: String, pathAfter: String) {
    File(pathBefore).renameTo(File(pathAfter))
}

actual fun copyAssetFile(path: String, target: String) {
    val source: Path = Path.of(path)
    val target: Path = Path.of(target)
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
}
