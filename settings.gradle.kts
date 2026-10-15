pluginManagement {
    repositories {
        mavenCentral()
        maven(url = "https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven(url = "https://maven.kikugie.dev/releases") {
            name = "KikuGie"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        version("1.20.1-fabric", "1.20.1").buildscript("build.fabric.gradle.kts")
        vcsVersion = "1.20.1-fabric"
    }
}

rootProject.name = "Ledger"
