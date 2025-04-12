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

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.texteditor.state.TextEditorState
import at.crowdware.nocode.texteditor.state.rememberTextEditorState
import at.crowdware.nocode.view.desktop.*
import at.crowdware.nocode.viewmodel.GlobalProjectState

/*
@Composable
fun fileTreeIconProvider(node: TreeNode) {
    when (node.type) { // Assuming you have a `type` field in TreeNode to determine the type
        NodeType.DIRECTORY -> Icon(Icons.Default.Folder, modifier = Modifier.size(16.dp), contentDescription = null, tint = ExtendedTheme.colors.directoryColor)
        NodeType.IMAGE -> Icon(Icons.Default.Image, modifier = Modifier.size(16.dp), contentDescription = null, tint = ExtendedTheme.colors.imageColor)
        NodeType.VIDEO -> Icon(Icons.Default.Movie, modifier = Modifier.size(16.dp), contentDescription = null, tint = ExtendedTheme.colors.videoColor)
        NodeType.SOUND -> Icon(Icons.Default.MusicNote, modifier = Modifier.size(16.dp), contentDescription = null, tint = ExtendedTheme.colors.soundColor)
        NodeType.XML -> Icon(Icons.Default.InsertDriveFile, modifier = Modifier.size(16.dp), contentDescription = null, tint = ExtendedTheme.colors.xmlColor)
        else -> Icon(Icons.Default.InsertDriveFile, modifier = Modifier.size(16.dp), contentDescription = null, tint = MaterialTheme.colors.onSurface) // Default file icon
    }
}
*/

@Composable
fun desktop() {
    val currentProject = GlobalProjectState.projectState
    val state: TextEditorState = rememberTextEditorState(AnnotatedString(""))

    LaunchedEffect(currentProject?.currentFileContent) {
        currentProject?.currentFileContent?.let { state.setText(it.text) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = MaterialTheme.colors.primary)
    ) {
        toolbar(currentProject)
        if (currentProject?.isProjectStructureVisible == true || currentProject?.extension == "md")
            projectStructure(currentProject)
        else
            widgetPalette(currentProject, state)
        syntaxEditor(currentProject, state = state)
        if (currentProject?.isPortrait == true) {
            mobilePreview(currentProject)
            propertyPanel(Modifier.width(320.dp), currentProject)
        } else if (currentProject?.isPortrait == false) {
            Column() {
                desktopPreview(currentProject)
                propertyPanel(Modifier.width(960.dp),currentProject)
            }
        }
    }
}