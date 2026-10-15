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

stonecutter.properties(file("stonecutter.properties.yaml"))

val pluginDefaults = settings.getPluginManagement().getPlugins()
pluginDefaults.id("org.jetbrains.kotlin.jvm").version(stonecutter.properties.get<String>("plugins.kotlin"))
pluginDefaults.id("fabric-loom").version(stonecutter.properties.get<String>("plugins.fabric_loom"))
pluginDefaults.id("me.modmuss50.mod-publish-plugin").version(stonecutter.properties.get<String>("plugins.mod_publish"))

stonecutter {
    create(rootProject) {
        val minecraftVersion: String = stonecutter.properties["minecraft.version"]
        version("$minecraftVersion-fabric", minecraftVersion).buildscript("build.fabric.gradle.kts")
        vcsVersion = "$minecraftVersion-fabric"
    }
}

rootProject.name = stonecutter.properties["mod.name"]
