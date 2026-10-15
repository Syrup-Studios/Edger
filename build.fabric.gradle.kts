import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("fabric-loom") version "1.14.+"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.jakemarsden.git-hooks") version "0.0.2"
    `maven-publish`
}

val modId: String by project
val modName: String by project
val modVersion: String by project
val mavenGroup: String by project

base.archivesName.set(modId)
version = modVersion
group = mavenGroup

sourceSets {
    create("testmod") {
        runtimeClasspath += sourceSets["main"].runtimeClasspath
        compileClasspath += sourceSets["main"].compileClasspath
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    //serverOnlyMinecraftJar()

    runs {
        create("testmodClient") {
            client()
            ideConfigGenerated(true)
            name("Networking Testmod (Client)")
            source(sourceSets.getByName("testmod"))
        }
    }
}

fun DependencyHandlerScope.modImplementationAndInclude(dep: Any) {
    modImplementation(dep)
    include(dep)
}

repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.nucleoid.xyz/")
    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenCentral()
    mavenLocal()
}

val includeImplementation: Configuration by configurations.creating {
    configurations.implementation.configure { extendsFrom(this@creating) }
}

dependencies {
    // Dependency versions are in stonecutter.properties.yaml.

    // Fabric
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    // Permissions
    modImplementationAndInclude("me.lucko:fabric-permissions-api:${property("deps.fabric_permissions")}")

    // Translations
    modImplementationAndInclude("xyz.nucleoid:server-translations-api:${property("deps.translations")}")

    // Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_kotlin")}")

    // Database
    includeImplementation("org.jetbrains.exposed:exposed-core:${property("deps.exposed")}")
    includeImplementation("org.jetbrains.exposed:exposed-dao:${property("deps.exposed")}")
    includeImplementation("org.jetbrains.exposed:exposed-java-time:${property("deps.exposed")}")
    includeImplementation("org.jetbrains.exposed:exposed-jdbc:${property("deps.exposed")}")
    includeImplementation("org.xerial:sqlite-jdbc:${property("deps.sqlite_jdbc")}")

    // Config
    includeImplementation("com.uchuhimo:konf-core:${property("deps.konf")}")
    includeImplementation("com.uchuhimo:konf-toml:${property("deps.konf")}")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${property("deps.detekt")}")
}

tasks {
    processResources {
        val values = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to version,
            "fabricApi" to project.property("deps.fabric_api"),
            "fabricLoader" to project.property("deps.fabric_loader"),
            "fabricKotlin" to project.property("deps.fabric_kotlin"),
            "minecraft" to stonecutter.current.version,
        )

        inputs.properties(values)
        filesMatching("fabric.mod.json") {
            expand(values)
        }
    }

    jar {
        from(rootProject.file("LICENSE.md"))
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    // select the repositories you want to publish to
    repositories {
        // mavenLocal()
    }
}

detekt {
    buildUponDefaultConfig = true
    autoCorrect = true
    config.setFrom(rootProject.files("detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = JavaVersion.VERSION_21.toString()
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "detekt")
    )
}

afterEvaluate {
    dependencies {
        handleIncludes(includeImplementation)
    }
}

/* Thanks to https://github.com/jakobkmar for original script */
fun DependencyHandlerScope.includeTransitive(
    dependencies: Set<ResolvedDependency>,
    minecraftLibs: Set<ResolvedDependency>,
    kotlinDependency: ResolvedDependency,
    checkedDependencies: MutableSet<ResolvedDependency> = HashSet()
) {
    dependencies.forEach {
        if (checkedDependencies.contains(it) || it.moduleGroup == "org.jetbrains.kotlin" || it.moduleGroup == "org.jetbrains.kotlinx") return@forEach

        if (kotlinDependency.children.any { dep -> dep.name == it.name }) {
            println("Skipping -> ${it.name} (already in fabric-language-kotlin)")
        } else if (minecraftLibs.any { dep -> dep.moduleGroup == it.moduleGroup && dep.moduleName == it.moduleName }) {
            println("Skipping -> ${it.name} (already in minecraft)")
        } else {
            include(it.name)
            println("Including -> ${it.name}")
        }
        checkedDependencies += it

        includeTransitive(it.children, minecraftLibs, kotlinDependency, checkedDependencies)
    }
}

fun DependencyHandlerScope.handleIncludes(configuration: Configuration) {
    includeTransitive(
        configuration.resolvedConfiguration.firstLevelModuleDependencies,
        configurations.minecraftLibraries.get().resolvedConfiguration.firstLevelModuleDependencies,
        configurations.modImplementation.get().resolvedConfiguration.firstLevelModuleDependencies
            .first { it.moduleGroup == "net.fabricmc" && it.moduleName == "fabric-language-kotlin" },
    )
}
