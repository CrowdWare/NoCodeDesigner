package at.crowdware.nocodelib

import org.w3c.dom.Node
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Attr


external fun parseXmlString(xmlData: String): Document

actual class AppParser {
    actual fun parse(xmlData: String): App {
        val parser = DOMParser()
        val doc = parseXmlString(xmlData)
        val handler = AppHandler()
        doc.documentElement?.let { element ->
            traverseXml(element, handler) // Verarbeite das root-Element
        }
        return App(type = "dom", items = handler.items)
    }

    private fun traverseXml(node: Node, handler: AppHandler) {
        when (node.nodeType) {
            Node.ELEMENT_NODE -> {
                val element = node as? Element ?: return

                // Erstelle ein Attribut-Map aus den XML-Attributen
                val attrs = mutableMapOf<String, String>()
                for (i in 0 until element.attributes.length) {
                    val attr = element.attributes.item(i) as? Attr // Cast zu Attr, falls es nicht null ist
                    if (attr != null) {
                        attrs[attr.name] = attr.value // Sichere Zugriffe auf name und value
                    }
                }
                handler.startElement(element.tagName, attrs)

                val children = element.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i) // Kann null sein
                    if (child != null) {
                        traverseXml(child, handler) // Nur nicht-null Nodes werden verarbeitet
                    }
                }

                handler.endElement(element.tagName)
            }
        }
    }
}


class AppHandler {
    var appType: String = ""
    val items = mutableListOf<String>()
    private var isInNavigation = false

    fun startElement(tagName: String, attributes: Map<String, String>) {
        when (tagName) {
            "app" -> appType = attributes["name"] ?: "Unknown App"
            "navigation" -> isInNavigation = true
            "item" -> if (isInNavigation) {
                val pageId = attributes["page"] ?: ""
                if (pageId.isNotEmpty()) {
                    items.add(pageId)
                }
            }
        }
    }

    fun endElement(tagName: String) {
        if (tagName == "navigation") {
            isInNavigation = false
        }
    }
}

actual class PageParser {
    actual fun parse(xmlData: String): Page {
        val parser = DOMParser()
        val doc = parseXmlString(xmlData)
        val handler = PageHandler()
        doc.documentElement?.let { element ->
            traverseXml(element, handler) // Verarbeite das root-Element
        }
        return Page(color = handler.color, backgroundColor = handler.backgroundColor, padding = handler.padding, elements = handler.uiElements)
    }

    private fun traverseXml(node: Node, handler: PageHandler) {
        when (node.nodeType) {
            Node.ELEMENT_NODE -> {
                val element = node as? Element ?: return

                // Erstelle ein Attribut-Map aus den XML-Attributen
                val attrs = mutableMapOf<String, String>()
                for (i in 0 until element.attributes.length) {
                    val attr = element.attributes.item(i) as? Attr // Cast zu Attr, falls es nicht null ist
                    if (attr != null) {
                        attrs[attr.name] = attr.value // Sichere Zugriffe auf name und value
                    }
                }
                handler.handleStartElement(element.tagName, attrs)

                val children = element.childNodes
                for (i in 0 until children.length) {
                    val child = children.item(i) // Kann null sein
                    if (child != null) {
                        traverseXml(child, handler) // Nur nicht-null Nodes werden verarbeitet
                    }
                }

                handler.handleEndElement(element.tagName)
            }
            Node.TEXT_NODE -> {
                val textContent = node.nodeValue?.trim()
                if (!textContent.isNullOrEmpty()) {
                    handler.handleCharacters(textContent)
                }
            }
        }
    }
}


class PageHandler : PageHandlerCommon by PageHandlerBase() {

    fun parse(xmlData: String) {
        val parser = DOMParser()
        val doc = parseXmlString(xmlData)
        doc.documentElement?.let { element ->
            traverseXml(element) // Verarbeite das root-Element
        }
    }

    private fun traverseXml(node: Node) {
        if (node.nodeType == Node.ELEMENT_NODE) {
            val element = node as? Element ?: return
            val attrs = mutableMapOf<String, String>()
            for (i in 0 until element.attributes.length) {
                val attr = element.attributes.item(i)
                if (attr != null) {
                    attrs[attr.name] = attr.value
                }
            }
            handleStartElement(element.tagName, attrs)

            val children = node.childNodes
            for (i in 0 until children.length) {
                val child = children.item(i) // Kann null sein
                if (child != null) {
                    traverseXml(child)
                }
            }
            handleEndElement(node.nodeName)
        } else if(node.nodeType == Node.TEXT_NODE) {
            val textContent = node.nodeValue?.trim()
            if (!textContent.isNullOrEmpty()) {
                handleCharacters(textContent)
            }
        }
    }
}
