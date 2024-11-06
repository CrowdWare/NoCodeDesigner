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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import at.crowdware.nocodedesigner.viewmodel.ProjectState
import at.crowdware.nocodedesigner.viewmodel.copyResourceToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        fun start(title: String, folder: String, source: String, app: App, currentProject: ProjectState) {
            val tempDir = createTempDirectory().toFile()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    tempDir.mkdir()
                    currentProject.parseError = "Build APK started...\n"
                    copyResourceToFile("apk${File.separator}app-debug.apk", "${tempDir.path}${File.separator}app-debug.apk")
                    currentProject.parseError += "base apk copied\n"
                    copyResourceToFile("apk${File.separator}apksigner", "${tempDir.path}${File.separator}apksigner")
                    currentProject.parseError += "apksigner copied\n"
                    copyResourceToFile("apk${File.separator}apksigner.jar", "${tempDir.path}${File.separator}apksigner.jar")
                    currentProject.parseError += "apksigner.jar copied\n"
                    changeFilePermissions("${tempDir.path}${File.separator}apksigner")
                    currentProject.parseError += "permission added\n"
                    copyResourceToFile("apk${File.separator}apktool", "${tempDir.path}${File.separator}apktool")
                    copyResourceToFile("apk${File.separator}apktool.bat", "${tempDir.path}${File.separator}apktool.bat")
                    currentProject.parseError += "apktool copied\n"
                    copyResourceToFile("apk${File.separator}apktool_2.9.3.jar", "${tempDir.path}${File.separator}apktool_2.9.3.jar")
                    currentProject.parseError += "apktool.jar copied\n"
                    changeFilePermissions("${tempDir.path}${File.separator}apktool")
                    currentProject.parseError += "permission added\n"
                    runProcess(
                        listOf("${tempDir.path}${File.separator}apktool", "d", "app-debug.apk", "-f", "-o", "out${File.separator}"),
                        "${tempDir.path}${File.separator}", currentProject
                    )
                    currentProject.parseError += "apk extracted\n"
                    changeAppId(app.id, "${tempDir.path}", app.name)
                    currentProject.parseError += "appId changed\n"
                    changeIcon("$source${File.separator}images${File.separator}${app.icon}", "${tempDir.path}")
                    currentProject.parseError += "icon exchanged\n"
                    copyFilesToAsset(source, tempDir.path)
                    currentProject.parseError += "assets copied\n"

                    runProcess(
                        listOf("${tempDir.path}${File.separator}apktool", "b", "out${File.separator}", "-o", "rebuild.apk"),
                        "${tempDir.path}${File.separator}", currentProject
                    )
                    currentProject.parseError += "apk builded\n"
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
                        ), "${tempDir.path}${File.separator}", currentProject
                    )
                    currentProject.parseError += "keypair generated\n"
                    runProcess(
                        listOf(
                            "${tempDir.path}${File.separator}apksigner",
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
                        ), "${tempDir.path}${File.separator}", currentProject
                    )
                    currentProject.parseError += "apk signed\n"
                    runProcess(
                        listOf(
                            "keytool",
                            "-delete",
                            "-alias",
                            "androiddebugkey",
                            "-keystore",
                            "debug.keystore"
                        ), "${tempDir.path}", currentProject
                    )
                    currentProject.parseError += "keypair deleted\n"
                    File(tempDir, "signed.apk").copyTo(File("$folder${File.separator}$title.apk"), overwrite = true)
                    tempDir.deleteRecursively()
                    currentProject.parseError += "cleaned up\n"
                    currentProject.parseError += "$title.apk is copied to $folder\n"
                } catch (e: Exception) {
                    println("Error while creating APK: ${e.message}")
                }
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
                    file.copyTo(targetFile, overwrite = true)
                }
            }
        }

        fun copyFilesToAsset(source: String, folder: String) {
            val sourceDir = File(source)
            val outputDir = File("$folder${File.separator}out${File.separator}assets")
            if (!outputDir.exists())
                outputDir.mkdir()

            copyFilesRecursively(sourceDir, outputDir)
        }

        fun changeAppId(newId: String, folder: String, name: String) {
            val orgAppId = "at.crowdware.nocodebrowser"
            val orgAppPath = "at/crowdware/nocodebrowser"
            val path = newId.replace(".", "/")
            // TODO.. check path on Windows

            val outputDir = File("$folder${File.separator}out")
            outputDir.walk().forEach { file ->
                if (file.isFile && file.extension == "smali") {
                    changeValueInFile(file, orgAppId, newId)
                    changeValueInFile(file, orgAppPath, path)
                }
            }
            changeValueInFile(File("$folder${File.separator}out${File.separator}AndroidManifest.xml"), orgAppId, newId)
            changeValueInFile(File("$folder${File.separator}out${File.separator}AndroidManifest.xml"), "\"NoCodeBrowser", "\"$name")
        }

        fun changeValueInFile(file: File, oldValue: String, newValue: String) {
            val content = file.readText()
            val updatedContent = content.replace(oldValue, newValue)
            file.writeText(updatedContent)
        }

        fun runProcess(command: List<String>, workingDirectory: String, currentProject: ProjectState) {
            val cmd = command[0].substringAfterLast(File.separator)
            try {
                val process = ProcessBuilder(command)
                    .directory(File(workingDirectory))
                    .inheritIO()
                    .start()
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    currentProject.parseError += "${cmd} executed successfully!\b"
                } else {
                    currentProject.parseError += "${cmd} failed with exit code: $exitCode\n"
                }
            } catch (e: Exception) {
                currentProject.parseError += "${cmd} failed with error: ${e.message}\n"
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
            g2dResult.clip = java.awt.geom.Area(
                java.awt.geom.RoundRectangle2D.Double(
                    0.0,
                    0.0,
                    size.toDouble(),
                    size.toDouble(),
                    size.toDouble(),
                    size.toDouble()
                )
            )
            g2dResult.drawImage(img, 0, 0, null)
            g2dResult.dispose()
            ImageIO.write(result, "PNG", File("${folder}${File.separator}round.png"))
        }

        fun changeIcon(iconFileName: String, folder: String) {
            println(iconFileName)
            makeRound(iconFileName, folder)
            resizeIcon(iconFileName, 192, "xxxhdpi", 432, folder)
            resizeIcon(iconFileName, 144, "xxhdpi", 324, folder)
            resizeIcon(iconFileName, 96, "xhdpi", 216, folder)
            resizeIcon(iconFileName, 72, "hdpi", 162, folder)
            resizeIcon(iconFileName, 48, "mdpi", 108, folder)

            File("$folder${File.separator}round.png").delete()
        }

        fun resizeIcon(iconFileName: String, size: Int, subDir: String, sizeForeground: Int, folder: String) {
            val outputDir = File("$folder${File.separator}out${File.separator}res${File.separator}mipmap-$subDir")
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

                ImageIO.write(bufferedImage1, "PNG", File("${outputDir.path}${File.separator}ic_launcher.png"))
            }

            val img2 = ImageIO.read(File("$folder${File.separator}round.png"))
            if (img2 != null) {
                val imgResized2 = img2.getScaledInstance(size, size, Image.SCALE_SMOOTH)
                val bufferedImage2 = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
                val g2d2 = bufferedImage2.createGraphics()
                g2d2.drawImage(imgResized2, 0, 0, null)
                g2d2.dispose()

                ImageIO.write(bufferedImage2, "PNG", File("${outputDir.path}${File.separator}ic_launcher_round.png"))
            }

            val img3 = ImageIO.read(File(iconFileName))
            if (img3 != null) {
                val imgResized3 = img3.getScaledInstance(sizeForeground, sizeForeground, Image.SCALE_SMOOTH)
                val bufferedImage3 = BufferedImage(sizeForeground, sizeForeground, BufferedImage.TYPE_INT_ARGB)
                val g2d3 = bufferedImage3.createGraphics()
                g2d3.drawImage(imgResized3, 0, 0, null)
                g2d3.dispose()

                ImageIO.write(bufferedImage3, "PNG", File("${outputDir.path}${File.separator}ic_launcher_foreground.png"))
            }
        }
    }
}