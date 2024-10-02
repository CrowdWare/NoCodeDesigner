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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.utils.Collisions
import at.crowdware.nocodedesigner.utils.uiStates
import at.crowdware.nocodedesigner.viewmodel.GlobalProjectState
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodedesigner.viewmodel.projectState


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DraggableIcon(
    imageVector: ImageVector,
    label: String,
    xml: String
) {
    val currentState = uiStates.current
    val currentProject: ProjectState = GlobalProjectState.projectState!!
    var localOffset by remember { mutableStateOf(Offset.Zero)}
    var dragShadow by remember { mutableStateOf(1f) }
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isDarkTheme = MaterialTheme.colors.isLight.not() // Detect if dark mode is enabled
    val iconTint = if (isHovered) {
        if (isDarkTheme) {
            MaterialTheme.colors.onSurface.copy(alpha = 0.8f) // Brighten in dark mode when hovered
        } else {
            MaterialTheme.colors.onSurface.copy(alpha = 0.6f) // Darken in light mode when hovered
        }
    } else {
        if (isDarkTheme) {
            MaterialTheme.colors.onSurface.copy(alpha = 0.6f) // Default dark mode
        } else {
            MaterialTheme.colors.onSurface // Default light mode
        }
    }

    // Box background and border color based on hover and theme
    val backgroundColor = if (isHovered) {
        if (isDarkTheme) MaterialTheme.colors.surface.copy(alpha = 0.1f) // Slightly lighter surface color in dark mode
        else MaterialTheme.colors.surface.copy(alpha = 0.2f) // Slightly darker surface color in light mode
    } else {
        Color.Transparent // Default background (no fill)
    }

    val borderColor = if (isHovered) {
        if (isDarkTheme) MaterialTheme.colors.onSurface.copy(alpha = 0.4f) // Lighter border in dark mode
        else MaterialTheme.colors.onSurface.copy(alpha = 0.7f) // Darker border in light mode
    } else {
        if (isDarkTheme) MaterialTheme.colors.onSurface.copy(alpha = 0.2f) // Subtle border in dark mode
        else MaterialTheme.colors.onSurface.copy(alpha = 0.4f) // Subtle border in light mode
    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(95.dp)
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp)  // Padding inside the border
            .hoverable(interactionSource = interactionSource) // Detect hover state
            .onGloballyPositioned {
                currentState.objectLocalPosition = it.localToWindow(Offset.Zero)
            }
            /*.pointerMoveHandler(
                onEnter = {
                    isHovered = true // Trigger hover effect when the pointer enters the Box
                    false
                },
                onExit = {
                    isHovered = false // Reset hover effect when the pointer exits the Box
                    false
                }
            )*/
            .pointerInput(Unit) {
                val collisions = Collisions()
                detectDragGestures(
                    onDragStart = {
                        dragShadow = 0.2f
                    },
                    onDrag = { _, dragAmount ->
                        localOffset += dragAmount
                        var pos = Offset(
                            currentState.objectLocalPosition.x + localOffset.x,
                            currentState.objectLocalPosition.y + localOffset.y
                        )

                        currentState.hasCollided.value =
                            collisions.detect(pos, currentState.targetLocalPosition, currentState.targetSize)
                    },
                    onDragCancel = {

                    },
                    onDragEnd = {
                        var pos = Offset(
                            currentState.objectLocalPosition.x + localOffset.x,
                            currentState.objectLocalPosition.y + localOffset.y
                        )
                        currentState.hasCollided.value =
                            collisions.detect(pos, currentState.targetLocalPosition, currentState.targetSize)
                        if (currentState.hasCollided.value) {

                            val cursorPosition =
                                currentProject.currentFileContent.selection.start  // Get current cursor position
                            val currentText = currentProject.currentFileContent.text

                            // Insert new text at the cursor position
                            val newTextValue =
                                currentText.substring(0, cursorPosition) + xml + currentText.substring(
                                    cursorPosition
                                )
                            // Update the TextFieldValue with new text and move the cursor after the inserted text
                            currentProject.currentFileContent = currentProject.currentFileContent.copy(
                                text = newTextValue,
                                selection = TextRange(cursorPosition + xml.length)  // Move cursor to after the inserted text
                            )
                            currentProject.saveFileContent()
                        }
                        currentState.hasCollided.value = false
                        dragShadow = 1f
                        localOffset = Offset.Zero
                    })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, // Center the icon and text horizontally
            verticalArrangement = Arrangement.Center,           // Center vertically
            modifier = Modifier.padding(8.dp)  // Optional padding inside the column
        ) {
            // Display the icon
            Icon(
                imageVector = imageVector,
                contentDescription = "",
                modifier = Modifier
                    .size(48.dp)
                    .alpha(dragShadow),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(4.dp)) // Spacer to add space between icon and text

            // Display the text below the icon
            Text(modifier = Modifier.alpha(dragShadow),
                text = label,        // The text you want to display below the icon
                fontSize = 12.sp,         // Adjust the font size
                color = MaterialTheme.colors.onSurface,    // Adjust the text color
                style = MaterialTheme.typography.body1
            )
        }
    }
}