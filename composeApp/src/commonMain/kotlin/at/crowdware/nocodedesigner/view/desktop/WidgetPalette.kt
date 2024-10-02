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
 *  IABuilder is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IABuilder.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.SmartButton
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.ui.DraggableIcon
import at.crowdware.nocodedesigner.viewmodel.ProjectState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun widgetPalette(currentProject: ProjectState?) {
    var totalHeight by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
            .onGloballyPositioned { coordinates ->
                // Capture the full height of the Column containing TreeView and Accordion
                totalHeight = coordinates.size.height.toFloat()
            }) {
        var treeViewHeight by remember { mutableStateOf(0.5f) }

        BasicText(
            text = "Widget Palette",
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
            LazyColumn(modifier = Modifier/*.width(320.dp)*/.background(color = MaterialTheme.colors.surface)) {
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                /*.pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true // Trigger hover
                                        false
                                    },
                                    onExit = {
                                        isHovered = false // Remove hover
                                        false
                                    }
                                )*/,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Container",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.CheckBox,
                                        label = "Row",
                                        xml = "<row>\n\n</row>\n"
                                    )

                                    DraggableIcon(
                                        imageVector = Icons.Outlined.TextFields,
                                        label = "Column",
                                        xml = "<column>\n\n</column>\n"
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                /*.pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true // Trigger hover
                                        false
                                    },
                                    onExit = {
                                        isHovered = false // Remove hover
                                        false
                                    }
                                )*/,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Basic Widgets",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.CheckBox,
                                        label = "Label",
                                        xml = "<text>lorem ipsum dolor</text>\n"
                                    )
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.CheckBox,
                                        label = "Markdown",
                                        xml = "<markdown>lorem ipsum dolor</markdown>\n"
                                    )
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.TextFields,
                                        label = "Spacer",
                                        xml = "<spacer height=\"8\"/>\n"
                                    )
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.TextFields,
                                        label = "Image",
                                        xml = "<image src=\"image.png\"/>\n"
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                /*.pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true // Trigger hover
                                        false
                                    },
                                    onExit = {
                                        isHovered = false // Remove hover
                                        false
                                    }
                                )*/,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Input Widgets",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.TextFields,
                                        label = "Textfield",
                                        xml = "<textfield label=\"Email\" placeholder=\"name@example.com\" value=\"\"/>\n"
                                    )
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.CheckBox,
                                        label = "Checkbox",
                                        xml = "<checkbox checked=\"false\"/>\n"
                                    )
                                    DraggableIcon(
                                        imageVector = Icons.Outlined.SmartButton,
                                        label = "Button",
                                        xml = "<button label=\"click me\" link=\"page2\"/>\n"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}