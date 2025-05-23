rootProject.name = "NoCodeDesigner"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}


include(":composeApp")
include(":nocodelib")
project(":nocodelib").projectDir = file("nocodelib/composeApp")