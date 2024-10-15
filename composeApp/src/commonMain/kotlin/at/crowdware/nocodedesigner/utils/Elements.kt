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
package at.crowdware.nocodedesigner.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import java.time.LocalDateTime

data class App(
    val name: String = "",
    val icon: String = "",
    val id: String = "",
    val smlVersion: String = "",
    val theme: ThemeElement = ThemeElement(),
    val navigation: NavigationElement = NavigationElement(),
    val deployment: DeploymentElement = DeploymentElement()
)

data class ThemeElement(
    val primary: String = "",
    val onPrimary: String = ""
)

data class NavigationElement(
    val type: String = "",
    val items: MutableList<ItemElement> = mutableListOf()
)

data class DeploymentElement(
    val files: MutableList<FileElement> = mutableListOf()
)

data class FileElement(val path: String, val time: LocalDateTime)

data class ItemElement (val page: String)

@ElementAnnotation("A **Page** is the base element of the NoCodeApp. You can put all other Elements inside a Page.")
data class Page(
    @HexColorAnnotation
    var color: String,

    @HexColorAnnotation
    var backgroundColor: String,

    @PaddingAnnotation
    var padding: Padding,

    @IgnoreForDocumentation
    val elements: MutableList<UIElement>)

sealed class UIElement {
    data object Zero : UIElement()
    @ElementAnnotation("With a **Text** element you can render text on the page.")
    data class TextElement(
        @StringAnnotation
        val text: String,

        @HexColorAnnotation
        val color: Color,

        @IntAnnotation
        val fontSize: TextUnit,

        @StringAnnotation("Enter one of the following weights normal, extralight, thin, medium, bold, semibold, extrabold, black like **fontWeight: \"bold\"**")
        val fontWeight: FontWeight,

        @StringAnnotation("Enter one of the following alignments left, center, right like **textAlign: \"center\"**")
        val textAlign: TextAlign
    ) : UIElement()
    @ElementAnnotation("With a **Button** element you can render a clickable button on the page. With a click you can load other pages or external websites.")
    data class ButtonElement(
        @StringAnnotation
        val label: String,

        @LinkAnnotation
        val link: String) : UIElement()
    @ElementAnnotation("With an **Image** element you can display an image on the page.")
    data class ImageElement(
        @StringAnnotation("Enter the name of the image file like **src: \"sample.png\"**.\nThe image file should be imported into assets first.")
        val src: String,

        @StringAnnotation("Enter the value for scale like crop, fit, inside, fillbounds, fillheight, fillwidth, none **scale: \"fit\"**")
        val scale: String,

        @LinkAnnotation
        val link: String) : UIElement()
    @ElementAnnotation("With a **Spacer** element you can create a visual distance between other elements on the page.")
    data class SpacerElement(
        @IntAnnotation
        val amount: Int,

        @WeightAnnotation()
        val weight: Int) : UIElement()
    @ElementAnnotation("With a **Video** element you can show and play videos on the page.")
    data class VideoElement(
        @StringAnnotation("Enter the name of the video file like **src: \"sample.mp4\"**.\nThe video file should be imported into assets first.")
        val src: String) : UIElement()
    @ElementAnnotation("With a **Youtube** element you can show and play YouTube videos on the page.")
    data class YoutubeElement(
        @StringAnnotation("Enter the YouTube video id in quotes like **id:\"FCyiuG\"**")
        val id: String) : UIElement()
    @ElementAnnotation("With a **Sound** element you can play sounds when the page is loaded.")
    data class SoundElement(
        @StringAnnotation("Enter the name of the sound file like **src: \"sample.mp3\"**.\nThe sound file should be imported into assets first.")
        val src: String) : UIElement()
    @ElementAnnotation("With a **Row** element you can arrange elements horizontally on the page.")
    data class RowElement(
        @PaddingAnnotation
        val padding: Padding,

        val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
    @ElementAnnotation("With a **Column** element you can arrange elements vertically on the page.")
    data class ColumnElement(
        @PaddingAnnotation
        val padding: Padding,

        val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
    @ElementAnnotation("With a **Markdown** element you can render styled text on the page.")
    data class MarkdownElement(
        @MarkdownAnnotation
        val text: String,

        @HexColorAnnotation
        val color: String,

        @IntAnnotation
        val fontSize: TextUnit,

        @StringAnnotation("Enter one of the following weights normal, extralight, thin, medium, bold, semibold, extrabold, black like **fontWeight: \"bold\"**")
        val fontWeight: FontWeight,

        @StringAnnotation("Enter one of the following alignments left, center, right like **textAlign: \"center\"**")
        val textAlign: TextAlign

        ) : UIElement()
}

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)


