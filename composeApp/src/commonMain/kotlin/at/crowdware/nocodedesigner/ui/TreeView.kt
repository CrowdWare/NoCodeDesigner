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

package at.crowdware.nocodedesigner.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState

@Composable
fun TreeNodeView(
    node: TreeNode,
    level: Int = 0,
    iconProvider: @Composable ((TreeNode) -> Unit)? = null,
    onClick: (TreeNode) -> Unit
) {
    val rotationAngle by animateFloatAsState(if (node.expanded.value) 0f else -90f)
    var expanded by remember { mutableStateOf(false) }
    var contextMenuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current
    val currentProject = GlobalProjectState.projectState

    Column {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(node) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when {
                                    event.type == PointerEventType.Press && event.buttons.isPrimaryPressed -> {
                                        if (node.children.isEmpty()) {
                                            onClick(node)
                                        } else {
                                            node.expanded.value = !node.expanded.value
                                        }
                                    }
                                    event.type == PointerEventType.Press && event.buttons.isSecondaryPressed -> {
                                        val position = event.changes.first().position
                                        contextMenuOffset = with(density) {
                                            DpOffset(
                                                position.x.toDp() + 5.dp,
                                                position.y.toDp() - 25.dp // Adjusted to move menu 20.dp higher
                                            )
                                        }
                                        expanded = true
                                    }
                                }
                            }
                        }
                    }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width((level * 16).dp))
                if (node.children?.isEmpty() == false) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                iconProvider?.let {
                    it(node)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    node.title.value,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
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
                offset = contextMenuOffset,
                properties = PopupProperties(focusable = true)
            ) {
                if (node.children.isEmpty()) {
                    if (node.title.value != "home.qml" && node.title.value != "app.qml") {
                        if (node.type == NodeType.QML) {
                            DropdownMenuItem(onClick = {
                                expanded = false
                                currentProject?.currentTreeNode = node
                                currentProject?.isRenamePageDialogVisible = true
                            }) {
                                Text(text = "Rename", fontSize = 12.sp)
                            }
                        }
                        DropdownMenuItem(
                            modifier = Modifier.background(color = Color.DarkGray),
                            onClick = {
                                expanded = false
                                currentProject?.currentTreeNode = node
                                currentProject?.deleteItem(node)
                            }
                        ) {
                            Text(text = "Delete", fontSize = 12.sp)
                        }
                    }
                } else if (node.title.value == "pages"){
                    DropdownMenuItem(onClick = {
                        expanded = false
                        currentProject?.isPageDialogVisible = true
                    }) {
                        Text(text = "New", fontSize = 12.sp)
                    }
                }
            }
        }

        AnimatedVisibility(visible = node.expanded.value) {
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                node.children.forEach { child ->
                    TreeNodeView(
                        node = child,
                        level = level + 1,
                        iconProvider = iconProvider,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
fun TreeView(
    tree: List<TreeNode>,
    iconProvider: @Composable ((TreeNode) -> Unit)? = null,
    onClick: (TreeNode) -> Unit
) {
    val listState = rememberLazyListState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colors.surface)
        ) {
            items(tree.size) { index ->
                TreeNodeView(
                    node = tree[index],
                    iconProvider = iconProvider,
                    onClick = onClick)
            }
        }

        // Conditionally show the scrollbar only when the content is scrollable
        if (listState.canScrollForward || listState.canScrollBackward) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            )
        }
    }
}