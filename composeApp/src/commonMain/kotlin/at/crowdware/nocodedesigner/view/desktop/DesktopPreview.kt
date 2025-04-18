package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocode.ui.HoverableIcon
import at.crowdware.nocode.ui.TooltipPosition
import at.crowdware.nocode.utils.*
import at.crowdware.nocode.view.desktop.*
import at.crowdware.nocode.viewmodel.ProjectState

@Composable
fun desktopPreview(currentProject: ProjectState?) {
    var node: SmlNode? = if (currentProject?.isPageLoaded == true) currentProject.parsedPage else null
    val scrollState = rememberScrollState()
    val lang = currentProject?.lang

    if (node == null && currentProject != null) {
        // in case of syntax error we keep showing the last page
        node = currentProject.cachedPage
    }

    Column(modifier = Modifier.width(960.dp).height(560.dp).background(color = MaterialTheme.colors.primary)) {
        Row {
            BasicText(
                text = "Desktop Preview",
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                style = TextStyle(color = MaterialTheme.colors.onPrimary),
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(24.dp)) {
                HoverableIcon(
                    painter = painterResource("drawable/portrait.xml"),
                    onClick = { currentProject?.isPortrait = true },
                    tooltipText = "Mobile Preview",
                    isSelected = false,
                    tooltipPosition = TooltipPosition.Left
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            // Outer phone box with fixed aspect ratio (16:9) that scales dynamically
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(960.dp, 540.dp )
                    .aspectRatio(16f/9f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF353739))
                    .border(2.dp, Color.Gray, RoundedCornerShape(15.dp))
            ) {
                // Inner screen with relative size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .fillMaxHeight(0.96f)
                        .align(Alignment.Center)
                        .background(Color.Black)
                ) {
                    val density = LocalDensity.current
                    val fontScale = LocalDensity.current.fontScale

                    val scale = 0.8f
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density = density.density * scale,
                            fontScale = fontScale
                        ),
                    ) {
                        if (node != null && node.children.isNotEmpty() && currentProject?.extension == "sml") {
                            val pageBackgroundColor = hexToColor(getStringValue(node, "background", "background"))
                            Box(
                                modifier = Modifier
                                    .size((1.0 / scale * 960.0).dp, (1.0 / scale * 540).dp)
                                    .background(pageBackgroundColor)

                            ) {
                                var modifier = Modifier as Modifier
                                val scrollableProperty = node.properties["scrollable"] as? PropertyValue.StringValue
                                if (scrollableProperty?.value == "true") {
                                    modifier = modifier.verticalScroll(scrollState)
                                }
                                val padding = getPadding(node)
                                Column(
                                    modifier = modifier
                                        .padding(
                                            start = padding.left.dp,
                                            top = padding.top.dp,
                                            bottom = padding.bottom.dp,
                                            end = padding.right.dp
                                        )
                                        .fillMaxSize()
                                        .background(color = pageBackgroundColor)
                                ) {
                                    RenderPage(node, lang!!,"", currentProject)
                                }
                            }
                        } else if (currentProject != null && currentProject.extension == "md") {
                            // markdown here
                            val md = MarkdownElement(
                                text = currentProject.currentFileContent.text,
                                part = "",
                                color = "#000000",
                                14.sp,
                                FontWeight.Normal,
                                TextAlign.Left, 0, 0, 0
                            )
                            Box(
                                modifier = Modifier
                                    .size((1.0 / scale * 960.0).dp, (1.0 / scale * 540).dp)
                                    .background(hexToColor("#F6F6F6"))

                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .background(color = hexToColor("#F6F6F6"))
                                ) {
                                    // we have to split the text to find out, where the images shall be rendered
                                    val imagePattern = Regex("!\\[([^\\]]*)\\]\\s*\\(\\s*([^\\s)]+)\\s*\"?([^\"\\)]*)\"?\\)")
                                    var currentIndex = 0
                                    val matches = imagePattern.findAll(md.text).toList()
                                    matches.forEach { match ->
                                        val startIndex = match.range.first
                                        val endIndex = match.range.last
                                        if (currentIndex < startIndex) {
                                            val textBeforeImage = md.text.substring(currentIndex, startIndex)
                                            Text(
                                                text = parseMarkdown(textBeforeImage),
                                                style = TextStyle(
                                                    color = hexToColor(
                                                        md.color,
                                                        colorNameToHex("onBackground")
                                                    )
                                                ),
                                                fontSize = md.fontSize,
                                                fontWeight = md.fontWeight,
                                                textAlign = md.textAlign
                                            )
                                        }
                                        val altText = match.groupValues[1]
                                        val imageUrl = match.groupValues[2].trim()
                                        dynamicImageFromAssets(
                                            modifier = Modifier,
                                            imageUrl,
                                            "fit",
                                            "",
                                            0,
                                            0
                                        )

                                        currentIndex = endIndex + 1
                                    }
                                    val remainingText = md.text.substring(currentIndex)
                                    Text(
                                        text = parseMarkdown(remainingText),
                                        style = TextStyle(color = hexToColor(md.color, colorNameToHex("onBackground"))),
                                        fontSize = md.fontSize,
                                        fontWeight = md.fontWeight,
                                        textAlign = md.textAlign
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