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

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.InputSource


class AppHandler : DefaultHandler() {
    var appType: String = ""
    val items = mutableListOf<String>()

    private var isInNavigation = false

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
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

class PageHandler : DefaultHandler(), PageHandlerCommon by PageHandlerBase() {

    override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
        val attrs = mutableMapOf<String, String>()
        for (i in 0 until (attributes?.length ?: 0)) {
            attrs[attributes!!.getQName(i)] = attributes.getValue(i)
        }
        handleStartElement(qName ?: "", attrs)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        handleCharacters(String(ch, start, length))
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        handleEndElement(qName ?: "")
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
        println("Creating new page with ${handler.uiElements.size} elements")
        return Page(color = handler.color, backgroundColor = handler.backgroundColor, padding = handler.padding, elements = handler.uiElements)
    }
}
