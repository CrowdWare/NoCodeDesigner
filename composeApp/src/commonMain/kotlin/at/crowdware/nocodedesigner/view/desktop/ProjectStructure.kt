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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.ui.TreeView
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import java.awt.Cursor


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun projectStructure(currentProject: ProjectState) {
    var totalHeight by remember { mutableStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    var treeNode by remember { mutableStateOf(TreeNode(mutableStateOf(""), "", NodeType.OTHER)) }
    var treeNodeOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerOffset by remember { mutableStateOf(Offset.Zero) }
    var treeViewHeight by remember { mutableStateOf(0.5f) }
    var treeViewSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
            .onGloballyPositioned { coordinates ->
                // Capture the full height of the Column containing TreeView and Accordion
                totalHeight = coordinates.size.height.toFloat()
            }) {
        BasicText(
            text = "Project Structure",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(treeViewHeight)
                .background(MaterialTheme.colors.surface)
                .onGloballyPositioned { layoutCoordinates ->
                    treeViewSize = layoutCoordinates.size
                }
        ) {

            TreeView(
                tree = currentProject.treeData,
                iconProvider = { node -> fileTreeIconProvider(node) },
                onNodeDoubleClick = { node ->
                    if (node.type == NodeType.SML)
                        currentProject.LoadFile(node.path)
                },
                onNodeRightClick = { node, offset, pOffset ->
                    expanded = true
                    treeNode = node
                    treeNodeOffset = offset
                    pointerOffset = pOffset
                },
            )
            if (expanded) {
                val density = LocalDensity.current
                val dpOffset = with(density) {
                    DpOffset((treeNodeOffset.x + pointerOffset.x - 40).toDp(), (treeNodeOffset.y - treeViewSize.height - 60).toDp())
                }
                println("$treeNodeOffset")
                DropdownMenu(
                    modifier = Modifier
                        .background(
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            border = BorderStroke(1.dp, color = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    offset = dpOffset,
                    properties = PopupProperties(focusable = true)
                ) {
                    if (treeNode.children.isEmpty()) {
                        if (treeNode.title.value != "home.sml" && treeNode.title.value != "app.sml") {
                            if (treeNode.type == NodeType.SML) {
                                DropdownMenuItem(onClick = {
                                    expanded = false
                                    currentProject.currentTreeNode = treeNode
                                    currentProject.isRenamePageDialogVisible = true
                                }) {
                                    Text(text = "Rename", fontSize = 12.sp)
                                }
                            }
                            DropdownMenuItem(
                                modifier = Modifier.background(color = Color.DarkGray),
                                onClick = {
                                    expanded = false
                                    currentProject.currentTreeNode = treeNode
                                    currentProject.deleteItem(treeNode)
                                }
                            ) {
                                Text(text = "Delete", fontSize = 12.sp)
                            }
                        }
                    } else if (treeNode.title.value == "pages") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isPageDialogVisible = true
                        }) {
                            Text(text = "New", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "assets") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportAssetDialogVisible = true
                        }) {
                            Text(text = "Import", fontSize = 12.sp)
                        }
                    }
                }
            }
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
                onNodeDoubleClick = { node ->
                    openWebPage(node.path)
                },
                onNodeRightClick = { _,_,_->

                },
            )
        }
    }
}
