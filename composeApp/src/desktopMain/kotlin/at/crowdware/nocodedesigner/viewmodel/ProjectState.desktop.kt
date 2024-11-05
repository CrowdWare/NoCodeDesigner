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

package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import at.crowdware.nocodedesigner.model.*
import java.io.File
import at.crowdware.nocodedesigner.utils.parseApp
import at.crowdware.nocodedesigner.utils.parseBook
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
            val allowedFolderNames = listOf("images", "videos", "sounds", "pages", "parts", "models")
            val nodeType = getNodeType(file)
            val children = if (file.isDirectory) {
                file.listFiles()
                    ?.filter { it.name != ".DS_Store" }
                    ?.flatMap {
                        if (it.isDirectory && allowedFolderNames.contains(it.name)) {
                            it.listFiles()?.filter { file -> file.name != ".DS_Store" }?.map { mapFileToTreeNode(it) } ?: emptyList()
                        } else if (!it.isDirectory) {
                            listOf(mapFileToTreeNode(it))
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()
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
            } else if (node.title.value == "images") {
                imagesNode = node
            } else if (node.title.value == "videos") {
                videosNode = node
            } else if (node.title.value == "sounds") {
                soundsNode = node
            } else if (node.title.value == "parts") {
                partsNode = node
            } else if (node.title.value == "models") {
                modelsNode = node
            } else if (node.title.value == "textures") {
                texturesNode = node
            }
            return node
        }

        val nodes = file.listFiles()
            // Python 3 server.py runs the webserver for NoCodeBrowser testing
            ?.filter {
                it.name != ".DS_Store" &&
                        !it.name.endsWith(".py") &&
                        (it.isDirectory && it.name in listOf("pages", "parts", "images", "sounds", "videos", "models", "textures")) ||
                        (it.isFile && it.name in listOf("app.sml", "book.sml")) // Füge die gewünschten Dateien hinzu
            }
            ?.map { mapFileToTreeNode(it) }
            ?: emptyList()
        val sortedNodes = nodes.sortedWith(compareBy<TreeNode> { it.type != NodeType.DIRECTORY }.thenBy { it.title.value })
        treeData = sortedNodes.toList()
        folder = path

        // app.sml load and parse
        val appFile =  File("$folder/app.sml")
        if (appFile.exists()) {
            loadApp()
            LoadFile("$folder/pages/home.sml")
        }

        // book.sml load and parse
        val bookFile = File("$folder/book.sml")
        if(bookFile.exists()) {
            loadBook()
            LoadFile("$folder/parts/home.md")
        }
    }

    override fun loadApp() {
        val appFile = File("$folder/app.sml")
        try {
            val uiSml = appFile.readText()
            val result = parseApp(uiSml)
            app = result.first
        } catch (e: Exception) {
            println("Error parsing app.sml: ${e.message}")
        }
    }

    override fun loadBook() {
        val bookFile = File("$folder/book.sml")
        try {
            val uiSml = bookFile.readText()
            val result = parseBook(uiSml)
            book = result.first
        } catch (e: Exception) {
            println("Error parsing book.sml: ${e.message}")
        }
    }

    override suspend fun createProjectFiles(
        path: String,
        uuid: String,
        pid: String,
        name: String,
        appId: String,
        theme: String,
        createBook: Boolean,
        createApp: Boolean
    ) {
        val dir = File("$path$name")
        dir.mkdirs()
        if(createApp) {
            val pages = File("$path$name/pages")
            pages.mkdirs()
            val videos = File("$path$name/videos")
            videos.mkdirs()
            val sounds = File("$path$name/sounds")
            sounds.mkdirs()
            val images = File("$path$name/images")
            images.mkdirs()
            val models = File("$path$name/models")
            models.mkdirs()
            val textures = File("$path$name/textures")
            textures.mkdirs()
            val app = File("$path$name/app.sml")
            var appContent = "App {\n  smlVersion: \"1.0\"\n  name: \"$name\"\n  version: \"1.0\"\n  id: \"$appId.$name\"\n  icon: \"icon.png\"\n\n  Navigation {\n    type: \"HorizontalPager\"\n\n    Item { page: \"home\" }  \n  }\n"
            if(theme == "Light")
                appContent += writeLightTheme()
            else
                appContent += writeDarkTheme()
            appContent += "// deployment start - don't edit here\n\n// deployment end\n}\n\n"
            app.writeText(appContent)

            val home = File("$path$name/pages/home.sml")
            home.writeText("Page {\n  padding: \"8\"\n\n  Column {\n    padding: \"8\"\n\n    Text { text: \"Home\" }\n  }\n}\n")
            copyResourceToFile("python/server.py", "$path/$name/server.py")
            copyResourceToFile("python/upd_deploy.py", "$path/$name/upd_deploy.py")
            copyResourceToFile("icons/default.icon.png", "$path/$name/images/icon.png")
        }

        if (createBook) {
            val parts = File("$path$name/parts")
            parts.mkdirs()
            val homemd = File("$path$name/parts/home.md")
            homemd.writeText("# BookTitle\nLorem ipsum dolor\n")

            val book = File("$path$name/book.sml")
            var bookContent = "Ebook {\n  smlVersion: \"1.0\"\n  name: \"$name\"\n  version: \"1.0\"\n  theme: \"Epub3\"\n  creator: \"\"\n  language: \"en\"\n\n  Part {\n    src: \"home.md\"\n  }\n}\n"
            book.writeText(bookContent)
        }

        val images = File("$path$name/images")
        images.mkdirs()

        LoadProject("$path$name", uuid, pid)
    }
}

fun writeDarkTheme(): String {
    var content = "\n"
    content += "  Theme {\n"
    content += "    primary: \"#FFB951\"\n"
    content += "    onPrimary: \"#452B00\"\n"
    content += "    primaryContainer: \"#633F00\"\n"
    content += "    onPrimaryContainer: \"#FFDDB3\"\n"
    content += "    secondary: \"#DDC2A1\"\n"
    content += "    onSecondary: \"#3E2D16\"\n"
    content += "    secondaryContainer: \"#56442A\"\n"
    content += "    onSecondaryContainer: \"#FBDEBC\"\n"
    content += "    tertiary: \"#B8CEA1\"\n"
    content += "    onTertiary: \"#243515\"\n"
    content += "    tertiaryContainer: \"#3A4C2A\"\n"
    content += "    onTertiaryContainer: \"#D4EABB\"\n"
    content += "    error: \"#FFB4AB\"\n"
    content += "    errorContainer: \"#93000A\"\n"
    content += "    onError: \"#690005\"\n"
    content += "    onErrorContainer: \"#FFDAD6\"\n"
    content += "    background: \"#1F1B16\"\n"
    content += "    onBackground: \"#EAE1D9\"\n"
    content += "    surface: \"#1F1B16\"\n"
    content += "    onSurface: \"#EAE1D9\"\n"
    content += "    surfaceVariant: \"#4F4539\"\n"
    content += "    onSurfaceVariant: \"#D3C4B4\"\n"
    content += "    outline: \"#9C8F80\"\n"
    content += "    inverseOnSurface: \"#1F1B16\"\n"
    content += "    inverseSurface: \"#EAE1D9\"\n"
    content += "    inversePrimary: \"#825500\"\n"
    content += "    surfaceTint: \"#FFB951\"\n"
    content += "    outlineVariant: \"#4F4539\"\n"
    content += "    scrim: \"#000000\"\n"
    content += "  }\n\n"
    return content
}

fun writeLightTheme(): String {
    var content = "\n"
    content += "  Theme {\n"
    content += "    primary: \"#825500\"\n"
    content += "    onPrimary: \"#FFFFFF\"\n"
    content += "    primaryContainer: \"#FFDDB3\"\n"
    content += "    onPrimaryContainer: \"#291800\"\n"
    content += "    secondary: \"#6F5B40\"\n"
    content += "    onSecondary: \"#FFFFFF\"\n"
    content += "    secondaryContainer: \"#FBDEBC\"\n"
    content += "    onSecondaryContainer: \"#271904\"\n"
    content += "    tertiary: \"#51643F\"\n"
    content += "    onTertiary: \"#FFFFFF\"\n"
    content += "    tertiaryContainer: \"#D4EABB\"\n"
    content += "    onTertiaryContainer: \"#102004\"\n"
    content += "    error: \"#BA1A1A\"\n"
    content += "    errorContainer: \"#FFDAD6\"\n"
    content += "    onError: \"#FFFFFF\"\n"
    content += "    onErrorContainer: \"#410002\"\n"
    content += "    background: \"#FFFBFF\"\n"
    content += "    onBackground: \"#1F1B16\"\n"
    content += "    surface: \"#FFFBFF\"\n"
    content += "    onSurface: \"#1F1B16\"\n"
    content += "    surfaceVariant: \"#F0E0CF\"\n"
    content += "    onSurfaceVariant: \"#4F4539\"\n"
    content += "    outline: \"#817567\"\n"
    content += "    inverseOnSurface: \"#F9EFE7\"\n"
    content += "    inverseSurface: \"#34302A\"\n"
    content += "    inversePrimary: \"#FFB951\"\n"
    content += "    surfaceTint: \"#825500\"\n"
    content += "    utlineVariant: \"#D3C4B4\"\n"
    content += "    scrim: \"#000000\"\n"
    content += "  }\n\n"
    return content
}

actual fun createProjectState(): ProjectState {
    return DesktopProjectState()
}

actual fun copyResourceToFile(resourcePath: String, outputPath: String) {
    val classLoader = Thread.currentThread().contextClassLoader
    val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)

    if (inputStream != null) {
        val targetPath = Paths.get(outputPath)
        Files.createDirectories(targetPath.parent)
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
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

actual fun createPage(path: String, title: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("Page {\n\ttitle:\"$title\"\n}")
}

actual fun createPart(path: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("# Header\nLorem ipsum dolor\n")
}

actual fun renameFile(pathBefore: String, pathAfter: String) {
    File(pathBefore).renameTo(File(pathAfter))
}

actual fun copyAssetFile(path: String, target: String) {
    val source: Path = Path.of(path)
    val target: Path = Path.of(target)
    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)
}
