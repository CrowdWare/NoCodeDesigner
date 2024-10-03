package at.crowdware.nocodedesigner.ui

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.input.pointer.pointerMoveFilter


@OptIn(ExperimentalComposeUiApi::class)
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
}

@Composable
expect fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit
)


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
    return lerp(color, Color.White, lightenFactor)
}
