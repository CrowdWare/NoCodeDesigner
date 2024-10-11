package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodelib.HexColorAnnotation
import at.crowdware.nocodelib.IgnoreForDocumentation
import at.crowdware.nocodelib.MarkdownAnnotation
import at.crowdware.nocodelib.PaddingAnnotation
import kotlin.reflect.KProperty
import kotlin.reflect.KClass

@Composable
fun propertyPanel(currentProject: ProjectState?) {
    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
    ) {
        BasicText(
            text = "Properties",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
        ) {
            val element = currentProject?.actualElement
            Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                Column() {
                    if (element != null) {
                        Text(
                            text = element.simpleName.substringBefore("Element"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onPrimary
                        )
                        element.fields.forEach { field ->
                            // TODO: find the properties
                            field.annotations.forEach { annotation ->
                                when (annotation) {
                                    is HexColorAnnotation -> {
                                        println("Property: Description: ${annotation.description}")
                                    }

                                    is PaddingAnnotation -> {
                                        println("Property: Description: ${annotation.description}")
                                    }

                                    is MarkdownAnnotation -> {
                                        println("Property:  Description: ${annotation.description}")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}