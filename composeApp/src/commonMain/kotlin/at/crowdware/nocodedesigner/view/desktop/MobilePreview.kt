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
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodelib.Page
import at.crowdware.nocodelib.UIElement
import at.crowdware.nocodelib.UIElement.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.platform.*

/*
@Composable
fun mobilePreview(currentProject: ProjectState?) {
    var page: Page? = if (currentProject?.isPageLoaded == true) currentProject.page else null

    if(page == null && currentProject != null) {
        page = currentProject.cachedPage
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
                ) {
                    // Scalable content inside the screen
                    CompositionLocalProvider(
                        LocalDensity provides LocalDensity.current.copy(density = LocalDensity.current.density * 0.7f)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(360.dp, 640.dp)
                        ) {
                            if (page != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(
                                            start = page.padding.left.dp,
                                            top = page.padding.top.dp,
                                            bottom = page.padding.bottom.dp,
                                            end = page.padding.right.dp
                                        )
                                        //.fillMaxSize()
                                        .size(360.dp, 640.dp)
                                        .background(color = hexToColor(page.backgroundColor))
                                ) {
                                    RenderPage(page)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
*/

@Composable
fun mobilePreview(currentProject: ProjectState?) {
    var page: Page? = if (currentProject?.isPageLoaded == true) currentProject.page else null

    if(page == null && currentProject != null) {
        page = currentProject.cachedPage
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
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            // Outer phone box with fixed aspect ratio (9:16) that scales dynamically
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(360.dp, 640.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF353739))
                    .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
            ) {
                // Inner screen with relative size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.9f)
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
                        Box(
                            modifier = Modifier
                                .size((1.0/scale*360.0).dp, (1.0/scale*640).dp)
                                .background(Color.White)
                        ) {
                            if (page != null) {
                                Row(
                                    modifier = Modifier
                                        .padding(
                                            start = page.padding.left.dp,
                                            top = page.padding.top.dp,
                                            bottom = page.padding.bottom.dp,
                                            end = page.padding.right.dp
                                        )
                                        .fillMaxSize()
                                        .background(color = hexToColor(page.backgroundColor))
                                ) {
                                    RenderPage(page)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun renderText(element: TextElement) {
    CustomText(
        text = element.text,
        color = element.color,
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun renderMarkdown(element: MarkdownElement) {
    val parsedMarkdown = parseMarkdown(element.text)
    Text(
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(element.color)),
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun renderButton(element: ButtonElement) {
    Button(modifier = Modifier.fillMaxWidth(), onClick =  { handleButtonClick(element.link) }) {
        Text(text = element.label)
    }
}

@Composable
fun renderColumn(element: ColumnElement) {
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

@Composable
fun renderRow(element: RowElement) {
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

@Composable
fun RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
            renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(element)
        }
        is ButtonElement -> {
            renderButton(element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            dynamicImageFromAssets(filename = element.src, element.scale, element.link)
        }
        is SoundElement -> {
            dynamicSoundfromAssets(element.src)
        }
        is VideoElement -> {
            dynamicVideofromAssets(element.src)
        }
        is YoutubeElement -> {
            dynamicYoutube()
        }
        else -> {
            println("Unknown element: $element")
        }
    }
}

@Composable
fun RowScope.RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
            renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(element)
        }
        is ButtonElement -> {
            renderButton(element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            dynamicImageFromAssets(filename = element.src, element.scale, element.link)
        }
        is SoundElement -> {
            dynamicSoundfromAssets(element.src)
        }
        is SpacerElement -> {
            var mod = Modifier as Modifier

            if (element.amount > 0 )
                mod = mod.then(Modifier.width(element.amount.dp))
            if (element.weight > 0.0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))

            Spacer(modifier = mod)
        }
        is VideoElement -> {
            dynamicVideofromAssets(element.src)
        }
        is YoutubeElement -> {
            dynamicYoutube()
        }
        else -> {
            println("Unsupported element: $element")
        }
    }
}

@Composable
fun ColumnScope.RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
           renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(element)
        }
        is ButtonElement -> {
            renderButton(element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            dynamicImageFromAssets(filename = element.src, element.scale, element.link)
        }
        is SoundElement -> {
            dynamicSoundfromAssets(element.src)
        }
        is SpacerElement -> {
            var mod = Modifier as Modifier

            if (element.amount >0 )
                mod = mod.then(Modifier.height(element.amount.dp))
            if (element.weight > 0.0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))

            Spacer(modifier = mod)
        }
        is VideoElement -> {
            dynamicVideofromAssets(element.src)
        }
        is YoutubeElement -> {
            dynamicYoutube()
        }
        else -> {
            println("Unsupported element: $element")
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
expect fun dynamicVideofromAssets(filename: String)
@Composable
expect fun dynamicYoutube()

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