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

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.utils.*
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.reflect.KClass


expect fun getNodeType(path: String): NodeType
expect suspend fun loadFileContent(path: String, uuid: String, pid: String): String
expect fun saveFileContent(path: String, uuid: String, pid: String, content: String)
expect fun createProjectState(): ProjectState
expect fun fileExists(path: String): Boolean
expect fun deleteFile(path: String)
expect fun createPage(path: String, title: String)
expect fun createPart(path: String)
expect fun renameFile(pathBefore: String, pathAfter: String)
expect fun copyAssetFile(path: String, target: String)
expect fun copyResourceToFile(resourcePath: String, outputPath: String)

abstract class ProjectState {
    var currentFileContent by mutableStateOf(TextFieldValue(""))
    var projectName by mutableStateOf("")
        private set
    var fileName by mutableStateOf("No file loaded")
    var folder by mutableStateOf("")
    var path by mutableStateOf("")
    var treeData by mutableStateOf<List<TreeNode>>(emptyList())
    var elementData by mutableStateOf<List<TreeNode>>(emptyList())
    var extension by mutableStateOf("")
        private set
    var isPageDialogVisible by mutableStateOf(false)
    var isPartDialogVisible by mutableStateOf(false)
    var isRenameFileDialogVisible by mutableStateOf(false)
    var isProjectStructureVisible by mutableStateOf(true)
    var isNewProjectDialogVisible by mutableStateOf(false)
    var isOpenProjectDialogVisible by mutableStateOf(false)
    var isImportImageDialogVisible by mutableStateOf(false)
    var isImportVideoDialogVisible by mutableStateOf(false)
    var isImportSoundDialogVisible by mutableStateOf(false)
    var isImportModelDialogVisible by mutableStateOf(false)
    var isImportTextureDialogVisible by mutableStateOf(false)
    var isCreateEbookVisible by mutableStateOf(false)
    var isCreateAPKVisible by mutableStateOf(false)
    var isCreateHTMLVisible by mutableStateOf(false)
    var isAboutDialogOpen by  mutableStateOf(false)
    var isEditorVisible by mutableStateOf(false)
    var darkMode by mutableStateOf(false)
    var currentTreeNode by mutableStateOf(null as TreeNode?)
    var isPageLoaded by mutableStateOf(false)
    var actualElement: KClass<*>? by mutableStateOf(null)
    var parseError: String? by mutableStateOf(null)

    lateinit var pageNode: TreeNode
    lateinit var imagesNode: TreeNode
    lateinit var videosNode: TreeNode
    lateinit var soundsNode: TreeNode
    lateinit var partsNode: TreeNode
    lateinit var modelsNode: TreeNode
    lateinit var texturesNode: TreeNode
    var app: App? by mutableStateOf(null)
    var book: Book? by mutableStateOf(null)
    var page: Page? by mutableStateOf(null)
    var cachedPage: Page? by mutableStateOf(null)

    abstract fun loadApp()
    abstract fun loadBook()
    abstract suspend fun loadProjectFiles(path: String, uuid: String, pid: String)
    abstract suspend fun createProjectFiles(
        path: String,
        uuid: String,
        pid: String,
        name: String,
        appId: String,
        theme: String,
        createBook: Boolean,
        createApp: Boolean
    )

    fun createEbook(title: String, folder: String) {
        book?.let { CreateEbook.start(title, folder, this.folder, it) }
    }

    fun createAPK(title: String, folder: String) {
        app?.let { CreateAPK.start(title, folder, this.folder, it, this) }
    }

    fun createHTML(folder: String) {
        app!!.deployDirHtml  = folder
        save(app!!)
        app?.let { CreateHTML.start(folder, this.folder, it) }
    }

    fun LoadProject(path: String = folder, uuid: String, pid: String) {
        folder = path
        projectName = path

        CoroutineScope(Dispatchers.Main).launch {
            loadProjectFiles(path, uuid, pid)
        }
    }

    fun ImportImageFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast("/")
            val target = "$folder/images/$filename"
            copyAssetFile(file.path, target)
            val pngTarget = if (!target.endsWith(".png")) {
                val pngPath = target.substringBeforeLast(".") + ".png"
                convertToPng(File(target), File(pngPath))
                pngPath
            } else {
                target
            }
            val node = TreeNode(title = mutableStateOf(filename), path = pngTarget, type = getNodeType(target))
            imagesNode.children.add(node)
        }
    }

    fun save(app: App) {
        // TODO: Navigation is missing, but not used yet
        val file = File(folder, "app.sml")
        var sml = "App {\n"
        sml += "\tsmlVersion: \"${app.smlVersion}\"\n"
        sml += "\tname: \"${app.name}\"\n"
        sml += "\tdescription: \"${app.description}\"\n"
        sml += "\tid: \"${app.id}\"\n"
        sml += "\ticon: \"${app.icon}\"\n"
        sml += "\tdeployDirHtml: \"${app.deployDirHtml}\"\n"
        sml += "\n"
        sml += "\tTheme {\n"
        sml += "\t\tprimary: \"" + app.theme.primary.toString() + "\"\n"
        sml += "\t\tonPrimary: \"" + app.theme.onPrimary.toString() + "\"\n"
        sml += "\t\tprimaryContainer: \"" + app.theme.primaryContainer.toString() + "\"\n"
        sml += "\t\tonPrimaryContainer: \"" + app.theme.onPrimaryContainer.toString() + "\"\n"
        sml += "\t\tsecondary: \"" + app.theme.secondary.toString() + "\"\n"
        sml += "\t\tonSecondary: \"" + app.theme.onSecondary.toString() + "\"\n"
        sml += "\t\tsecondaryContainer: \"" + app.theme.secondaryContainer.toString() + "\"\n"
        sml += "\t\tonSecondaryContainer: \"" + app.theme.onSecondaryContainer.toString() + "\"\n"
        sml += "\t\ttertiary: \"" + app.theme.tertiary.toString() + "\"\n"
        sml += "\t\tonTertiary: \"" + app.theme.onTertiary.toString() + "\"\n"
        sml += "\t\ttertiaryContainer: \"" + app.theme.tertiaryContainer.toString() + "\"\n"
        sml += "\t\tonTertiaryContainer: \"" + app.theme.onTertiaryContainer.toString() + "\"\n"
        sml += "\t\terror: \"" + app.theme.error.toString() + "\"\n"
        sml += "\t\terrorContainer: \"" + app.theme.errorContainer.toString() + "\"\n"
        sml += "\t\tonError: \"" + app.theme.onError.toString() + "\"\n"
        sml += "\t\tonErrorContainer: \"" + app.theme.onErrorContainer.toString() + "\"\n"
        sml += "\t\tbackground: \"" + app.theme.background.toString() + "\"\n"
        sml += "\t\tonBackground: \"" + app.theme.onBackground.toString() + "\"\n"
        sml += "\t\tsurface: \"" + app.theme.surface.toString() + "\"\n"
        sml += "\t\tonSurface: \"" + app.theme.onSurface.toString() + "\"\n"
        sml += "\t\tsurfaceVariant: \"" + app.theme.surfaceVariant.toString() + "\"\n"
        sml += "\t\tonSurfaceVariant: \"" + app.theme.onSurfaceVariant.toString() + "\"\n"
        sml += "\t\toutline: \"" + app.theme.outline.toString() + "\"\n"
        sml += "\t\tinverseOnSurface: \"" + app.theme.inverseOnSurface.toString() + "\"\n"
        sml += "\t\tinverseSurface: \"" + app.theme.inverseSurface.toString() + "\"\n"
        sml += "\t\tinversePrimary: \"" + app.theme.inversePrimary.toString() + "\"\n"
        sml += "\t\tsurfaceTint: \"" + app.theme.surfaceTint.toString() + "\"\n"
        sml += "\t\toutlineVariant: \"" + app.theme.outlineVariant.toString() + "\"\n"
        sml += "\t\tscrim: \"" + app.theme.scrim.toString() + "\"\n"
        sml += "\t}\n"
        sml += "}\n"
        file.writeText(sml)
    }

    fun ImportVideoFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast("/")
            val target = "$folder/videos/$filename"
            copyAssetFile(file.path, target)
            val node = TreeNode(title = mutableStateOf(filename), path = file.path, type = getNodeType(file.path))
            videosNode.children.add(node)
        }
    }

    fun ImportSoundFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast("/")
            val target = "$folder/sounds/$filename"
            copyAssetFile(file.path, target)
            val node = TreeNode(title = mutableStateOf(filename), path = file.path, type = getNodeType(file.path))
            soundsNode.children.add(node)
        }
    }

    fun ImportModelFile(path: String) {
        val filename = path.substringAfterLast("/")
        val target  = "$folder/models/$filename"
        copyAssetFile(path, target)
        val node = TreeNode(title = mutableStateOf(filename), path = path, type = getNodeType(path))
        modelsNode.children.add(node)
    }

    fun ImportTextureFile(path: String) {
        val filename = path.substringAfterLast("/")
        val target  = "$folder/textures/$filename"
        copyAssetFile(path, target)
        val node = TreeNode(title = mutableStateOf(filename), path = path, type = getNodeType(path))
        texturesNode.children.add(node)
    }

    fun LoadFile(filePath: String) {
        path = filePath
        fileName = path.substringAfterLast("/")

        CoroutineScope(Dispatchers.Main).launch {
            extension = path.substringAfterLast('.', "")
            if (extension.isEmpty()) {
                extension = when {
                    fileExists("$path.sml") -> "sml"
                    else -> {
                        println("Keine gültige Datei gefunden.")
                        return@launch // Wenn keine gültige Datei gefunden wird, breche ab
                    }
                }
                path = "$filePath.$extension"
            }
            val fileText = loadFileContent(path, "", "")
            if (extension == "sml") {
                if (fileName == "book.sml") {
                    loadElementData(Book())
                } else if (fileName == "app.sml") {
                    loadElementData(App())
                } else {
                    val result = parsePage(fileText)
                    page = result.first
                    parseError = result.second
                    if (page != null) {
                        cachedPage = page
                        isPageLoaded = true
                        loadElementData(page)
                    }
                }
            } else {
                page = Page(color = "", backgroundColor = "", title = "", padding = Padding(0, 0, 0, 0), scrollable =  "false", elements = mutableListOf())
                elementData = emptyList()
                val clsName = "at.crowdware.nocodedesigner.utils.Markdown"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
            currentFileContent = TextFieldValue(
                text = fileText,
                selection = TextRange(fileText.length)
            )

            isEditorVisible = true
        }
    }

    fun reloadPage() {
        if(extension == "sml") {
            val result = parsePage(currentFileContent.text)
            page = result.first
            parseError = result.second
            if (page != null) {
                cachedPage = page
                isPageLoaded = true
                loadElementData(page)
            }
        }
    }

    private fun loadElementData(obj: Any?) {
        when (obj) {
            is Page -> {
                elementData = listOf(mapPageToTreeNodes(page!!))
                val clsName = "at.crowdware.nocodedesigner.utils.Page"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
            is App -> {
                elementData = listOf(mapAppToTreeNode(obj!! as App))
                val clsName = "at.crowdware.nocodedesigner.utils.App"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
            is Book -> {
                elementData = listOf(mapBookToTreeNode(obj!! as Book))
                val clsName = "at.crowdware.nocodedesigner.utils.Book"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
        }
    }

    fun mapUIElementToTreeNode(uiElement: UIElement): TreeNode {
        // Create a TreeNode based on the type of the UIElement
        return when (uiElement) {
            is UIElement.TextElement -> TreeNode(
                title = mutableStateOf("Text"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.ButtonElement -> TreeNode(
                title = mutableStateOf("Button"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.ImageElement -> TreeNode(
                title = mutableStateOf("Image"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.SpacerElement -> TreeNode(
                title = mutableStateOf("Spacer"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.VideoElement -> TreeNode(
                title = mutableStateOf("Video"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.YoutubeElement -> TreeNode(
                title = mutableStateOf("Youtube"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.SoundElement -> TreeNode(
                title = mutableStateOf("Sound"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.MarkdownElement -> TreeNode(
                title = mutableStateOf("Markdown"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.RowElement -> TreeNode(
                title = mutableStateOf("Row"),
                type = NodeType.DIRECTORY,
                path = "",
                children = mutableStateListOf(
                    *uiElement.uiElements.map { mapUIElementToTreeNode(it) }.toTypedArray()
                ),
                expanded = mutableStateOf(true)
            )
            is UIElement.ColumnElement -> TreeNode(
                title = mutableStateOf("Column"),
                type = NodeType.DIRECTORY,
                path = "",
                children = mutableStateListOf(
                    *uiElement.uiElements.map { mapUIElementToTreeNode(it) }.toTypedArray()
                ),
                expanded = mutableStateOf(true)
            )
            is UIElement.SceneElement -> TreeNode(
                title = mutableStateOf("Scene"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.Zero -> TreeNode(
                title = mutableStateOf("Zero Element"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
        }
    }

    fun mapBookToTreeNode(book: Book): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("Book"),
            type = mutableStateOf("Book"),
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)
        )
        return rootNode
    }

    fun mapAppToTreeNode(app: App): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("App"),
            type = mutableStateOf("App"),
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)
        )
        return rootNode
    }

    fun mapPageToTreeNodes(page: Page): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("Page"),
            type = NodeType.DIRECTORY,
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)  // Root is expanded by default
        )
        rootNode.children.addAll(page.elements.map { mapUIElementToTreeNode(it) })

        return rootNode
    }

    fun saveFileContent() {
        if (path.isEmpty()) return
        saveFileContent(path, "", "", currentFileContent.text)
    }

    fun addPage(name: String) {
        val path = "$folder/pages/$name.sml"
        createPage(path, name)

        val newNode = TreeNode(title = mutableStateOf( "${name}.sml"), path = path, type= NodeType.SML)
        val updatedChildren = pageNode.children + newNode
        pageNode.children.clear()
        pageNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun addPart(name: String) {
        val path = "$folder/parts/$name.md"
        createPart(path)

        val newNode = TreeNode(title = mutableStateOf( "${name}.md"), path = path, type= NodeType.MD)
        val updatedChildren = partsNode.children + newNode
        partsNode.children.clear()
        partsNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun deleteItem(treeNode: TreeNode) {
        deleteFile(treeNode.path)

        if (currentTreeNode?.type == NodeType.SML) {
            val title = currentTreeNode?.title?.value

            pageNode.children.remove(currentTreeNode as TreeNode)

            if (title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }
        } else if (currentTreeNode?.type == NodeType.MD) {
            val title = currentTreeNode?.title?.value

            partsNode.children.remove(currentTreeNode as TreeNode)

            if (title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }
        } else {
            imagesNode.children.remove(currentTreeNode as TreeNode)
        }
    }

    fun renameFile(name: String) {
        val folder = currentTreeNode?.path?.substringBeforeLast("/")
        val ext = currentTreeNode?.path?.substringAfterLast(".")
        val newPath = "$folder/$name.$ext"
        renameFile(currentTreeNode?.path!!, newPath)
        currentTreeNode!!.title.value = "$name.$ext"
        currentTreeNode!!.path = newPath
    }

    fun convertToPng(inputFile: File, outputFile: File) {
        val image: BufferedImage = ImageIO.read(inputFile)
        ImageIO.write(image, "png", outputFile)
    }
}

object GlobalProjectState {
    var projectState: ProjectState? = null
}