/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.plugin.PluginManager
import at.crowdware.nocode.theme.AppTheme
import at.crowdware.nocode.ui.HoverableIcon
import at.crowdware.nocode.utils.*
import at.crowdware.nocode.viewmodel.ProjectState
import java.io.File

@Composable
fun toolbar(currentProject: ProjectState?) {
    val app = currentProject?.app
    Column(
        modifier = Modifier.width(52.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            text = "Build",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        HoverableIcon(
            onClick = { currentProject?.isNewProjectDialogVisible = true },
            painter = painterResource("drawable/create_folder.xml"),
            tooltipText = "Create new Project",
            isSelected = currentProject?.isNewProjectDialogVisible == true
        )
        HoverableIcon(
            onClick = { currentProject?.isOpenProjectDialogVisible = true },
            painter = painterResource("drawable/open_file.xml"),
            tooltipText = "Open Project",
            isSelected = currentProject?.isOpenProjectDialogVisible == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        HoverableIcon(
            onClick = { currentProject?.isProjectStructureVisible = true },
            painter = painterResource("drawable/tree.xml"),
            tooltipText = "Project Structure",
            isSelected = currentProject?.isProjectStructureVisible == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        /*
        if(currentProject?.extension == "sml" && currentProject.fileName != "app.sml" && currentProject.fileName != "ebook.sml") {
            currentProject.isProjectStructureVisible.let {
                HoverableIcon(
                    onClick = { currentProject.isProjectStructureVisible = false },
                    painter = painterResource("drawable/library.xml"),
                    tooltipText = "Widget Palette",
                    isSelected = !it
                )
            }
        }*/

        /*
        if (currentProject != null) {
            if (currentProject.book != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = { currentProject.isCreateEbookVisible = true },
                    painter = painterResource("drawable/book.xml"),
                    tooltipText = "Create Ebook",
                    isSelected = currentProject.isCreateEbookVisible == true
                )
            }
        }*/

    /*
        if (currentProject != null) {
            if (currentProject.site != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = { currentProject.isCreateHTMLVisible = true },
                    painter = painterResource("drawable/html.xml"),
                    tooltipText = "Create HTML",
                    isSelected = currentProject.isCreateHTMLVisible == true
                )
            }
        }
        */
/*
        if (currentProject != null) {
            println("Test: ${currentProject.app}")
            if (currentProject.site != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = { currentProject.isCreateCourseVisible = true },
                    painter = painterResource("drawable/course.xml"),
                    tooltipText = "Create Course",
                    isSelected = currentProject.isCreateCourseVisible == true
                )
            }
        }
*/
        // here we dynamically list all plugins installed, and execute them on click
        PluginManager.all().forEach { plugin ->
            val iconPath = ".plugin-cache/${plugin.id}/${plugin.icon}"
            val iconPainter = if (plugin.icon != null && File(iconPath).exists()) {
                try {
                    loadSvgResource(iconPath)
                } catch (e: Exception) {
                    painterResource("drawable/course.xml")
                }
            } else {
                painterResource("drawable/course.xml")
            }
            Spacer(modifier = Modifier.height(8.dp))
            HoverableIcon(
                onClick = {
                    val outputDir = File("export/${plugin.id}")
                    outputDir.mkdirs()
                    val source = currentProject?.folder
                    val languages = listOf("de", "en", "pt", "fr", "eo", "es")
                    val pages = mutableListOf<Page>()
                    val parts = mutableListOf<PartElement>()
                    for (lang in languages) {
                        val sourceDir = File(source, "pages-$lang")
                        sourceDir.walkTopDown().forEach { file ->
                            if (file.isFile) {
                                val pageContent = file.readText()
                                val page = parsePage(pageContent, lang)
                                if (page.first != null) {
                                    page.first!!.language = lang
                                    pages.add(page.first!!)
                                }
                            }
                        }
                    }
                    val result = plugin.export(app!!, pages, parts, outputDir)
                    println("▶️ Export mit Plugin ${plugin.label}: ${result.message} to ${outputDir.absolutePath}")
                },
                painter = iconPainter!!,
                tooltipText = plugin.label,
                isSelected = false
            )
        }
    }
}