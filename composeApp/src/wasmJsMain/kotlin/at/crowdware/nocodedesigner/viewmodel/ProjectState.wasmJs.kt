package at.crowdware.nocodedesigner.viewmodel

import at.crowdware.nocodelib.XmlAppParser
import at.crowdware.nocodelib.XmlPageParser
import at.crowdware.nocodedesigner.model.NodeType
import at.crowdware.nocodedesigner.model.TreeNode
import at.crowdware.nocodedesigner.model.extensionToNodeType
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.w3c.fetch.Response

actual fun saveFileContent(path: String, uuid: String, pid: String, content: String) {
    TODO("Not yet implemented")
}

@Serializable
data class FilePathRequest(val path: String)

// Server Response Model
@kotlinx.serialization.Serializable
data class ServerTreeNode(
    val name: String,
    val type: String, // 'f' für Datei, 'd' für Verzeichnis
    val children: List<ServerTreeNode>? = null
)

@kotlinx.serialization.Serializable
data class FileResponse(
    val status: String,
    val files: List<ServerTreeNode>
)

actual fun getNodeType(path: String): NodeType {
    val extension = path.substringAfterLast('.', "").lowercase()
    return extensionToNodeType[extension] ?: NodeType.OTHER
}

actual fun getDisplayName(path: String): String {
    return path.substringBeforeLast(".") // Entfernt die letzte Dateiendung, falls vorhanden
}

actual suspend fun loadFileContent(path: String, uuid: String, pid: String): String {
    return "Hello world!"
}

suspend fun fetchData(url: String): String {
    val response: Response = window.fetch(url).await() // Typ explizit angeben
    if (response.ok) {
        return response.text().await() // Rückgabewert von `text()` ist ebenfalls ein `Promise<String>`
    } else {
        throw Exception("Failed to fetch data: ${response.statusText}")
    }
}

fun callFetchFromKotlin(url: String, uuid: String, pid: String) {
    // Übergabe der Callback-Funktionen direkt in JavaScript
    fetchData(url, uuid, pid, ::onFetchResponse, ::onFetchError)
}

// Deklariere die externe JavaScript-Funktion `fetchData` mit den zusätzlichen Callbacks
external fun fetchData(
    url: String,
    uuid: String,
    pid: String,
    onSuccess: (String) -> Unit,  // Erfolg-Callback
    onError: (String) -> Unit     // Fehler-Callback
)

class WasmProjectState : ProjectState() {
    override suspend fun loadProjectFiles(path: String, uuid: String, pid: String) {

        callFetchFromKotlin("http://localhost:5000/listFiles", uuid, pid)
    }

    override suspend fun createProjectFiles(path: String, uuid: String, pid: String, name: String, appId:String) {
        TODO("Not yet implemented")
    }
}

fun convertToTreeNode(serverNode: ServerTreeNode, directoryPath: String): TreeNode {
    val nodeType = if (serverNode.type == "d") {
        NodeType.DIRECTORY
    } else {
        // Bestimme den Dateityp anhand der Dateierweiterung
        val extension = serverNode.name.substringAfterLast('.', "").lowercase()
        extensionToNodeType[extension] ?: NodeType.OTHER
    }

    return TreeNode(
        title = serverNode.name,
        path = "$directoryPath/${serverNode.name}",
        type = nodeType,
        children = serverNode.children?.map { convertToTreeNode(it, "$directoryPath/${serverNode.name}") }
    )
}


// Erfolg-Callback in Kotlin
fun onFetchResponse(response: String) {
    // JSON-Antwort in FileResponse umwandeln
    val fileResponse = Json.decodeFromString<FileResponse>(response)
    val nodes = fileResponse.files.map { convertToTreeNode(it, "") }
    val ps = GlobalProjectState.projectState
    if (ps != null) {
        val sortedNodes = nodes.sortedWith(compareBy<TreeNode> { it.type != NodeType.DIRECTORY }.thenBy { it.title })
        ps.treeData = sortedNodes.toList()

        // app.xml load and parse
        val xmlData = """
        <?xml version="1.0" encoding="utf-8"?>
        <app name="MyApp" id="at.crowdware.iav" icon="icon.png">
            <navigation type="HorizontalPager">
                <item page="home"/>
                <item page="page2"/>
                <item page="page3"/>
            </navigation>
        </app>
            """.trimIndent()
        val appParser = XmlAppParser()
        ps.app = appParser.parse(xmlData)

        println("App Type: ${ps.app.type}")
        println("Pages: ${ps.app.items.joinToString()}")

        val xml = "<page><text>Das ist eine test page</text><button label='btnLabel' link='btnlink'/></page>"
        val pageParser = XmlPageParser()
        ps.page = pageParser.parse(xml)
        println("Page : ${ps.page.elements.joinToString()}")

    } else {
        println("ProjectState is null!")
    }
}

// Fehler-Callback in Kotlin
fun onFetchError(errorMessage: String) {
    println("Fetch request failed: $errorMessage")
}

actual fun createProjectState(): ProjectState {
    return WasmProjectState()
}

actual fun fileExists(path: String): Boolean {
    TODO("Not yet implemented")
    return false
}