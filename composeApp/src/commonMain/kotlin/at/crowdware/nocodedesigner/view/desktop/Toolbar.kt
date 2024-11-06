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

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddBusiness
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.ui.HoverableIcon
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.*
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.brands.Github
import compose.icons.fontawesomeicons.solid.FolderOpen
import compose.icons.fontawesomeicons.solid.Palette
import compose.icons.fontawesomeicons.solid.PlusCircle
import androidx.compose.ui.res.painterResource

@Composable
fun toolbar(currentProject: ProjectState?) {
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
        currentProject?.isProjectStructureVisible?.let {
            HoverableIcon(
                onClick = { currentProject.isProjectStructureVisible = false },
                painter = painterResource("drawable/library.xml"),
                tooltipText = "Widget Palette",
                isSelected = !it
            )
        }
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
        }
        if (currentProject != null) {
            if (currentProject.app != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = { currentProject?.isCreateAPKVisible = true },
                    painter = painterResource("drawable/android.xml"),
                    tooltipText = "Create APK",
                    isSelected = currentProject?.isCreateAPKVisible == true
                )
            }
        }
        if (currentProject != null) {
            if (currentProject.app != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = { currentProject.isCreateHTMLVisible = true },
                    painter = painterResource("drawable/html.xml"),
                    tooltipText = "Create HTML",
                    isSelected = currentProject.isCreateHTMLVisible == true
                )
            }
        }
        if (currentProject != null) {
            Spacer(modifier = Modifier.height(8.dp))
            HoverableIcon(
                onClick = { currentProject.isSettingsVisible = true },
                painter = painterResource("drawable/settings.xml"),
                tooltipText = "Settings",
                isSelected = currentProject.isSettingsVisible == true
            )
        }
    }
}