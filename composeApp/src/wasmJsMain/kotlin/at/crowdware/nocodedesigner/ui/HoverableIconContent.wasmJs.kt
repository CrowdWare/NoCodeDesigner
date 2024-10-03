package at.crowdware.nocodedesigner.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
actual fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit
) {
    TODO("Not yet implemented")
}