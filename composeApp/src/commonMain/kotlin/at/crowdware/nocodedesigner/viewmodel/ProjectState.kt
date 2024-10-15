package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import at.crowdware.nocodedesigner.utils.App
import at.crowdware.nocodedesigner.utils.Page
import at.crowdware.nocodedesigner.utils.UIElement
import at.crowdware.nocodedesigner.utils.parsePage
import kotlinx.serialization.json.JsonNull.content
import kotlin.reflect.KClass


expect fun getNodeType(path: String): NodeType
expect suspend fun loadFileContent(path: String, uuid: String, pid: String): String
expect fun saveFileContent(path: String, uuid: String, pid: String, content: String)
expect fun createProjectState(): ProjectState
expect fun fileExists(path: String): Boolean
expect fun deleteFile(path: String)
expect fun createPage(path: String)
expect fun renameFile(pathBefore: String, pathAfter: String)
expect fun copyAssetFile(path: String, target: String)

abstract class ProjectState {
    var currentFileContent by mutableStateOf(TextFieldValue(""))
    var projectName by mutableStateOf("")
        private set
    var fileName by mutableStateOf("No file loaded")
    var folder by mutableStateOf("")
        private set
    var path by mutableStateOf("")
        private set
    var treeData by mutableStateOf<List<TreeNode>>(emptyList())
    var elementData by mutableStateOf<List<TreeNode>>(emptyList())
    var extension by mutableStateOf("")
        private set
    var isPageDialogVisible by mutableStateOf(false)
    var isRenamePageDialogVisible by mutableStateOf(false)
    var isProjectStructureVisible by mutableStateOf(true)
    var isNewProjectDialogVisible by mutableStateOf(false)
    var isOpenProjectDialogVisible by mutableStateOf(false)
    var isImportAssetDialogVisible by mutableStateOf(false)
    var isAboutDialogOpen by  mutableStateOf(false)
    var isEditorVisible by mutableStateOf(false)
    var darkMode by mutableStateOf(false)
    var currentTreeNode by mutableStateOf(null as TreeNode?)
    var isPageLoaded by mutableStateOf(false)
    var actualElement: KClass<*>? by mutableStateOf(null)
    var parseError: String? by mutableStateOf(null)

    lateinit var pageNode: TreeNode
    lateinit var assetsNode: TreeNode
    var app: App? by mutableStateOf(null)
    var page: Page? by mutableStateOf(null)
    var cachedPage: Page? by mutableStateOf(null)

    abstract suspend fun loadProjectFiles(path: String, uuid: String, pid: String)
    abstract suspend fun createProjectFiles(path: String, uuid: String, pid: String, name: String, appId:String)


    fun LoadProject(path: String = folder, uuid: String, pid: String) {
        folder = path
        projectName = path

        CoroutineScope(Dispatchers.Main).launch {
            loadProjectFiles(path, uuid, pid)
        }
    }

    fun ImportFile(path: String) {
        val filename = path.substringAfterLast("/")
        val target  = "$folder/assets/$filename"
        copyAssetFile(path, target)
        val node = TreeNode(title = mutableStateOf(filename), path = path, type = getNodeType(path))
        assetsNode.children.add(node)
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
            val result = parsePage(fileText)
            page = result.first
            parseError = result.second
            if (page != null) {
                cachedPage = page
                isPageLoaded = true
                loadElementData()
            }

            currentFileContent = TextFieldValue(
                text = fileText,
                selection = TextRange(fileText.length)
            )

            isEditorVisible = true
        }
    }

    fun reloadPage() {
        val result = parsePage(currentFileContent.text)
        page = result.first
        parseError = result.second
        if(page != null) {
            cachedPage = page
            isPageLoaded = true
            loadElementData()
        }
    }

    private fun loadElementData() {
        elementData = listOf(mapPageToTreeNodes(page!!))
        var clsName = "at.crowdware.nocodedesigner.utils.Page"
        val clazz = Class.forName(clsName).kotlin
        actualElement = clazz
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
            UIElement.Zero -> TreeNode(
                title = mutableStateOf("Zero Element"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
        }
    }

    // Function to map a Page to TreeNode as the root, with all its elements as children
    fun mapPageToTreeNodes(page: Page): TreeNode {
        // Create the root node for the Page
        val rootNode = TreeNode(
            title = mutableStateOf("Page"),
            type = NodeType.DIRECTORY,
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)  // Root is expanded by default
        )

        // Map each UIElement in the Page to a child TreeNode and add to rootNode
        rootNode.children.addAll(page.elements.map { mapUIElementToTreeNode(it) })

        return rootNode
    }

    fun saveFileContent() {
        if (path.isEmpty()) return
        saveFileContent(path, "", "", currentFileContent.text)
    }

    fun addPage(name: String) {
        val path = "$folder/pages/$name.sml"
        createPage(path)

        val newNode = TreeNode(title = mutableStateOf( "${name}.sml"), path = path, type= NodeType.SML)
        val updatedChildren = pageNode.children + newNode
        pageNode.children.clear()
        pageNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun deleteItem(treeNode: TreeNode) {
        deleteFile(treeNode.path)

        if (currentTreeNode?.type == NodeType.SML) {
            val title = currentTreeNode?.title?.value

            pageNode.children.remove(currentTreeNode as TreeNode)

            if ( title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }

        } else {
            assetsNode.children.remove(currentTreeNode as TreeNode)
        }
    }

    fun renamePage(name: String) {
        val newPath = "$folder/pages/$name.sml"
        renameFile(currentTreeNode?.path!!, newPath)
        currentTreeNode!!.title.value = "$name.sml"
        currentTreeNode!!.path = newPath
    }
}

object GlobalProjectState {
    var projectState: ProjectState? = null
}