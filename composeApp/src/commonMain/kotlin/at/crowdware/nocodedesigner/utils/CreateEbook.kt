package at.crowdware.nocodedesigner.utils

import at.crowdware.nocodedesigner.Version
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import net.pwall.mustache.Template
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory


class CreateEbook {
    companion object {
        fun start(title: String, folder: String, source: String, book: Book) {
            val dir = File(folder)
            dir.mkdirs()

            val tempDir = createTempDirectory().toFile()
            val guid = UUID.randomUUID().toString()

            copyAssets(book.theme, tempDir)

            File(tempDir, "EPUB/parts").mkdirs()
            File(tempDir, "EPUB/images").mkdirs()
            File(tempDir, "META-INF").mkdirs()

            copyImages(tempDir, source)
            writeMimetype(tempDir)
            writeContainer(tempDir)

            generatePackage(tempDir, book, guid)
            val toc = generateParts(tempDir, book, source)
            generateToc(tempDir, book, toc)

            val files = getAllFiles(tempDir)

            ZipOutputStream(Files.newOutputStream(Paths.get("$folder/$title.epub"))).use { zip ->
                files.forEach { file ->
                    zip.putNextEntry(ZipEntry(file.relativeTo(tempDir).path))
                    zip.write(file.readBytes())
                    zip.closeEntry()
                }
            }
            tempDir.deleteRecursively()
        }


        fun copyAssets(theme: String, targetDir: File) {
            val classLoader = Thread.currentThread().contextClassLoader
            val resourcePath = "themes/$theme/assets"
            val resourceURL = classLoader.getResource(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

            copyDirectoryFromResources(classLoader, resourceURL, resourcePath, targetDir)
        }

        fun copyDirectoryFromResources(classLoader: ClassLoader, resourceURL: URL, resourcePath: String, targetDir: File) {
            if (resourceURL.protocol == "jar") {
                val jarPath = resourceURL.path.substringBefore("!")
                val jarFile = File(URL(jarPath).toURI())
                val jar = java.util.jar.JarFile(jarFile)

                jar.entries().asSequence().filter { entry ->
                    entry.name.startsWith(resourcePath) && !entry.isDirectory
                }.forEach { entry ->
                    val entryName = entry.name.removePrefix(resourcePath).trimStart('/')
                    val targetFile = File(targetDir, entryName)

                    if (!targetFile.parentFile.exists()) {
                        targetFile.parentFile.mkdirs() // Ensure parent directories exist
                    }

                    classLoader.getResourceAsStream(entry.name)?.use { inputStream ->
                        copyStreamToFile(inputStream, targetFile)
                    }
                }
            } else {
                val directory = File(resourceURL.toURI())

                directory.walkTopDown().forEach { file ->
                    val relativePath = file.relativeTo(directory).path
                    val targetFile = File(targetDir, relativePath)

                    if (file.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        classLoader.getResourceAsStream("$resourcePath/$relativePath")?.use { inputStream ->
                            copyStreamToFile(inputStream, targetFile)
                        }
                    }
                }
            }
        }

        fun copyStreamToFile(inputStream: InputStream, targetFile: File) {
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        fun copyImages(dir: File, source: String) {
            val sourceDir = File(source, "images")
            val targetDir = File(dir, "EPUB/images")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val targetFile = File(targetDir, file.name)
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }

        fun writeMimetype(dir: File) {
            val mimeFile = File(dir, "mimetype")
            mimeFile.writeText("application/epub+zip", Charsets.UTF_8)
        }

        fun writeContainer(dir: File) {
            val metaInfDir = File(dir, "META-INF")

            metaInfDir.mkdirs()

            val containerFile = File(metaInfDir, "container.xml")
            containerFile.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n" +
                    "  <rootfiles>\n" +
                    "    <rootfile full-path=\"EPUB/package.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                    "  </rootfiles>\n" +
                    "</container>", Charsets.UTF_8)
        }

        fun generatePackage(dir: File, book: Book, guid: String) {
            val context = mutableMapOf<String, Any>()

            context["uuid"] = guid
            context["lang"] = book.language
            context["title"] = book.name
            context["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            context["version"] = Version.version
            context["creator"] = book.creator

            val items = mutableListOf<Map<String, String>>()
            val spine = mutableListOf<String>()

            for (part in book.parts) {
                if (!part.pdfOnly) {
                    val name = part.name.replace(" ", "-").lowercase()
                    if (name != "toc") {
                        val item = mutableMapOf<String, String>()
                        item["href"] = "parts/$name.xhtml"
                        item["id"] = name
                        item["type"] = "application/xhtml+xml"
                        items.add(item)
                        spine.add(name)
                    }
                }
            }

            val imagesDir = File(dir, "EPUB/images")
            if (imagesDir.exists()) {
                imagesDir.walkTopDown().forEach { file ->
                    if (file.isFile && file.name != ".DS_Store") {
                        val filename = file.nameWithoutExtension
                        val extension = file.extension
                        val item = mutableMapOf<String, String>()
                        item["href"] = "images/${file.name}"
                        item["id"] = "${filename}_img"
                        item["type"] = "image/$extension"
                        items.add(item)
                    }
                }
            }
            // Add items and spine to context
            context["items"] = items
            context["spine"] = spine

            // Read and process the template file
            val classLoader = Thread.currentThread().contextClassLoader

            // Path inside the resources (e.g., "themes/<theme>/assets")
            val resourcePath = "themes/${book.theme}/layout/package.opf"
            val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
            val data = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

            val template = Template.parse(data)
            val renderedXml = template.processToString(context)

            // Write the rendered XML to the output file
            val outputPath = Paths.get(dir.path, "EPUB", "package.opf")
            outputPath.parent.createDirectories()
            File(outputPath.toUri()).writeText(renderedXml, Charsets.UTF_8)
        }

        fun generateParts(dir: File, book: Book, source: String): List<Map<String, Any>> {
            val toc = mutableListOf<Map<String, Any>>()

            val item = mutableMapOf<String, Any>(
                "href" to "toc.xhtml",
                "name" to if (book.language == "de") "Inhaltsverzeichnis" else "Table of Contents",
                "id" to "nav",
                "parts" to mutableListOf<Any>()
            )
            toc.add(item)

            val path = Paths.get("").toAbsolutePath().toString()

            for (part in book.parts) {
                if (!part.pdfOnly) {
                    val context = mutableMapOf<String, Any>()
                    val partSourcePath = Paths.get(source, "parts", part.src).toFile()

                    val text = partSourcePath.readText(Charsets.UTF_8)
                    val name = part.name.replace(" ", "-").lowercase()

                    if (name != "toc") {
                        val options = MutableDataSet()
                        options.set(HtmlRenderer.GENERATE_HEADER_ID, true)
                        options.set(HtmlRenderer.RENDER_HEADER_ID,true)

                        val parser = Parser.builder(options).build()
                        val document = parser.parse(text)
                        val renderer = HtmlRenderer.builder(options).build()
                        // Markdown processing and table fixing
                        val html = fixTables(renderer.render(document))

                        val linkList = getLinks(html, name)
                        toc.addAll(linkList)

                        context["content"] = html

                        val classLoader = Thread.currentThread().contextClassLoader
                        val resourcePath = "themes/${book.theme}/layout/template.xhtml"
                        val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
                        val templateData = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

                        val template = Template.parse(templateData)
                        val xhtml = template.processToString(context)

                        val outputFile = Paths.get(dir.path, "EPUB", "parts", "$name.xhtml").toFile()
                        outputFile.writeText(xhtml, Charsets.UTF_8)
                    }
                }
            }

            return toc
        }

        fun getAllFiles(dir: File): List<File> {
            return dir.walk().filter { it.isFile }.toList()
        }

        fun fixTables(text: String): String {
            return text
                .replace("<th align=\"center\"", "<th class=\"center\"")
                .replace("<th align=\"right\"", "<th class=\"right\"")
                .replace("<th align=\"left\"", "<th class=\"left\"")
                .replace("<td align=\"center\"", "<td class=\"center\"")
                .replace("<td align=\"right\"", "<td class=\"right\"")
                .replace("<td align=\"left\"", "<td class=\"left\"")
        }

        fun generateToc(dir: File, book: Book, parts: List<Map<String, Any>>) {
            val context = mutableMapOf<String, Any>()
            context["parts"] = parts

            val classLoader = Thread.currentThread().contextClassLoader
            val resourcePath = "themes/${book.theme}/layout/toc.xhtml"
            val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
            val templateData = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

            val template = Template.parse(templateData)
            val xhtml = template.processToString(context)

            val outputPath = Paths.get(dir.path, "EPUB", "parts", "toc.xhtml")
            Files.writeString(outputPath, xhtml, StandardCharsets.UTF_8)
        }

        private fun getLinks(text: String, partName: String): List<Map<String, Any>> {
            val nodes = mutableListOf<Map<String, Any>>()
            val linksList = mutableListOf<Map<String, Any>>()

            for (line in text.split("\n")) {
                if (line.isBlank()) continue

                val c = when {
                    line.startsWith("<h1 ") -> 1
                    line.startsWith("<h2 ") -> 2
                    line.startsWith("<h3 ") -> 3
                    line.startsWith("<h4 ") -> 4
                    line.startsWith("<h5 ") -> 5
                    line.startsWith("<h6 ") -> 6
                    else -> 0
                }

                if (c > 0) {
                    val idStart = line.indexOf("id=") + 4
                    val idEnd = line.indexOf('"', idStart)
                    val id = line.substring(idStart, idEnd)

                    val nameStart = line.indexOf(">", idEnd) + 1
                    val nameEnd = line.indexOf("<", nameStart)
                    val name = line.substring(nameStart, nameEnd)

                    val item = mutableMapOf<String, Any>()
                    item["href"] = "$partName.xhtml#$id"
                    item["name"] = name
                    item["id"] = id
                    item["parts"] = mutableListOf<Map<String, Any>>()

                    if (nodes.size < c) {
                        nodes.add(item)
                    } else {
                        nodes[c - 1] = item
                    }

                    if (c == 1) {
                        linksList.add(item)
                    } else {
                        (nodes[c - 2]["parts"] as MutableList<Map<String, Any>>).add(item)
                    }
                }
            }

            return linksList
        }
    }
}