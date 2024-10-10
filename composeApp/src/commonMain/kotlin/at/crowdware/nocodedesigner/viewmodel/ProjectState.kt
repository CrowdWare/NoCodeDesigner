package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import at.crowdware.nocodelib.App
import at.crowdware.nocodelib.Page


expect fun getNodeType(path: String): NodeType
expect fun getDisplayName(path: String): String
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
    var docuData by mutableStateOf<List<TreeNode>>(emptyList())
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
    val focusRequester by mutableStateOf(FocusRequester())

    lateinit var pageNode: TreeNode
    lateinit var assetsNode: TreeNode
    lateinit var app: App
    lateinit var page: Page

    abstract suspend fun loadProjectFiles(path: String, uuid: String, pid: String)
    abstract suspend fun createProjectFiles(path: String, uuid: String, pid: String, name: String, appId:String)

    fun isDialogOpen(): Boolean {
        return when {
            isNewProjectDialogVisible -> true
            isAboutDialogOpen -> true
            else -> false
        }
    }

    fun CreateProject(path: String, uuid: String, pid: String) {
        val projectState = at.crowdware.nocodedesigner.viewmodel.GlobalProjectState.projectState
        if (projectState != null) {
            CoroutineScope(Dispatchers.Main).launch {
                projectState.createProjectFiles(path = path, uuid = uuid, pid = pid, name="", appId = "")
            }
        } else {
            println("Error: ProjectState is null. Make sure GlobalProjectState.projectState is initialized.")
        }
    }

    fun LoadDoku() {
        if(docuData.isNotEmpty())
            return

        val node = TreeNode(
            title = mutableStateOf( "SML"),
            path = "https://nocode.crowdware.at/sml.html",
            type = NodeType.SML,
        )
        val nodePage = TreeNode(
            title = mutableStateOf( "Page"),
            path = "https://nocode.crowdware.at/page.html",
            type = NodeType.SML,
        )
        val nodeText = TreeNode(
            title = mutableStateOf( "Text"),
            path = "https://nocode.crowdware.at/text.html",
            type = NodeType.SML,
        )
        val nodeMd = TreeNode(
            title = mutableStateOf( "Markdown"),
            path = "https://nocode.crowdware.at/markdown.html",
            type = NodeType.SML,
        )
        val nodeBasics = TreeNode(
            title = mutableStateOf( "Basics"),
            path = "https://nocode.crowdware.at/elements.html",
            type = NodeType.DIRECTORY,
            children = mutableStateListOf(nodeText, nodeMd)
        )
        docuData = docuData + listOf( node,  nodePage,  nodeBasics)
    }

    fun LoadProject(path: String = folder, uuid: String, pid: String) {
        folder = path
        projectName = getDisplayName(path)

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
            println("loading: $path")
            val fileText = loadFileContent("$path", "", "")
            currentFileContent = TextFieldValue(fileText)
            isEditorVisible = true
        }
    }

    fun saveFileContent() {
        if (path.isEmpty()) return
        saveFileContent(path, "", "", currentFileContent.text)
    }

    fun addPage(name: String) {
        val path = "$folder/pages/$name.sml"
        createPage(path)

        val newNode = TreeNode(title = mutableStateOf("${name}.sml"), path, NodeType.SML)
        val updatedChildren = pageNode.children + newNode
        pageNode.children.clear()
        pageNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun deleteItem(treeNode: TreeNode) {
        deleteFile(treeNode.path)

        if (currentTreeNode?.type == NodeType.SML) {
            val title = currentTreeNode?.title?.value

            pageNode.children.remove(currentTreeNode)

            if ( title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }

        } else {
            assetsNode.children.remove(currentTreeNode)
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