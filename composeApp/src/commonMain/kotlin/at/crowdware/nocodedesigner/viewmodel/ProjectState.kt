package at.crowdware.nocodedesigner.viewmodel

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import at.crowdware.nocodelib.App
import at.crowdware.nocodelib.Page
//import java.io.File

expect fun getNodeType(path: String): NodeType
expect fun getDisplayName(path: String): String
expect suspend fun loadFileContent(path: String, uuid: String, pid: String): String
expect fun saveFileContent(path: String, uuid: String, pid: String, content: String)
expect fun createProjectState(): ProjectState
expect fun fileExists(path: String): Boolean

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
       // private set
    var extension by mutableStateOf("")
        private set

    var isProjectStructureVisible by mutableStateOf(true)
    var isNewProjectDialogVisible by mutableStateOf(false)
    var isOpenProjectDialogVisible by mutableStateOf(false)
    var isAboutDialogOpen by  mutableStateOf(false)
    var darkMode by mutableStateOf(false)

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

    fun LoadProject(path: String = folder, uuid: String, pid: String) {
        folder = path
        projectName = getDisplayName(path)


        val projectState = GlobalProjectState.projectState
        if (projectState != null) {
            CoroutineScope(Dispatchers.Main).launch {
                projectState.loadProjectFiles(path, uuid, pid)
            }
        } else {
            println("Error: ProjectState is null. Make sure GlobalProjectState.projectState is initialized.")
        }
    }

    fun LoadFile(filePath: String) {
        path = filePath
        fileName = path.substringAfterLast("/")
        println("loadFile: $path $fileName")
        CoroutineScope(Dispatchers.Main).launch {
            extension = path.substringAfterLast('.', "")
            if (extension.isEmpty()) {
                extension = when {
                    fileExists("$path.xml") -> "xml"
                    fileExists("$path.qml") -> "qml"
                    else -> {
                        println("Keine gültige Datei gefunden.")
                        return@launch // Wenn keine gültige Datei gefunden wird, breche ab
                    }
                }
                path = "$filePath.$extension"
            }
            val fileText = loadFileContent("$path", "", "")
            currentFileContent = TextFieldValue(fileText)
        }
    }

    fun saveFileContent() {
        if (path.isEmpty()) return
        saveFileContent(path, "", "", currentFileContent.text)
    }
}

//val projectState = compositionLocalOf { ProjectState() }
val projectState = compositionLocalOf { createProjectState() }

object GlobalProjectState {
    var projectState: ProjectState? = null
}