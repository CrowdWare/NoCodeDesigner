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

package at.crowdware.nocodedesigner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

import at.crowdware.nocodedesigner.theme.AppTheme
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.ui.*
import at.crowdware.nocodedesigner.view.desktop.desktop
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodedesigner.viewmodel.createProjectState


import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Frame
import java.awt.Window
import java.io.PrintStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch

val LocalProjectState = compositionLocalOf<ProjectState> { error("No ProjectState provided") }

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val appName = "NoCodeDesigner"
    val version = Version.version
    val loadedState = loadAppState()
    val windowState = rememberWindowState(
        width = (loadedState?.windowWidth ?: 1600).dp,
        height = (loadedState?.windowHeight ?: 800).dp
    )
    val projectState = createProjectState()
    GlobalProjectState.projectState = projectState
    projectState.darkMode = androidx.compose.foundation.isSystemInDarkTheme()


    // setup logging, all println are stored in a log file
    //setupLogging()

    // Load the native library
    val macLib = Native.load(getLibraryPath(), MacLib::class.java) as MacLib

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
                onAppClose(frame as ComposeWindow, projectState.projectName)
                quitResponse.performQuit()
            }
        }
    }

    Window(
        onCloseRequest = {},
        title = appName + " [" + loadedState?.lastProject.toString() + "]",
        transparent = true,
        undecorated = true,
        resizable = true,
        state = windowState
    ) {
        var isMaximized by remember { mutableStateOf(window.extendedState == Frame.MAXIMIZED_BOTH) }
        window.minimumSize = Dimension(770, 735)
        CompositionLocalProvider(LocalProjectState provides projectState) {
            LaunchedEffect(Unit) {
                // set new location
                window.setLocation(loadedState?.windowX ?: 100, loadedState?.windowY ?: 100)

                projectState.LoadProject(loadedState?.lastProject.toString(), "", "")
                // Listen for changes in the window's maximized state
                window.addWindowStateListener {
                    isMaximized = (window.extendedState == Frame.MAXIMIZED_BOTH)
                }
            }

            MenuBar {
                Menu("File", mnemonic = 'F') {
                    Item("Create Project", onClick = {
                        projectState.isNewProjectDialogVisible = true
                    })
                    Item("Create Page", onClick = {
                        projectState.isPageDialogVisible = true
                    })
                    Item("Import Asset", onClick = {
                        projectState.isImportAssetDialogVisible = true
                    })
                    Separator()
                    Item("Open", onClick = {
                        projectState.isOpenProjectDialogVisible = true
                    })
                }
            }
            AppTheme(darkTheme = projectState.darkMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.5.dp, Color.Gray, RoundedCornerShape(10.dp)),
                    color = Color(55, 55, 55),
                    shape = RoundedCornerShape(10.dp) //window has round corners now

                ) {
                    Column {
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
                                        onAppClose(window, projectState.folder)
                                        exitApplication()
                                    } // Close
                                    Spacer(modifier = Modifier.width(8.dp))
                                    WindowControlButton(color = Color(255, 189, 76)) {
                                        window.extendedState = Frame.ICONIFIED
                                    } // Minimize
                                    Spacer(modifier = Modifier.width(8.dp))
                                    WindowControlButton(color = Color(87, 188, 87)) {
                                        val isMaximized = window.extendedState == Frame.MAXIMIZED_BOTH
                                        window.extendedState = if (isMaximized) Frame.NORMAL else Frame.MAXIMIZED_BOTH
                                    } // Fullscreen/Restore
                                }
                                // Add the title or caption text
                                var caption = appName
                                if (!projectState.projectName.isEmpty())
                                    caption += " - " + projectState.projectName
                                Text(
                                    text = caption,
                                    color = MaterialTheme.colors.onPrimary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        desktop()
                        if (projectState.isAboutDialogOpen) {
                            AboutDialog(
                                appName = appName,
                                version = version,
                                onDismissRequest = { projectState.isAboutDialogOpen = false }
                            )
                        }
                        if(projectState.isPageDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            var pageName by remember { mutableStateOf("") }
                            pageDialog(
                                name = pageName,
                                onNameChange = { pageName = it},
                                onDismissRequest = { projectState.isPageDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isPageDialogVisible = false
                                    coroutineScope.launch {
                                        projectState.addPage(pageName)
                                    }
                                })
                        }
                        if (projectState.isRenamePageDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            val title = projectState.currentTreeNode?.title?.value?.substringBefore(".")
                            var pageName by remember { mutableStateOf(title ?: "") }
                            renamePageDialog(
                                name = pageName,
                                onNameChange = { pageName = it},
                                onDismissRequest = { projectState.isRenamePageDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isRenamePageDialogVisible = false
                                    coroutineScope.launch {
                                        projectState.renamePage(pageName)
                                    }
                                })
                        }

                        if (projectState.isNewProjectDialogVisible) {
                            val coroutineScope = rememberCoroutineScope()
                            var projectName by remember { mutableStateOf("") }
                            var appId by remember { mutableStateOf("com.sample.app") }
                            var projectFolder by remember { mutableStateOf("") }
                            projectDialog(
                                name = projectName,
                                folder = projectFolder,
                                onFolderChange = {projectFolder = it},
                                onNameChange = {projectName = it},
                                id = appId,
                                onIdChange = {appId = it},
                                onDismissRequest = { projectState.isNewProjectDialogVisible = false },
                                onCreateRequest = {
                                    projectState.isNewProjectDialogVisible = false
                                    coroutineScope.launch {
                                        projectState.createProjectFiles(projectFolder, "", "", projectName, appId)
                                    }
                                })
                        }
                        if(projectState.isOpenProjectDialogVisible) {
                            try {
                                // Call the native Swift method
                                val folderPathPointer = macLib.selectFolder(projectState.darkMode)

                                // Check if the pointer is null
                                if (folderPathPointer == null) {
                                    println("No folder selected or an error occurred in the Swift function.")
                                } else {
                                    // Convert the result from a Pointer to a String
                                    val folderPath = folderPathPointer.getString(0)
                                    println("Selected folder: $folderPath")
                                    projectState.LoadProject(folderPath, "", "")
                                }
                            } catch (e: Exception) {
                                // Catch any exceptions that happen during the JNA call
                                println("Error calling native function: ${e.message}")
                                e.printStackTrace()
                            }
                            projectState.isOpenProjectDialogVisible = false
                        }
                        if(projectState.isImportAssetDialogVisible) {
                            try {
                                // Call the native Swift method
                                val filePathPointer = macLib.selectFile(projectState.darkMode)

                                // Check if the pointer is null
                                if (filePathPointer == null) {
                                    println("No file selected or an error occurred in the Swift function.")
                                } else {
                                    // Convert the result from a Pointer to a String
                                    val filePath = filePathPointer.getString(0)
                                    projectState.ImportFile(filePath)
                                }
                            } catch (e: Exception) {
                                // Catch any exceptions that happen during the JNA call
                                println("Error calling native function: ${e.message}")
                                e.printStackTrace()
                            }
                            projectState.isImportAssetDialogVisible = false
                        }
                    }
                }
            }
        }
    }
}

fun onAppClose(frame: ComposeWindow, folder: String) {
    // Save the app state when the window is closed
    saveAppState(
        AppState(
            windowWidth = frame.width,
            windowHeight = frame.height,
            windowX = frame.x,
            windowY = frame.y,
            lastProject = folder
        )
    )
}

// Define the interface for the Swift library
interface MacLib : Library {
    fun selectFolder(darkMode: Boolean): Pointer?
    fun selectFile(darkMode: Boolean): Pointer?
}

// JNA interface to access Objective-C runtime
interface ObjCRuntime : Library {
    fun objc_getClass(className: String): Pointer
    fun sel_registerName(selectorName: String): Pointer
    fun objc_msgSend(receiver: Pointer, selector: Pointer): Pointer
}

fun isRunningInProdMode(): Boolean {
    val bp = getBundlePath()
    // in dev you get like: /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin
    // in prod you get like: /Users/art/SourceCode/NoCodeDesigner/build/compose/binaries/main/app/NoCodeDesigner.app
    return bp.contains(".app")
}

fun getBundlePath(): String {
    val objc = Native.load("objc", ObjCRuntime::class.java) as ObjCRuntime
    val nsBundleClass = objc.objc_getClass("NSBundle")
    val mainBundleSelector = objc.sel_registerName("mainBundle")
    val mainBundle = objc.objc_msgSend(nsBundleClass, mainBundleSelector)
    val bundlePathSelector = objc.sel_registerName("bundlePath")
    val bundlePathNSString = objc.objc_msgSend(mainBundle, bundlePathSelector)
    val utf8StringSelector = objc.sel_registerName("UTF8String")
    val bundlePathPointer = objc.objc_msgSend(bundlePathNSString, utf8StringSelector)

    return bundlePathPointer.getString(0)
}

fun getLibraryPath(): String {
    val home = System.getProperty("user.dir")

    return if (isRunningInProdMode()) {
        val objc = Native.load("objc", ObjCRuntime::class.java) as ObjCRuntime
        val nsBundleClass = objc.objc_getClass("NSBundle")
        val mainBundleSelector = objc.sel_registerName("mainBundle")
        val mainBundle = objc.objc_msgSend(nsBundleClass, mainBundleSelector)
        val bundlePathSelector = objc.sel_registerName("bundlePath")
        val bundlePathNSString = objc.objc_msgSend(mainBundle, bundlePathSelector)
        val utf8StringSelector = objc.sel_registerName("UTF8String")
        val bundlePathPointer = objc.objc_msgSend(bundlePathNSString, utf8StringSelector)
        val bundlePath = bundlePathPointer.getString(0)
        val prodPath = "$bundlePath/Contents/Resources/libmac.dylib"
        prodPath
    } else {
        val devPath = "$home/mac/.build/release/libmac.dylib"
        devPath
    }
}

fun setupLogging() {
    val userHome = System.getProperty("user.home")
    val configDirectory = File("$userHome/Library/Application Support/NoCodeDesigner")
    val tempFile = File("$configDirectory/NoCodeDesigner.log")
    if(!configDirectory.exists()) {
        configDirectory.mkdirs()
    }
    if (!tempFile.exists()) {
        tempFile.createNewFile()
    }

    // Redirect stdout and stderr to the file
    val logStream = PrintStream(tempFile.outputStream())
    System.setOut(logStream)
    System.setErr(logStream)

    println("Logging initialized. Writing to: ${tempFile.absolutePath}")
}

@Serializable
data class AppState(
    val windowWidth: Int,
    val windowHeight: Int,
    val windowX: Int,
    val windowY: Int,
    val lastProject: String,
)

fun saveAppState(state: AppState) {
    val userHome = System.getProperty("user.home")
    val configDirectory = File("$userHome/Library/Application Support/NoCodeDesigner")

    // Create the directory if it doesn't exist
    if (!configDirectory.exists()) {
        configDirectory.mkdirs()
    }

    val configFile = File(configDirectory, "app_state.json")
    try {
        val jsonState = Json.encodeToString(state)
        configFile.writeText(jsonState)
    } catch (e: IOException) {
        println("Error writing app state: ${e.message}")
        e.printStackTrace()
    }
}

fun loadAppState(): AppState? {
    val userHome = System.getProperty("user.home")
    val configDirectory = File("$userHome/Library/Application Support/NoCodeDesigner")
    val configFile = File(configDirectory, "app_state.json")

    if(!configDirectory.exists()) {
        configDirectory.mkdirs()
    }

    return try {
        val jsonState = configFile.readText()
        Json.decodeFromString<AppState>(jsonState)
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null if loading fails
    }
}

