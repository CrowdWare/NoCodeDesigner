import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.text.SimpleDateFormat
import java.util.Date

val minor = SimpleDateFormat("yy").format(Date()).toInt()
val build = SimpleDateFormat("MMddHH").format(Date()).toInt()
val version = "1.$minor.$build".take(10)


plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.0"

}

repositories {

    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") // JetBrains Compose Repository
}

kotlin {
    jvmToolchain(11) // Wenn Java 11 verwendet werden soll

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "composeApp"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    jvm("desktop")
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation("org.jetbrains.compose.material:material-icons-extended:1.6.11")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation(project(":nocodeLib"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("net.java.dev.jna:jna:5.9.0")
                implementation("net.java.dev.jna:jna-platform:5.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                kotlin.srcDir(layout.buildDirectory.dir("generated/version"))
            }
        }

       val wasmJsMain by getting {
            dependencies {

                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")  // Korrekte Coroutines-Version
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                kotlin.srcDir(layout.buildDirectory.dir("generated/version"))
            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "at.crowdware.nocodedesigner.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NoCodeDesigner"
            packageVersion = "$version"
            linux {
                iconFile.set(project.file("desktopAppIcons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("desktopAppIcons/WindowsIcon.ico"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icons/icon.icns"))
                bundleID = "at.crowdware.nocodedesigner.desktopApp"
            }
        }
    }
}

tasks.named("wasmJsProcessResources", Copy::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("src/commonMain/resources") {
        exclude("index.html") // explizit ausschließen
    }
    from("src/wasmMain/resources") {
        exclude("index.html") // explizit ausschließen
    }
}

tasks.named("wasmJsJar", Jar::class) {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Verhindert das Kopieren doppelter Manifest-Dateien
}


tasks.register<Copy>("copyDylibToApp") {
    dependsOn("createDistributable")

    // Berechne die Pfade zur Konfigurationszeit
    val fromPath = layout.buildDirectory.dir("../mac/.build/release/libmac.dylib").get().asFile
    val toPath = layout.buildDirectory.dir("compose/binaries/main/app/NoCodeDesigner.app/Contents/Resources").get().asFile

    // Vermeide die Verwendung von `file()` oder `project` zur Ausführungszeit
    from(fromPath)
    into(toPath)

    // Protokollierung und Überprüfung zur Ausführungszeit
    doLast {
        println("Copying $fromPath to $toPath")
        if (!fromPath.exists()) {
            throw GradleException(".dylib file not found at $fromPath")
        }
    }
}

tasks.register("deploy") {
    dependsOn("generateVersionFile")
    dependsOn("copyDylibToApp")
    finalizedBy("packageDmg")
    doFirst {
        println("Deploy App")
    }
}

tasks.named("assemble") {
    mustRunAfter("generateVersionFile")
}

tasks.register("generateVersionFile") {
    val outputDir = layout.buildDirectory.dir("generated/version").get().asFile
    val versionValue = version

    inputs.property("version", versionValue)
    outputs.dir(outputDir)

    doLast {
        // Schreibe die Versionsnummer in die Datei
        val versionFile = outputDir.resolve("Version.kt")
        versionFile.parentFile.mkdirs()
        versionFile.writeText("""
            package at.crowdware.nocodedesigner

            object Version {
                const val version = "$versionValue"
            }
        """.trimIndent())
        println("Version changed to: $versionValue")
    }
}

tasks.named("compileKotlinWasmJs") {
    dependsOn("generateVersionFile") // Stellt sicher, dass die Version-Datei vor dem Kompilieren für Wasm generiert wird
}

tasks.named("compileKotlinDesktop") {
    dependsOn("generateVersionFile") // Stellt sicher, dass die Version-Datei vor dem Kompilieren generiert wird
}

tasks.named("build") {
    dependsOn("generateVersionFile") // Stellt sicher, dass die Version-Datei vor dem Kompilieren generiert wird
}
