/*
 * Copyright (C) 2024 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class Page(
    @HexColorAnnotation
    var color: String,

    @HexColorAnnotation
    var backgroundColor: String,

    @PaddingAnnotation
    var padding: Padding,

   @IgnoreForDocumentation
    val elements: MutableList<UIElement>)
data class App(val type: String, val items: MutableList<String>)

sealed class UIElement {
    data object Zero : UIElement()
    data class TextElement(
        val text: String,
        val color: Color,
        val fontSize: TextUnit,
        val fontWeight: FontWeight,
        val textAlign: TextAlign
    ) : UIElement()
    data class ButtonElement(val label: String, val link: String) : UIElement()
    data class ImageElement(val src: String, val scale: String, val link: String) : UIElement()
    data class SpacerElement(val height: Int) : UIElement()
    data class VideoElement(val src: String, val height: Int) : UIElement()
    data class YoutubeElement(val id: String, val height: Int) : UIElement()
    data class SoundElement(val src: String) : UIElement()
    data class RowElement(val padding: Padding, val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
    data class ColumnElement(val padding: Padding, val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
    data class MarkdownElement(
        @MarkdownAnnotation
        val text: String,
        @HexColorAnnotation
        val color: String) : UIElement()
}

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)


fun generateDokuForClass(kClass: KClass<*>) {

    println("Class Name: ${kClass.simpleName}")

    kClass.members.forEach { member ->
        if (member is KProperty<*>) {
            if (member.annotations.any { it is IgnoreForDocumentation }) {
                return@forEach
            }

            member.annotations.forEach { annotation ->
                when (annotation) {
                    is HexColorAnnotation -> {
                        println("Property: ${member.name}, Description: ${annotation.description}")
                    }
                    is PaddingAnnotation -> {
                        println("Property: ${member.name}, Description: ${annotation.description}")
                    }
                    is MarkdownAnnotation ->{
                        println("Property: ${member.name}, Description: ${annotation.description}")
                    }
                }
            }
        }
    }
}

fun generateDoku() {
    generateDokuForClass(Page::class)
    generateDokuForClass(UIElement.MarkdownElement::class)
}
