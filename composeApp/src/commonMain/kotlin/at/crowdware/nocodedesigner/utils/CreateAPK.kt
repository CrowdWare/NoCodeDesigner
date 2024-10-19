package at.crowdware.nocodedesigner.utils

import at.crowdware.nocodedesigner.viewmodel.copyResourceToFile
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import javax.imageio.ImageIO
import kotlin.io.path.createTempDirectory

class CreateAPK {
    companion object {
        fun start(title: String, folder: String, source: String, app: App) {
            val tempDir = createTempDirectory().toFile()
            tempDir.mkdir()
            try {
                copyResourceToFile("apk/app-debug.apk", "${tempDir.path}/app-debug.apk")
                copyResourceToFile("apk/apksigner", "${tempDir.path}/apksigner")
                copyResourceToFile("apk/apksigner.jar", "${tempDir.path}/apksigner.jar")
                changeFilePermissions("${tempDir.path}/apksigner")
                copyResourceToFile("apk/apktool", "${tempDir.path}/apktool")
                copyResourceToFile("apk/apktool_2.9.3.jar", "${tempDir.path}/apktool_2.9.3.jar")
                changeFilePermissions("${tempDir.path}/apktool")
                runProcess(listOf("${tempDir.path}/apktool", "d", "app-debug.apk", "-f", "-o", "out/"), "${tempDir.path}/")

                changeAppId(app.id, "${tempDir.path}", app.name)
                changeIcon("$source/images/${app.icon}", "${tempDir.path}")
                copyFilesToAsset(source, tempDir.path)

                runProcess(listOf("${tempDir.path}/apktool", "b", "out/", "-o", "rebuild.apk"), "${tempDir.path}/")
                runProcess(
                    listOf(
                        "keytool",
                        "-genkey",
                        "-v",
                        "-keystore",
                        "debug.keystore",
                        "-keyalg",
                        "RSA",
                        "-keysize",
                        "2048",
                        "-validity",
                        "10000",
                        "-alias",
                        "androiddebugkey",
                        "-storepass",
                        "android123",
                        "-keypass",
                        "android123",
                        "-dname",
                        "CN=Android Debug,O=Android,C=US"
                    ), "${tempDir.path}/"
                )
                runProcess(
                    listOf(
                        "${tempDir.path}/apksigner",
                        "sign",
                        "--ks",
                        "debug.keystore",
                        "--ks-key-alias",
                        "androiddebugkey",
                        "--ks-pass",
                        "pass:android123",
                        "--key-pass",
                        "pass:android123",
                        "--out",
                        "signed.apk",
                        "rebuild.apk"
                    ), "${tempDir.path}/"
                )
                runProcess(listOf("keytool", "-delete", "-alias", "androiddebugkey", "-keystore", "debug.keystore"),"${tempDir.path}")
                File(tempDir, "signed.apk").copyTo(File("$folder/$title.apk"), overwrite = true)
                tempDir.deleteRecursively()
                println("ready")
            } catch(e: Exception) {
                println("Error while creating APK: ${e.message}")
            }
        }

        fun copyFilesRecursively(sourceDir: File, targetDir: File) {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            sourceDir.listFiles()?.forEach { file ->
                val targetFile = File(targetDir, file.name.replace(".sml", "_sml"))

                if (file.isDirectory) {
                    copyFilesRecursively(file, targetFile)
                } else if (file.extension != "py" && file.name != ".DS_Store") {
                    println("Copying ${file.absolutePath} to ${targetFile.absolutePath}")
                    file.copyTo(targetFile, overwrite = true)
                }
            }
        }

        fun copyFilesToAsset(source: String, folder: String) {
            val sourceDir = File(source)
            val outputDir = File("$folder/out/assets")
            if(!outputDir.exists())
                outputDir.mkdir()

            copyFilesRecursively(sourceDir, outputDir)
        }

        fun changeAppId(newId: String, folder: String, name: String) {
            val orgAppId = "at.crowdware.nocodebrowser"
            val orgAppPath = "at/crowdware/nocodebrowser"
            val path = newId.replace(".", "/")

            val outputDir = File("$folder/out")
            outputDir.walk().forEach { file ->
                if (file.isFile && file.extension == "smali") {
                    changeValueInFile(file, orgAppId, newId)
                    changeValueInFile(file, orgAppPath, path)
                }
            }
            changeValueInFile(File("$folder/out/AndroidManifest.xml"), orgAppId, newId)
            changeValueInFile(File("$folder/out/AndroidManifest.xml"), "\"NoCodeBrowser", "\"$name")
        }

        fun changeValueInFile(file: File, oldValue: String, newValue: String) {
            val content = file.readText()
            val updatedContent = content.replace(oldValue, newValue)
            file.writeText(updatedContent)
        }

        fun runProcess(command: List<String>, workingDirectory: String) {
            try {
                val process = ProcessBuilder(command)
                    .directory(File(workingDirectory))
                    .inheritIO()
                    .start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    println("Command ${command[0]} executed successfully!")
                } else {
                    println("Command ${command[0]} failed with exit code: $exitCode")
                }
            } catch (e: Exception) {
                println("runProzess: ${e.message}")
                e.printStackTrace()
            }
        }

        fun changeFilePermissions(filePath: String) {
            val path: Path = Paths.get(filePath)

            // Definiere die gewünschten Berechtigungen
            val permissions = setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_EXECUTE,
                PosixFilePermission.OTHERS_READ,
                PosixFilePermission.OTHERS_EXECUTE
            )

            try {
                Files.setPosixFilePermissions(path, permissions)
                println("Permissions changed successfully!")
            } catch (e: Exception) {
                println("Error changing permissions: ${e.message}")
                e.printStackTrace()
            }
        }

        fun makeRound(iconFileName: String, folder: String) {
            val img = ImageIO.read(File(iconFileName)).let { original ->
                val imgResized = original.getScaledInstance(192, 192, java.awt.Image.SCALE_SMOOTH)
                BufferedImage(192, 192, BufferedImage.TYPE_INT_ARGB).apply {
                    val g2d = createGraphics()
                    g2d.drawImage(imgResized, 0, 0, null)
                    g2d.dispose()
                }
            }

            val size = minOf(img.width, img.height)
            val mask = BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY)

            val g2d = mask.createGraphics()
            g2d.color = Color.WHITE
            g2d.fillOval(0, 0, size, size)
            g2d.dispose()

            val result = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g2dResult = result.createGraphics()
            g2dResult.drawImage(img, 0, 0, null)
            g2dResult.clip = java.awt.geom.Area(java.awt.geom.RoundRectangle2D.Double(0.0, 0.0, size.toDouble(), size.toDouble(), size.toDouble(), size.toDouble()))
            g2dResult.drawImage(img, 0, 0, null)
            g2dResult.dispose()
            ImageIO.write(result, "PNG", File("${folder}/round.png"))
        }

        fun changeIcon(iconFileName: String, folder: String) {
            println(iconFileName)
            makeRound(iconFileName, folder)
            resizeIcon(iconFileName, 192, "xxxhdpi", 432, folder)
            resizeIcon(iconFileName, 144, "xxhdpi", 324, folder)
            resizeIcon(iconFileName, 96, "xhdpi", 216, folder)
            resizeIcon(iconFileName, 72, "hdpi", 162, folder)
            resizeIcon(iconFileName, 48, "mdpi", 108, folder)

            File("$folder/round.png").delete()
        }

        fun resizeIcon(iconFileName: String, size: Int, subDir: String, sizeForeground: Int, folder: String) {
            val outputDir = File("$folder/out/res/mipmap-$subDir")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }

            val img1 = ImageIO.read(File(iconFileName))
            if (img1 != null) {
                val imgResized1 = img1.getScaledInstance(size, size, Image.SCALE_SMOOTH)
                val bufferedImage1 = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
                val g2d1 = bufferedImage1.createGraphics()
                g2d1.drawImage(imgResized1, 0, 0, null)
                g2d1.dispose()

                ImageIO.write(bufferedImage1, "PNG", File("${outputDir.path}/ic_launcher.png"))
            }

            val img2 = ImageIO.read(File("$folder/round.png"))
            if (img2 != null) {
                val imgResized2 = img2.getScaledInstance(size, size, Image.SCALE_SMOOTH)
                val bufferedImage2 = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
                val g2d2 = bufferedImage2.createGraphics()
                g2d2.drawImage(imgResized2, 0, 0, null)
                g2d2.dispose()

                ImageIO.write(bufferedImage2, "PNG", File("${outputDir.path}/ic_launcher_round.png"))
            }

            val img3 = ImageIO.read(File(iconFileName))
            if (img3 != null) {
                val imgResized3 = img3.getScaledInstance(sizeForeground, sizeForeground, Image.SCALE_SMOOTH)
                val bufferedImage3 = BufferedImage(sizeForeground, sizeForeground, BufferedImage.TYPE_INT_ARGB)
                val g2d3 = bufferedImage3.createGraphics()
                g2d3.drawImage(imgResized3, 0, 0, null)
                g2d3.dispose()

                ImageIO.write(bufferedImage3, "PNG", File("${outputDir.path}/ic_launcher_foreground.png"))
            }
        }


    }
}