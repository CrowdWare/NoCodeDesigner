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
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.model.TreeNode


@Composable
fun TreeNodeView(
    node: TreeNode,
    iconProvider: @Composable ((TreeNode) -> Unit)? = null, // Optional icon provider for specific use cases
    onClick: (TreeNode) -> Unit
) {
    val rotationAngle by animateFloatAsState(if (node.expanded.value) 0f else -90f) // Rotation angle for arrow

    Column {
        // Row for the node itself
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (node.children == null || node.children.isEmpty()) {
                        // Only trigger onClick if the node has no children (i.e., it's a file)
                        onClick(node)
                    } else {
                        // Toggle expanded state for directories
                        node.expanded.value = !node.expanded.value
                    }
                } // Toggle expanded state on click
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically // Vertically align icon and text
        ) {
            // Icon for expanding/collapsing child nodes
            if (node.children?.isEmpty() == false) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFFB0B0B0),
                    modifier = Modifier
                        .rotate(rotationAngle)
                        .size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp)) // Space between arrow icon and node text
            }

            // Display custom icon if provided
            iconProvider?.let {
                it(node) // Provide the node to the icon provider
                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
            }

            // Text for the node title
            Text(node.title, fontSize = 12.sp, style = MaterialTheme.typography.body1, color = MaterialTheme.colors.onSurface)
        }

        // Animated visibility for child nodes
        AnimatedVisibility(visible = node.expanded.value) {
            Column(
                modifier = Modifier.padding(start = 16.dp) // Indent child nodes
            ) {
                node.children?.forEach { child ->
                    TreeNodeView(
                        node = child,
                        iconProvider = iconProvider,
                        onClick = onClick)
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