/*
 * Copyright (C) 2025 CrowdWare
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

package at.crowdware.nocodedesigner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import at.crowdware.nocode.*
import at.crowdware.nocode.theme.AppTheme
import at.crowdware.nocode.theme.ExtendedTheme
import at.crowdware.nocode.ui.*
import at.crowdware.nocodedesigner.view.desktop.desktop
import at.crowdware.nocode.viewmodel.*
import at.crowdware.nocode.ui.WindowCaptionArea
import at.crowdware.nocode.ui.WindowControlButton
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Frame
import java.awt.Window
import java.io.File

val LocalProjectState = compositionLocalOf<ProjectState> { error("No ProjectState provided") }

private const val APPNAME = "NoCodeDesigner"

fun main() = application {
    println("🔥 application gestartet!")
    var appName = APPNAME
    var appTitle by mutableStateOf("")
    val version = Version.version
    val projectState = createProjectState()
    val appState = createAppState()
    GlobalProjectState.projectState = projectState
    GlobalAppState.appState = appState

    loadAppState(APPNAME)
    appState.theme = "Dark"
    appState.initLicense()

    appTitle = appName + " - " + appState.lastProject
    val windowState = rememberWindowState(
        width = (appState.windowWidth).dp,
        height = (appState.windowHeight).dp
    )
    if (appState.theme.isEmpty())
        appState.theme = if (androidx.compose.foundation.isSystemInDarkTheme()) "Dark" else "Light"
    val isWindows = System.getProperty("os.name").contains("Windows", ignoreCase = true)
    var isAskingToClose by remember { mutableStateOf(false) }

    // setup logging, all println are stored in a log file
    val isDevMode = System.getenv("DEV_MODE") == "true"
    if (!isDevMode)
        setupLogging(APPNAME)

    System.setProperty("apple.awt.application.name", appName)
    // Check if the desktop supports macOS actions (About, Quit, etc.)
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()

        // Set custom "About" handler
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler {
                projectState.isAboutDialogOpen = true
            }
        }

        // Set custom Quit handler if needed (optional)
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler { _, quitResponse ->
                var frame: Window = Frame.getWindows()[0]
                onAppClose(frame as ComposeWindow, projectState.folder, APPNAME)
                quitResponse.performQuit()
            }
        }
    }


    Window(
        onCloseRequest = { isAskingToClose = true },
        title = appName,
        transparent = !isWindows,
        undecorated = !isWindows,
        resizable = true,
        state = windowState,
        icon = painterResource("icons/WindowsIcon.ico")
    ) {

        var isMaximized by remember { mutableStateOf(window.extendedState == Frame.MAXIMIZED_BOTH) }
        window.minimumSize = Dimension(770, 735)
        CompositionLocalProvider(LocalProjectState provides projectState) {
            LaunchedEffect(appState.theme) {
                // set new location
                window.setLocation(appState.windowX ?: 100, appState.windowY ?: 100)

                projectState.LoadProject(appState.lastProject, "", "")
                // Listen for changes in the window's maximized state
                window.addWindowStateListener {
                    isMaximized = (window.extendedState == Frame.MAXIMIZED_BOTH)
                }
            }

            AppTheme(darkTheme = appState.theme == "Dark") {
                var shape = RectangleShape
                var borderShape = RectangleShape

                if (!isWindows) {
                    shape = RoundedCornerShape(10.dp)
                    borderShape = RoundedCornerShape(10.dp)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.5.dp, Color.Gray, borderShape),
                    color = Color(55, 55, 55),
                    shape = shape

                ) {
                    // used on Windows only, no close button on MacOS
                    if (isAskingToClose) {
                        onAppClose(window, projectState.folder, APPNAME)
                        exitApplication()
                    }
                    Column {
                        if (!isWindows) {
                            WindowCaptionArea {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(ExtendedTheme.colors.captionColor)
                                        .padding(start = 12.dp, top = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxHeight(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mac-style window controls (close, minimize, fullscreen)
                                        WindowControlButton(color = Color(255, 92, 92)) {
                                            onAppClose(window, projectState.folder, APPNAME)
                                            exitApplication()
                                        } // Close
                                        Spacer(modifier = Modifier.width(8.dp))
                                        WindowControlButton(color = Color(255, 189, 76)) {
                                            window.extendedState = Frame.ICONIFIED
                                        } // Minimize
                                        Spacer(modifier = Modifier.width(8.dp))
                                        WindowControlButton(color = Color(87, 188, 87)) {
                                            val isMaximized = window.extendedState == Frame.MAXIMIZED_BOTH
                                            window.extendedState =
                                                if (isMaximized) Frame.NORMAL else Frame.MAXIMIZED_BOTH
                                        } // Fullscreen/Restore
                                    }
                                    Text(
                                        text = appTitle,
                                        color = MaterialTheme.colors.onPrimary,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        desktop()

                        if (projectState.isAboutDialogOpen) {
                            aboutDialog(
                                appName = appName,
                                version = version,
                                onDismissRequest = { projectState.isAboutDialogOpen = false }
                            )
                        }
                        if (projectState.isPageDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            var pageName by remember { mutableStateOf(TextFieldValue("")) }
                            pageDialog(
                                name = pageName,
                                onNameChange = { pageName = it },
                                onDismissRequest = { projectState.isPageDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isPageDialogVisible = false
                                    projectState.currentTreeNode?.title
                                    coroutineScope.launch {
                                        projectState.addPage(pageName.text, projectState.currentTreeNode)
                                    }
                                })
                        }
                        if (projectState.isPartDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            var partName by remember { mutableStateOf(TextFieldValue("")) }
                            partDialog(
                                name = partName,
                                onNameChange = { partName = it },
                                onDismissRequest = { projectState.isPartDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isPartDialogVisible = false
                                    coroutineScope.launch {
                                        projectState.addPart(partName.text, projectState.currentTreeNode)
                                    }
                                })
                        }
                        if (projectState.isRenameFileDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            val title = projectState.currentTreeNode?.title?.value?.substringBefore(".")
                            var fileName by rememberSaveable { mutableStateOf(TextFieldValue(title ?: "")) }
                            renameFileDialog(
                                name = fileName,
                                onNameChange = { fileName = it },
                                onDismissRequest = { projectState.isRenameFileDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isRenameFileDialogVisible = false
                                    coroutineScope.launch {
                                        projectState.renameFile(fileName.text)
                                    }
                                })
                        }

                        if (projectState.isSettingsVisible) {
                            var license by remember { mutableStateOf(TextFieldValue(appState.license)) }
                            settingsDialog(
                                onDismissRequest = { projectState.isSettingsVisible = false },
                                license = license,
                                onLicenseChange = { license = it },
                                onCreateRequest = {
                                    projectState.isSettingsVisible = false
                                    appState.license = license.text
                                    appState.initLicense()
                                    saveState(window, projectState.folder, APPNAME)
                                })
                        }

                        if (projectState.isNewProjectDialogVisible) {
                            val folder = System.getProperty("user.home") + "/Ebooks"
                            val coroutineScope = rememberCoroutineScope()
                            var projectName by remember { mutableStateOf(TextFieldValue("")) }
                            var appId by remember { mutableStateOf(TextFieldValue("com.sample.app")) }
                            var projectFolder by remember { mutableStateOf(TextFieldValue(folder)) }
                            var theme by remember { mutableStateOf("Light") }
                            var createBook by remember { mutableStateOf(false) }
                            var createApp by remember { mutableStateOf(false) }

                            createProjectDialog(
                                name = projectName,
                                folder = projectFolder,
                                onFolderChange = { projectFolder = it },
                                onNameChange = { projectName = it },
                                id = appId,
                                app = createApp,
                                book = createBook,
                                lang = "de,en,pt,fr,es,eo",
                                onIdChange = { appId = it },
                                onDismissRequest = { projectState.isNewProjectDialogVisible = false },
                                theme = theme,
                                onThemeChanged = { theme = it },
                                onCheckBookChanged = { createBook = it },
                                onCheckAppChanged = { createApp = it },
                                onCreateRequest = { langs->
                                    projectState.isNewProjectDialogVisible = false
                                    coroutineScope.launch {
                                        var folder = projectFolder.text
                                        if (!projectFolder.text.endsWith(File.separator))
                                            folder = projectFolder.text + File.separator
                                        appTitle = appName + " - " + folder + projectName.text
                                        projectState.createProjectFiles(
                                            folder,
                                            "",
                                            "",
                                            projectName.text,
                                            appId.text,
                                            theme,
                                            createBook,
                                            createApp,
                                            langs
                                        )
                                    }
                                })
                        }

                        if (projectState.isCreateEbookVisible) {
                            val bookName = projectState.book?.name!!
                            val coroutineScope = rememberCoroutineScope()
                            var title by remember { mutableStateOf(TextFieldValue(bookName)) }

                            // reload the book to have newly added properties
                            projectState.loadBook()
                            var deployDir = projectState.book?.deployDirEpub!!
                            if (deployDir.isEmpty()) {
                                deployDir = System.getProperty("user.home") + "/" + APPNAME
                            }
                            var lang = projectState.book?.language!!
                            var folder by remember { mutableStateOf(TextFieldValue(deployDir)) }

                            createEbookDialog(
                                name = title,
                                folder = folder,
                                lang = lang,
                                onFolderChange = { folder = it },
                                onNameChange = { title = it },
                                onDismissRequest = { projectState.isCreateEbookVisible = false },
                                onCreateRequest = { langs ->
                                    projectState.isCreateEbookVisible = false
                                    coroutineScope.launch {
                                        var f = folder.text
                                        if (!folder.text.endsWith(File.separator))
                                            f += File.separator
                                        projectState.createEbook(title.text, f, langs)
                                    }
                                })
                        }

                        if (projectState.isCreateHTMLVisible) {
                            val appName = projectState.app?.name!!
                            var deploymentDir = projectState.app?.deployDirHtml
                            if (deploymentDir == null || deploymentDir.isEmpty()) {
                                deploymentDir = System.getProperty("user.home") + "/" + APPNAME
                            }
                            val coroutineScope = rememberCoroutineScope()
                            var folder by remember { mutableStateOf(TextFieldValue(deploymentDir!!)) }
                            createHTMLDialog(
                                folder = folder,
                                onFolderChange = { folder = it },
                                onDismissRequest = { projectState.isCreateHTMLVisible = false },
                                onCreateRequest = {
                                    projectState.isCreateHTMLVisible = false
                                    coroutineScope.launch {
                                        var f = folder.text
                                        if (!folder.text.endsWith(File.separator))
                                            f += File.separator
                                        projectState.createHTML(f)
                                    }
                                })
                        }

                        if (projectState.isCreateCourseVisible) {
                            val appName = projectState.app?.name!!
                            var deploymentDir = projectState.app?.deployDirHtml
                            if (deploymentDir == null || deploymentDir.isEmpty()) {
                                deploymentDir = System.getProperty("user.home") + "/" + APPNAME
                            }
                            val coroutineScope = rememberCoroutineScope()
                            var folder by remember { mutableStateOf(TextFieldValue(deploymentDir)) }
                            createCourseDialog(
                                folder = folder,
                                onFolderChange = { folder = it },
                                onDismissRequest = { projectState.isCreateCourseVisible = false },
                                onCreateRequest = {
                                    projectState.isCreateCourseVisible = false
                                    coroutineScope.launch {
                                        var f = folder.text
                                        if (!folder.text.endsWith(File.separator))
                                            f += File.separator
                                        projectState.createCourse(f, "de")  // TODO: set language via dialog
                                    }
                                })
                        }

                        DirectoryPicker(
                            show = projectState.isOpenProjectDialogVisible,
                            title = "Pick a project folder to be opened"
                        ) { path ->
                            projectState.isOpenProjectDialogVisible = false
                            if (path != null) {
                                saveState(window, path, APPNAME)
                                appTitle = "$appName - $path"
                                projectState.LoadProject(path, "", "")
                            }
                        }
                        MultipleFilePicker(
                            show = projectState.isImportImageDialogVisible,
                            title = "Pick one or more image files to import",
                            fileExtensions = listOf("png", "jpg", "jpeg", "webp", "gif", "bmp")
                        ) { platformFile ->
                            projectState.isImportImageDialogVisible = false
                            if (platformFile != null)
                                projectState.ImportImageFile(platformFile)
                        }
                        MultipleFilePicker(
                            show = projectState.isImportVideoDialogVisible,
                            title = "Pick one or more video files to import",
                            fileExtensions = listOf("mp4", "mkv", "webm", "avi", "mov", "flv", "ts", "3gp", "m4v")
                        ) { platformFile ->
                            projectState.isImportVideoDialogVisible = false
                            if (platformFile != null)
                                projectState.ImportVideoFile(platformFile)
                        }
                        MultipleFilePicker(
                            show = projectState.isImportSoundDialogVisible,
                            title = "Pick one or more sound files to import",
                            fileExtensions = listOf("wav", "mp3", "ogg", "flac", "aac", "amr", "opus", "midi")
                        ) { platformFile ->
                            projectState.isImportSoundDialogVisible = false
                            if (platformFile != null)
                                projectState.ImportSoundFile(platformFile)
                        }
                        FilePicker(
                            show = projectState.isImportModelDialogVisible,
                            title = "Pick a model file to import",
                            fileExtensions = listOf("glb", "gltf", "ktx", "bin")
                        ) { platformFile ->
                            projectState.isImportModelDialogVisible = false
                            if (platformFile != null)
                                projectState.ImportModelFile(platformFile.path)
                        }
                        FilePicker(
                            show = projectState.isImportTextureDialogVisible,
                            title = "Pick a texture file to import",
                            fileExtensions = listOf("\"png\", \"jpg\", \"jpeg\", \"webp\", \"gif\", \"bmp\"")
                        ) { platformFile ->
                            projectState.isImportTextureDialogVisible = false
                            if (platformFile != null)
                                projectState.ImportTextureFile(platformFile.path)
                        }
                    }
                }
            }
        }
    }
}