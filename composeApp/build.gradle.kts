import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

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
    /*
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
    }*/
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
                //implementation("com.mikepenz:multiplatform-markdown-renderer:0.26.0")
                //implementation("com.github.jeziellago:compose-markdown:0.2.7")
                //implementation("org.jetbrains.compose.ui:ui-markdown:1.0.0-alpha4")
                implementation("org.jetbrains.compose.material:material-icons-extended:1.6.11")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation(project(":nocodeLib"))
                //implementation(files("/Users/art/SourceCode/NoCode/NoCodeLib/composeLib/build/libs/nocodelib.jar"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("net.java.dev.jna:jna:5.9.0")
                implementation("net.java.dev.jna:jna-platform:5.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                //implementation(project(":NoCodeLib"))
                //implementation(files("/Users/art/SourceCode/NoCode/NoCodeLib/composeLib/build/libs/composeLib-desktop.jar"))
            }
        }

       val wasmJsMain by getting {
            dependencies {

                implementation(compose.runtime)
                //implementation(compose.html.core)

                //implementation("org.jetbrains.compose.web:web-core:1.6.0")
                //implementation("org.jetbrains.compose.web:web-core:1.6.11")  // Aktuelle Version von web-core
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")  // Korrekte Coroutines-Version
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                //implementation(project(":NoCodeLib"))
                //implementation(files("/Users/art/SourceCode/NoCode/NoCodeLib/composeLib/build/libs/composeLib-wasm-js.klib"))
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
            packageVersion = "1.0.0"
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

tasks.register("deploy") {
    dependsOn("copyDylibToApp") // Kopiere die .dylib Datei zuerst
    finalizedBy("packageDmg") // Rufe den bestehenden packageDmg Task auf
    doFirst {
        println("Copying .dylib to .app and packaging .dmg")
    }
}

tasks.register<Copy>("copyDylibToApp") {
    dependsOn("assemble") // Sicherstellen, dass die .app zuerst erstellt wird
    from("$buildDir/composeApp/mac/.build/release/libmac.dylib") // Pfad zur erstellten .dylib Datei
    into("$buildDir/composeApp/build/compose/binaries/main/app/macApp/at.crowdware.nocodedesigner/Contents/Resources") // Zielordner in der .app Struktur
}
