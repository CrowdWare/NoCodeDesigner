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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.model.extensionToNodeType
import at.crowdware.nocodedesigner.ui.TreeView
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import java.awt.Cursor


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun projectStructure(currentProject: ProjectState) {
    var totalHeight by remember { mutableStateOf(0f) }
    var expanded by remember { mutableStateOf(false) }
    var treeNode by remember { mutableStateOf(TreeNode(mutableStateOf(""), NodeType.OTHER)) }
    var treeNodeOffset by remember { mutableStateOf(Offset.Zero) }
    var pointerOffset by remember { mutableStateOf(Offset.Zero) }
    var treeViewHeight by remember { mutableStateOf(0.5f) }
    var treeViewSize by remember { mutableStateOf(IntSize.Zero) }

    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
            .onGloballyPositioned { coordinates ->
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
                    val pNode = node as? TreeNode
                    if (pNode != null) {
                        if (pNode.type == NodeType.SML || pNode.type == NodeType.MD)
                            currentProject.LoadFile(pNode.path)
                    }
                },
                onNodeRightClick = { node, offset, pOffset ->
                    expanded = true
                    treeNode = (node as? TreeNode)!!
                    treeNodeOffset = offset
                    pointerOffset = pOffset
                },
                onClick = {}
            )
            if (expanded) {
                val density = LocalDensity.current
                val dpOffset = with(density) {
                    DpOffset(
                        (treeNodeOffset.x + pointerOffset.x - 40).toDp(),
                        (treeNodeOffset.y - treeViewSize.height - 60).toDp()
                    )
                }
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
                    if (treeNode.type != NodeType.DIRECTORY) {
                        if (treeNode.title.value != "home.sml" && treeNode.title.value != "app.sml" && treeNode.title.value != "home.md") {
                            DropdownMenuItem(onClick = {
                                expanded = false
                                currentProject.currentTreeNode = treeNode
                                currentProject.isRenameFileDialogVisible = true
                            }) {
                                Text(text = "Rename", fontSize = 12.sp)
                            }

                            if (treeNode.type != NodeType.SML && treeNode.type != NodeType.MD) {
                                DropdownMenuItem(onClick = {
                                    expanded = false

                                    val ext = treeNode.title.value.substringAfter(".")
                                    val type = extensionToNodeType[ext]
                                    var ins = ""
                                    when(type) {
                                        NodeType.SOUND -> {ins = "Sound { src: \"${treeNode.title.value}\" }\n"}
                                        NodeType.IMAGE -> {ins = "Image { src: \"${treeNode.title.value}\" }\n"}
                                        NodeType.VIDEO -> {ins = "Video { src: \"${treeNode.title.value}\" }\n"}
                                        else -> {}
                                    }

                                    val cursorPosition = currentProject.currentFileContent.selection.start
                                    val currentText = currentProject.currentFileContent.text
                                    val newTextValue = currentText.substring(0, cursorPosition) + ins + currentText.substring(cursorPosition)
                                    currentProject.currentFileContent = currentProject.currentFileContent.copy(
                                        text = newTextValue,
                                        selection = TextRange(cursorPosition + ins.length)
                                    )
                                    currentProject.saveFileContent()
                                    currentProject.reloadPage()
                                }) {
                                    Text(text = "Insert", fontSize = 12.sp)
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
                    } else if (treeNode.title.value == "parts") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isPartDialogVisible = true
                        }) {
                            Text(text = "New", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "images") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportImageDialogVisible = true
                        }) {
                            Text(text = "Import", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "videos") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportVideoDialogVisible = true
                        }) {
                            Text(text = "Import", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "sounds") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportSoundDialogVisible = true
                        }) {
                            Text(text = "Import", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "models") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportModelDialogVisible = true
                        }) {
                            Text(text = "Import", fontSize = 12.sp)
                        }
                    } else if (treeNode.title.value == "textures") {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            currentProject.isImportTextureDialogVisible = true
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
                .height(8.dp)
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR)))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        val dragScalingFactor = totalHeight * 0.000014f
                        treeViewHeight = (treeViewHeight + (dragAmount.y / size.height) * dragScalingFactor)
                            .coerceIn(0.1f, 0.9f)
                    }
                }
                .background(MaterialTheme.colors.onSurface.copy(alpha = 0.2f)) // Divider color
        )

        BasicText(
            text = "Page Structure",
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
                tree = currentProject.elementData,
                iconProvider = { node -> fileTreeIconProvider(node) },
                onNodeDoubleClick = { node -> },
                onNodeRightClick = { _, _, _ -> },
                onClick = { node ->
                    var clsName = ""
                    if (node.title.value == "Page") {
                        clsName = "at.crowdware.nocodedesigner.utils.Page"
                    } else if (node.title.value == "Book") {
                        clsName = "at.crowdware.nocodedesigner.utils.Book"
                    } else {
                        clsName = "at.crowdware.nocodedesigner.utils.UIElement\$${node.title.value}Element"
                    }

                    val clazz = Class.forName(clsName).kotlin
                    currentProject.actualElement = clazz
                }
            )
        }
    }
}

