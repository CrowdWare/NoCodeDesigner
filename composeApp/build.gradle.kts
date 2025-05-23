import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.npm.importedPackageDir
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.*

// masterpiece in version numbering ;-)
// version will increment all 10 minutes
val currentDateTime: LocalDateTime = LocalDateTime.now()
val majorVersion = (currentDateTime.year - 2014) / 10
val yearPart = (currentDateTime.year - 2014) - 10
val monthPart = String.format("%02d", currentDateTime.monthValue)
val dayPart = String.format("%02d", currentDateTime.dayOfMonth)
val hourPart = String.format("%02d", currentDateTime.hour)
val minutesPart = String.format("%02d", currentDateTime.minute)
val version = "$majorVersion.$yearPart$monthPart.$dayPart$hourPart$minutesPart".take(11)

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.0"
}

repositories {

    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvmToolchain(17)

    jvm("desktop") {
        compilations.all {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                // Disable ProGuard-related tasks
                //kotlinOptions.freeCompilerArgs += "-Xno-proguard"
                kotlinOptions.freeCompilerArgs += listOf()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // NoCodeLib dependency
                implementation(project(":nocodelib"))
                
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation("org.jetbrains.compose.material:material-icons-extended:1.7.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                implementation("br.com.devsrsouza.compose.icons:font-awesome:1.1.1")
                implementation("br.com.devsrsouza.compose.icons:feather:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.3")
                implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
                implementation("com.darkrockstudios:mpfilepicker:3.1.0")
                implementation("net.pwall.mustache:kotlin-mustache:0.12")
                implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
                implementation("io.ktor:ktor-client-cio:3.0.0")
                implementation("io.ktor:ktor-client-core:3.0.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
                implementation(files("../../CodeEditor/ComposeTextEditor/build/libs/ComposeTextEditor-desktop-1.0.0.jar"))
                kotlin.srcDir(layout.buildDirectory.dir("generated/version"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("net.java.dev.jna:jna:5.9.0")
                implementation("net.java.dev.jna:jna-platform:5.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("org.jcodec:jcodec:0.2.5")
                implementation("org.jcodec:jcodec-javase:0.2.5")
            }
        }
    }
}


compose.desktop {

    application {
        mainClass = "at.crowdware.nocodedesigner.MainKt"

        nativeDistributions {
            modules("jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "NoCodeDesigner"
            packageVersion = "$version"
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icons/LinuxIcon.png"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icons/WindowsIcon.ico"))
                // Desktop-Shortcut hinzufügen
                menuGroup = "NoCodeDesigner" // Optional: Gruppierung im Startmenü
                shortcut = true // Aktiviert den Desktop-Shortcut

            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icons/icon.icns"))
                bundleID = "at.crowdware.nocodedesigner.desktopApp"
            }
        }
    }
}

tasks.named("assemble") {
    mustRunAfter("generateConstantsFile")
}

tasks.named("build") {
    dependsOn("generateConstantsFile")
}

tasks.named("compileKotlinDesktop") {
    dependsOn("generateConstantsFile")
}

tasks.register("generateConstantsFile") {
    val outputDir = layout.buildDirectory.dir("generated/version").get().asFile
    val versionValue = version

    inputs.property("version", versionValue)
    outputs.dir(outputDir)

    doLast {
        val versionFile = outputDir.resolve("Constants.kt")
        versionFile.parentFile.mkdirs()
        versionFile.writeText("""
            package at.crowdware.nocodelib

            object Version {
                const val version = "$versionValue"
            }
        """.trimIndent())
        println("Version changed to: $versionValue")
    }
}
