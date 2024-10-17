package at.crowdware.nocodedesigner.utils

import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory

class CreateEbook {
    companion object {
        fun start(title: String, folder: String, source: String, theme: String = "Epub3") {
            val dir = File("$folder/$title")
            dir.mkdirs()

            val tempDir = createTempDirectory().toFile()
            val guid = UUID.randomUUID().toString()

            println("tmp: $tempDir $guid")

            copyAssets(theme, tempDir)

            File(tempDir, "EPUB/parts").mkdirs()
            File(tempDir, "EPUB/images").mkdirs()
            File(tempDir, "META-INF").mkdirs()

            val currentDir = System.getProperty("user.dir")

            copyImages(tempDir, source)
/*            writeMimetype(tempDir)
            writeContainer(tempDir)

            generatePackage(tempDir, "book", guid)
            val toc = generateParts(tempDir, "book")
            generateToc(tempDir, "book", toc)
            generateNcx(tempDir, "book", guid)

            System.setProperty("user.dir", tempDir.path)

            val files = getAllFiles(tempDir)

            ZipOutputStream(Files.newOutputStream(Paths.get("output.zip"))).use { zip ->
                files.forEach { file ->
                    zip.putNextEntry(ZipEntry(file.relativeTo(tempDir).pathString))
                    zip.write(file.readBytes())
                    zip.closeEntry()
                }
            }

            System.setProperty("user.dir", currentDir)

            tempDir.deleteRecursively()
            */

        }


        fun copyAssets(theme: String, targetDir: File) {

            val classLoader = Thread.currentThread().contextClassLoader

            // Path inside the resources (e.g., "themes/<theme>/assets")
            val resourcePath = "themes/$theme/assets"

            // Get the resource URL to determine if it's a directory
            val resourceURL = classLoader.getResource(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

            // Recursively copy the resources
            copyDirectoryFromResources(classLoader, resourceURL, resourcePath, targetDir)
        }

        fun copyDirectoryFromResources(classLoader: ClassLoader, resourceURL: URL, resourcePath: String, targetDir: File) {
            // Determine if the URL is a directory by checking for a JAR protocol or a regular file path
            if (resourceURL.protocol == "jar") {
                // If the resource is inside a JAR, we handle the jar entry
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

                    // Copy entry as stream
                    classLoader.getResourceAsStream(entry.name)?.use { inputStream ->
                        copyStreamToFile(inputStream, targetFile)
                    }
                }
            } else {
                // When running from a local file system (not inside a JAR)
                val directory = File(resourceURL.toURI())

                directory.walkTopDown().forEach { file ->
                    val relativePath = file.relativeTo(directory).path
                    val targetFile = File(targetDir, relativePath)

                    if (file.isDirectory) {
                        // Create directories in target
                        targetFile.mkdirs()
                    } else {
                        // Copy files
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
                    println("cpy: ${file.name}")
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }

        fun writeMimetype(dir: File) {
            // Logik zum Schreiben der mimetype-Datei
        }

        fun writeContainer(dir: File) {
            // Logik zum Schreiben der container.xml
        }

        fun generatePackage(dir: File, book: String, guid: String) {
            // Logik zur Generierung des EPUB-Pakets
        }

        fun generateParts(dir: File, book: String): String {
            // Logik zur Generierung der Teile (parts)
            return "toc"
        }

        fun generateToc(dir: File, book: String, toc: String) {
            // Logik zur Generierung der Table of Contents
        }

        fun generateNcx(dir: File, book: String, guid: String) {
            // Logik zur Generierung der NCX-Datei
        }

        fun getAllFiles(dir: File): List<File> {
            return dir.walk().filter { it.isFile }.toList()
        }
    }
}