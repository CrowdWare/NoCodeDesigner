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

package at.crowdware.nocodedesigner.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodelib.Page
import at.crowdware.nocodelib.TextElement
import at.crowdware.nocodelib.ButtonElement
import at.crowdware.nocodelib.ColumnElement
import at.crowdware.nocodelib.RowElement
import at.crowdware.nocodelib.ImageElement
import at.crowdware.nocodelib.SoundElement
import at.crowdware.nocodelib.SpacerElement
import at.crowdware.nocodelib.VideoElement
import at.crowdware.nocodelib.MarkdownElement
import at.crowdware.nocodelib.UIElement
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import at.crowdware.nocodelib.YoutubeElement
import at.crowdware.nocodelib.isQmlRootElement
import at.crowdware.nocodelib.parsePage
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun mobilePreview(currentProject: ProjectState?) {
    var parseError = ""
    val qml = currentProject?.currentFileContent?.text ?: ""
    val ext = currentProject?.extension
    var lastQml = remember { mutableStateOf("") }
    var lastPage = remember { mutableStateOf(null as Page?) }

    val parsedPage: Page? = if(qml != lastQml.value) {
        try {
            if (ext == "qml") {
                if (qml.isEmpty()) {
                    parseError = "no page loaded"
                    null
                } else {
                    if (isQmlRootElement(qml, "Page")) {
                        println("parsing page")
                        val page = parsePage(qml)
                        if (page.elements.isEmpty()) {
                            parseError = "page is empty"
                            null
                        } else {
                            lastPage.value = page
                            lastQml.value = qml
                            page
                        }
                    } else {
                        parseError = "no page loaded"
                        null
                    }
                }
            } else {
                null
            }
        } catch (e: Exception) {
            parseError = e.message ?: ""
            println("Error parsing source: ${e.message}")
            null
        }
    } else {
        lastPage.value
    }



    Column(modifier = Modifier.width(430.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
        BasicText(
            text = "Mobile Preview",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface), // Fills the entire available space
            contentAlignment = Alignment.Center
        ) {
            // Outer phone box with fixed aspect ratio (9:16) that scales dynamically
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(360.dp, 640.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp)) // Rounded corners for the phone shape
                    .background(Color(0xFF353739)) // Outer phone background color
                    .border(2.dp, Color.Gray, RoundedCornerShape(24.dp)) // Optional border
            ) {
                // Inner screen with relative size (not aspect ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f) // Width of the screen relative to the phone body
                        .fillMaxHeight(0.9f) // Height of the screen relative to the phone body
                        .align(Alignment.Center) // Center the screen inside the phone
                        .background(Color.Black)
                    //.clip(RoundedCornerShape(16.dp))
                ) {
                    // Scalable content inside the screen
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        if (parsedPage != null) {
                            Row(
                                modifier = Modifier
                                    .padding(
                                        start = parsedPage.padding.left.dp,
                                        top = parsedPage.padding.top.dp,
                                        bottom = parsedPage.padding.bottom.dp,
                                        end = parsedPage.padding.right.dp
                                    )
                                    .fillMaxSize()
                                    .background(color = hexToColor(parsedPage.backgroundColor))
                            ) {
                                RenderPage(parsedPage)
                            }
                        } else {
                            Row {
                                Text(text = parseError, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
            CustomText(
                text = element.text,
                color = element.color,
                fontSize = element.fontSize,
                fontWeight = element.fontWeight,
                textAlign = element.textAlign
            )
        }
        is MarkdownElement -> {
            val parsedMarkdown = parseMarkdown(element.text)

            Text(
                text = parsedMarkdown,
                style = TextStyle(color = hexToColor(element.color))
            )
        }
        is ButtonElement -> {
            Button(modifier = Modifier.fillMaxWidth(), onClick =  { handleButtonClick(element.link) }) {
                Text(text = element.label)
            }
        }
        is ColumnElement -> {
            Column(modifier = Modifier.padding(
                top = element.padding.top.dp,
                bottom = element.padding.bottom.dp,
                start = element.padding.left.dp,
                end = element.padding.right.dp
            ).fillMaxWidth()) {
                for (childElement in element.uiElements) {
                    RenderUIElement(childElement)
                }
            }
        }
        is RowElement -> {
            Row(modifier = Modifier.padding(
                top = element.padding.top.dp,
                bottom = element.padding.bottom.dp,
                start = element.padding.left.dp,
                end = element.padding.right.dp
            ).fillMaxWidth()) {
                for (childElement in element.uiElements) {
                    RenderUIElement(childElement)
                }
            }
        }
        is ImageElement -> {
            dynamicImageFromAssets(filename = element.src, element.scale, element.link)
        }
        is SoundElement -> {
            dynamicSoundfromAssets(element.src)
        }
        is SpacerElement -> {
            Spacer(modifier = Modifier.height(element.height.dp))
        }
        is VideoElement -> {
            dynamicVideofromAssets(element.src, element.height)
        }
        is YoutubeElement -> {
            dynamicYoutube(element.height)
        }
        else -> {
            // Hier können andere Elemente behandelt werden
            println("Unknown element: $element")
        }
    }
}

fun hexToColor(hex: String): Color {
    val color = hex.trimStart('#')
    return when (color.length) {
        6 -> {
            // Hex without alpha (e.g., "RRGGBB")
            val r = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b)
        }
        8 -> {
            // Hex with alpha (e.g., "AARRGGBB")
            val a = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val r = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(6, 8).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b, a)
        }
        else -> Color.Black
    }
}

@Composable
fun RenderPage(page: Page) {
    for (element in page.elements) {
        RenderUIElement(element)
    }
}

fun parseMarkdown(markdown: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = markdown.split("\n") // Verarbeite alle Zeilen

    for (i in lines.indices) {
        val line = lines[i]
        var j = 0
        while (j < line.length) {
            when {
                line.startsWith("###### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("###### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("##### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("##### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("#### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("#### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("## ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## ").trim())
                    }
                    j = line.length
                }
                line.startsWith("# ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# ").trim())
                    }
                    j = line.length
                }
                line.startsWith("***", j) -> {
                    val endIndex = line.indexOf("***", j + 3)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 3, endIndex).trim())
                        }
                        j = endIndex + 3
                    } else {
                        builder.append("***")
                        j += 3
                    }
                }
                line.startsWith("**", j) -> {
                    val endIndex = line.indexOf("**", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("**")
                        j += 2
                    }
                }
                line.startsWith("*", j) -> {
                    val endIndex = line.indexOf("*", j + 1)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 1, endIndex).trim())
                        }
                        j = endIndex + 1
                    } else {
                        builder.append("*")
                        j += 1
                    }
                }
                line.startsWith("~~", j) -> {
                    val endIndex = line.indexOf("~~", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("~~")
                        j += 2
                    }
                }
                line.startsWith("(c)", j) || line.startsWith("(C)", j) -> {
                    builder.append("©")
                    j += 3
                }
                line.startsWith("(r)", j) || line.startsWith("(R)", j) -> {
                    builder.append("®")
                    j += 3
                }
                line.startsWith("(tm)", j) || line.startsWith("(TM)", j) -> {
                    builder.append("™")
                    j += 4
                }
                else -> {
                    builder.append(line[j])
                    j++
                }
            }
        }

        // Füge Zeilenumbrüche nur zwischen den Zeilen hinzu, nicht am Ende
        if (i < lines.size - 1) {
            builder.append("\n")
        }
    }

    return builder.toAnnotatedString()
}

@Composable
expect fun dynamicImageFromAssets(filename: String, scale: String, link: String)
@Composable
expect fun dynamicSoundfromAssets(filename: String)
@Composable
expect fun dynamicVideofromAssets(filename: String, height: Int)
@Composable
expect fun dynamicYoutube(height: Int)

fun handleButtonClick(link: String) {
    when {
        link.startsWith("page:") -> {
            val pageId = link.removePrefix("page:")
            loadPage(pageId)
        }
        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            openWebPage(url)
        }
        else -> {
            println("Unknown link type: $link")
        }
    }
}

@Composable
fun CustomText(
    text: String,
    color: Color,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start
) {
    // Determine the alignment for the Text
    val alignment = when (textAlign) {
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End -> Alignment.TopEnd
        else -> Alignment.TopStart
    }

    // Use a Box to apply the desired alignment
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment as Alignment
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

expect fun loadPage(pageId: String)
expect fun openWebPage(url: String)