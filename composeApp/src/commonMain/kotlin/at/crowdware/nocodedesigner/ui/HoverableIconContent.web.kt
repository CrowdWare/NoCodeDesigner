/*package at.crowdware.nocodedesigner.ui

import androidx.compose.runtime.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.events.SyntheticMouseEvent
import org.jetbrains.compose.web.events.SyntheticPointerEvent

@Composable
actual fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit
) {
    val lightenedBackgroundColor = LightenColor(MaterialTheme.colors.primary, 0.1f)

    Div(
        attrs = {
            style {
                width(48.px)
                height(48.px)
                backgroundColor(if (isHovered || isSelected) ExtendedTheme.colors.customAccentColor else Color.white)
                textAlign("center")
            }

            // Hover event handling
            onPointerEnter {
                onHoverChange(true)
            }

            onPointerLeave {
                onHoverChange(false)
            }

            onClick {
                onClick()
            }
        }
    ) {
        if (isHovered) {
            Div(
                attrs = {
                    style {
                        position(Position.Relative)
                        top((-30).px)
                        left(10.px)
                        backgroundColor(lightenedBackgroundColor)
                        padding(4.px)
                        borderRadius(4.px)
                    }
                }
            ) {
                Text(tooltipText)
            }
        }
    }
}*/