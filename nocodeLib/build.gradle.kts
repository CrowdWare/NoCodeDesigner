import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)

}

kotlin {
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    androidTarget {
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
            implementation("org.reflections:reflections:0.10.2")
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.uiTooling)
                implementation(libs.androidx.activityCompose)
                implementation("androidx.core:core-ktx:1.7.0")
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
            }

            resources.srcDir("src/androidMain/res")
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        getByName("commonMain") {
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
    }
}

android {
    namespace = "at.crowdware.nocodelib"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }

    // Referenziere das AndroidManifest.xml im Library-SourceSet
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
}

dependencies {
    androidTestImplementation(libs.androidx.uitest.junit4)
    debugImplementation(libs.androidx.uitest.testManifest)
    //temporary fix: https://youtrack.jetbrains.com/issue/CMP-5864
    androidTestImplementation("androidx.test:monitor") {
        version { strictly("1.6.1") }
    }
}

tasks.register<Copy>("copyAar") {
    // Task soll erst nach 'assemble' ausgeführt werden
    dependsOn("assemble")

    val fromPath = layout.buildDirectory.dir("outputs/aar/nocodeLib-debug.aar").get().asFile
    val toPath = layout.projectDirectory.dir("../../NoCodeBrowser/app/libs")
    println("Copying  $fromPath $toPath")
    from(fromPath)
    into(toPath)

    doLast {
        // Debug-Ausgaben
        println("Copying $fromPath $toPath")

        if (!fromPath.exists()) {
            throw GradleException(".aar file not found at")
        }
    }
}

// Ensure that 'copyAar' is executed after 'assemble'
tasks.named("assemble").configure {
    finalizedBy("copyAar")
}

tasks.withType<Test> {
    testLogging {
        events("passed", "failed", "skipped", "standardOut", "standardError")
        showStandardStreams = true
    }
}