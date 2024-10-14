package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.theme.ExtendedTheme
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodelib.*
import kotlin.reflect.KProperty

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
        val scrollState = rememberScrollState()
        val element = currentProject?.actualElement

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(end = 10.dp)
            ) {
                Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                    Column() {
                        if (element != null) {
                            element.simpleName?.let {
                                Text(
                                    text = it.substringBefore("Element"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.syntaxColor
                                )
                            }
                            element.members.forEach { member ->
                                if (member is KProperty<*>) {
                                    if (member.annotations.any { it is IgnoreForDocumentation }) {
                                        return@forEach
                                    }

                                    member.annotations.forEach { annotation ->
                                        when (annotation) {
                                            is WeightAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is HexColorAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }

                                            is PaddingAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }

                                            is MarkdownAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is IntAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is StringAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is LinkAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
@Composable
fun renderAnnotation(name: String, description: String) {
    Text(
        text = name,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ExtendedTheme.colors.attributeNameColor
    )
    val md = parseMarkdown(description)
    Text(
        text = md,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colors.onPrimary
    )
}
