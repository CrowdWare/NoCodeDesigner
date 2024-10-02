package at.crowdware.nocodelib

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory
import androidx.compose.runtime.Composable
import android.content.Context
import org.xml.sax.InputSource


class AppHandler : DefaultHandler() {
    var appType: String = ""
    val items = mutableListOf<String>()

    private var isInNavigation = false

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: org.xml.sax.Attributes?) {
        when (qName) {
            "app" -> {
                // Lese die App-Attribute aus
                appType = attributes?.getValue("name") ?: "Unknown App"
            }
            "navigation" -> {
                isInNavigation = true
            }
            "item" -> {
                if (isInNavigation) {
                    // Lese die Page-IDs aus und füge sie der Liste hinzu
                    val pageId = attributes?.getValue("page") ?: ""
                    if (pageId.isNotEmpty()) {
                        items.add(pageId)
                    }
                }
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        if (qName == "navigation") {
            isInNavigation = false
        }
    }
}

actual class AppParser {
    actual fun parse(xmlData: String): App {
        val saxParserFactory = SAXParserFactory.newInstance()
        val saxParser = saxParserFactory.newSAXParser()

        val handler = AppHandler()

        saxParser.parse(InputSource(xmlData.byteInputStream()), handler)

        return App(type = handler.appType, items = handler.items)
    }
}

actual class PageParser {
    actual fun parse(xmlData: String): Page {
        val saxParserFactory = SAXParserFactory.newInstance()
        val saxParser = saxParserFactory.newSAXParser()

        val handler = PageHandler()

        saxParser.parse(InputSource(xmlData.byteInputStream()), handler)

        return Page(color = handler.color, backgroundColor = handler.backgroundColor, padding = handler.padding, elements = handler.uiElements)
    }
}

class PageHandler : DefaultHandler(), PageHandlerCommon by PageHandlerBase() {

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val attrs = mutableMapOf<String, String>()
        for (i in 0 until (attributes?.length ?: 0)) {
            attrs[attributes!!.getQName(i)] = attributes.getValue(i)
        }
        handleStartElement(qName ?: "", attrs)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        handleCharacters(String(ch, start, length).trim())
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        handleEndElement(qName ?: "")
    }
}
