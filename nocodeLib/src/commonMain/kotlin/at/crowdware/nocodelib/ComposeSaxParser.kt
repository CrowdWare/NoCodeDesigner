package at.crowdware.nocodelib

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


data class Page(val color: String, val backgroundColor: String, val padding: Padding, val elements: List<UIElement>)
data class App(val type: String, val items: MutableList<String>)
sealed class UIElement
data class TextElement(val text: String, val color: String, val fontSize: TextUnit, val fontWeight: FontWeight, val textAlign: TextAlign) : UIElement()
data class ButtonElement(val label: String, val link: String) : UIElement()
data class ImageElement(val src: String, val scale: String, val link: String) : UIElement()
data class SpacerElement(val height: Int) : UIElement()
data class VideoElement(val src: String, val height: Int) : UIElement()
data class YoutubeElement(val id: String, val height: Int) : UIElement()
data class SoundElement(val src: String) : UIElement()
data class RowElement(val padding: Padding, val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
data class ColumnElement(val padding: Padding, val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
data class MarkdownElement(val text: String, val color: String) : UIElement()
data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)

expect class AppParser() {
    fun parse(xmlData: String): App
}

expect class PageParser() {
    fun parse(xmlData: String): Page
}

interface PageHandlerCommon {
    val uiElements: MutableList<UIElement>
    var currentElement: String
    var currentText: StringBuilder
    var colorString: String
    var fontWeight: FontWeight
    var fontSize: TextUnit
    var textAlign: TextAlign
    var inRow: Boolean
    var inColumn: Boolean
    var row: RowElement
    var column: ColumnElement
    var color: String
    var backgroundColor: String
    var padding: Padding

    fun handleStartElement(qName: String, attributes: Map<String, String>)
    fun handleCharacters(text: String)
    fun handleEndElement(qName: String)
}

open class PageHandlerBase : PageHandlerCommon {
    override val uiElements = mutableListOf<UIElement>()
    override var currentElement = ""
    override var currentText = StringBuilder()
    override var colorString = "#000000"
    override var fontWeight = FontWeight.Normal
    override var fontSize = 18.sp
    override var textAlign = TextAlign.Left
    override var inRow = false
    override var inColumn = false
    override var row = RowElement(Padding(0, 0, 0, 0))
    override var column = ColumnElement(Padding(0, 0, 0, 0))
    override var color = "#000000"
    override var backgroundColor = "#FFFFFF"
    override var padding = Padding(0, 0, 0, 0)
    private val elementStack = mutableListOf<UIElement>()

    override fun handleStartElement(qName: String, attributes: Map<String, String>) {
        currentElement = qName

        when (qName) {
            "page" -> {
                padding = parsePadding(attributes["padding"] ?: "0")
                color = attributes["color"] ?: "#000000"
                backgroundColor = attributes["backgroundColor"] ?: "#FFFFFF"
            }

            "text" -> {
                currentText.clear()
                colorString = attributes["color"] ?: color // inherits the page color
                fontWeight = when (attributes["fontWeight"] ?: "normal") {
                    "thin" -> FontWeight.Thin
                    "extralight" -> FontWeight.ExtraLight
                    "light" -> FontWeight.Light
                    "normal", "regular" -> FontWeight.Normal
                    "medium" -> FontWeight.Medium
                    "semibold" -> FontWeight.SemiBold
                    "bold" -> FontWeight.Bold
                    "extrabold" -> FontWeight.ExtraBold
                    "black" -> FontWeight.Black
                    else -> FontWeight.Normal
                }
                textAlign = when (attributes["textAlign"] ?: "left") {
                    "left" -> TextAlign.Left
                    "right" -> TextAlign.Right
                    "center" -> TextAlign.Center
                    "justify" -> TextAlign.Justify
                    else -> TextAlign.Left
                }
                fontSize = (attributes["fontSize"] ?: "18").toInt().sp
            }

            "markdown" -> {
                currentText.clear()
                colorString = attributes["color"] ?: color // inherits the page color
            }

            "spacer" -> {
                val spacerElement = SpacerElement((attributes["height"] ?: "8").toInt())
                addElementToStack(spacerElement)
            }

            "button" -> {
                val label = attributes["label"] ?: ""
                val link = attributes["link"] ?: ""
                val buttonElement = ButtonElement(label, link)
                addElementToStack(buttonElement)
            }

            "row" -> {
                val padding = parsePadding(attributes["padding"] ?: "0")
                val rowElement = RowElement(padding)
                addElementToStack(rowElement)
                elementStack.add(rowElement)
            }

            "column" -> {
                val padding = parsePadding(attributes["padding"] ?: "0")
                val columnElement = ColumnElement(padding)
                addElementToStack(columnElement)
                elementStack.add(columnElement)
            }

            "image" -> {
                val src = attributes["src"] ?: "default_image.png"
                val scale = attributes["scale"] ?: "fit"
                val link = attributes["link"] ?: ""
                val imageElement = ImageElement(src, scale, link)
                addElementToStack(imageElement)
            }

            "video" -> {
                val src = attributes["src"] ?: "default_video.mp4"
                val height = (attributes["height"] ?: "200").toInt()
                val videoElement = VideoElement(src, height)
                addElementToStack(videoElement)
            }

            "youtube" -> {
                val src = attributes["id"] ?: "P335ruDKONo" // hine ani Nessi Gomes
                val height = (attributes["height"] ?: "200").toInt()
                val videoElement = YoutubeElement(src, height)
                addElementToStack(videoElement)
            }

            "sound" -> {
                val src = attributes["src"] ?: "default_sound.mp3"
                val soundElement = SoundElement(src)
                addElementToStack(soundElement)
            }
        }
    }

    override fun handleCharacters(text: String) {
        if (currentElement == "text" || currentElement == "markdown") {
            currentText.append(text)
        }
    }

    override fun handleEndElement(qName: String) {
        when (qName) {
            "text" -> {
                val textElement = TextElement(
                    text = currentText.toString().trim(),
                    color = colorString,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    textAlign = textAlign
                )
                addElementToStack(textElement)
            }
            "markdown" -> {
                val md = MarkdownElement(currentText.toString().trim(), colorString)
                addElementToStack(md)
            }

            "row", "column" -> {
                elementStack.removeAt(elementStack.size - 1)
            }
        }
        currentElement = ""
    }

    private fun addElementToStack(element: UIElement) {
        if (elementStack.isNotEmpty()) {
            when (val parentElement = elementStack.last()) {
                is RowElement -> parentElement.uiElements.add(element)
                is ColumnElement -> parentElement.uiElements.add(element)
                else -> uiElements.add(element)
            }
        } else {
            uiElements.add(element)
        }
    }
}

fun parsePadding(padding: String): Padding {
    val paddingValues = padding.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // Alle Seiten gleich
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertikal und Horizontal gleich
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Oben, Rechts, Unten, Links
        else -> Padding(0, 0, 0, 0)
    }
}
