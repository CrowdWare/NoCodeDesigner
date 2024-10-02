package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import at.crowdware.nocodedesigner.theme.ExtendedTheme


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HoverableIcon(
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean) {
    var isHovered by remember { mutableStateOf(false) }
    val lightenedBackgroundColor = LightenColor(MaterialTheme.colors.primary, 0.1f)

    Box(modifier = Modifier
        .size(48.dp)
        /*.pointerMoveFilter(
            onEnter = {
                isHovered = true
                false
            },
            onExit = {
                isHovered = false
                false
            }
        )*/.clickable { onClick() }
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Hoverable Icon",
            tint = if (isHovered || isSelected) ExtendedTheme.colors.customAccentColor else MaterialTheme.colors.onPrimary,
            modifier = Modifier.size(32.dp).align(Alignment.Center)
        )
        // Tooltip
        if (isHovered) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(38, 8),
                properties = PopupProperties(focusable = false)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp, 16.dp)
                            .background(lightenedBackgroundColor, TriangleShape())
                    )
                    Box(
                        modifier = Modifier
                            .background(lightenedBackgroundColor, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        BasicText(
                            text = tooltipText,
                            style = TextStyle(
                                color = MaterialTheme.colors.onSurface,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

fun TriangleShape(): Shape {
    return GenericShape { size, _ ->
        moveTo(size.width, 0f)
        lineTo(0f, size.height / 2)
        lineTo(size.width, size.height)
        close()
    }
}

@Composable
fun LightenColor(color: Color, lightenFactor: Float = 0.3f): Color {
    // Mischt die Farbe mit Weiß, um sie aufzuhellen (ohne Transparenz)
    return lerp(color, Color.White, lightenFactor) // lightenFactor bestimmt, wie stark die Farbe aufgehellt wird
}
/*
package at.crowdware.nocodedesigner.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
expect fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit
)

@Composable
fun HoverableIcon(
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean
) {
    var isHovered by remember { mutableStateOf(false) }

    HoverableIconContent(
        isHovered = isHovered,
        onClick = onClick,
        imageVector = imageVector,
        tooltipText = tooltipText,
        isSelected = isSelected,
        onHoverChange = { hover -> isHovered = hover }
    )
}*/