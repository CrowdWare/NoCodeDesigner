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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.ui.TreeView
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import java.awt.Cursor


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun projectStructure(currentProject: ProjectState) {
    var totalHeight by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
            .onGloballyPositioned { coordinates ->
                // Capture the full height of the Column containing TreeView and Accordion
                totalHeight = coordinates.size.height.toFloat()
            }) {
        var treeViewHeight by remember { mutableStateOf(0.5f) }

        BasicText(
            text = "Project Structure",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(treeViewHeight)
                .background(MaterialTheme.colors.surface)
        ) {
            TreeView(
                tree = currentProject.treeData,
                iconProvider = { node -> fileTreeIconProvider(node) },
                onClick = { node ->
                    if (node.type == NodeType.SML)
                        currentProject.LoadFile(node.path)
                })
        }

        // Draggable Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp) // Divider thickness
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        // Update treeViewHeight based on drag
                        val dragScalingFactor = totalHeight * 0.000014f
                        treeViewHeight = (treeViewHeight + (dragAmount.y / size.height) * dragScalingFactor)
                            .coerceIn(0.1f, 0.9f)
                    }
                }
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f)) // Divider color
        )
        BasicText(
            text = "Documentation",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - treeViewHeight)
                .background(MaterialTheme.colors.surface) // Apply surface color here
        ) {
            TreeView(
                tree = currentProject.docuData,
                iconProvider = { node -> fileTreeIconProvider(node) },
                onClick = { node ->
                    openWebPage(node.path)
                }
            )
        }
    }
}