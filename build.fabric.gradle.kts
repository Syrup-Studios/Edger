import groovy.json.JsonOutput
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    `maven-publish`
}

val modId = property("mod.id").toString()
val javaVersion = property("java.version").toString().toInt()
val uploadVersion = "${property("mod.version")}+${stonecutter.current.version}-fabric"

base.archivesName.set(modId)
version = property("mod.version").toString()
group = property("mod.group").toString()

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveVersion.set(uploadVersion)
}

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

fun jsonStringContent(value: Any?): String = JsonOutput.toJson(requireNotNull(value).toString()).let { it.substring(1, it.length - 1) }

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
}

tasks {
    processResources {
        val values = mapOf(
            "id" to jsonStringContent(modId),
            "name" to jsonStringContent(project.property("mod.name")),
            "version" to jsonStringContent(project.version),
            "description" to jsonStringContent(project.property("mod.description")),
            "author" to jsonStringContent(project.property("mod.author")),
            "license" to jsonStringContent(project.property("mod.license")),
            "homepage" to jsonStringContent(project.property("mod.homepage")),
            "sources" to jsonStringContent(project.property("mod.sources")),
            "issues" to jsonStringContent(project.property("mod.issues")),
            "entrypoint" to jsonStringContent(project.property("mod.entrypoint")),
            "icon" to jsonStringContent("assets/$modId/icon.png"),
            "mixins" to jsonStringContent("$modId.mixins.json"),
            "fabricApi" to jsonStringContent(">=${project.property("deps.fabric_api")}"),
            "fabricLoader" to jsonStringContent(">=${project.property("deps.fabric_loader")}"),
            "fabricKotlin" to jsonStringContent(">=${project.property("deps.fabric_kotlin")}"),
            "minecraft" to jsonStringContent(project.property("mod.fabric_mc_range")),
        )

        inputs.properties(values)
        filesMatching("fabric.mod.json") { expand(values) { escapeBackslash = true } }
    }
}

tasks.withType<Jar>().configureEach {
    from(rootProject.files("NOTICE", "LICENSE.md", "GPL-3.0.txt"))
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(
        tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile },
        tasks.named<net.fabricmc.loom.task.RemapSourcesJarTask>("remapSourcesJar").flatMap { it.archiveFile },
    )
    into(rootProject.layout.buildDirectory.dir("libs/${project.property("mod.version")}"))
    dependsOn("assemble")
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
            version = uploadVersion
        }
    }

    // select the repositories you want to publish to
    repositories {
        maven {
            name = project.property("publish.maven_name").toString()
            url = uri(project.property("publish.maven_url").toString())
            credentials(PasswordCredentials::class)
        }
    }
}

val modrinthToken = providers.gradleProperty("publish.modrinth_token")
    .orElse(providers.gradleProperty("MODRINTH_TOKEN"))
    .orElse(providers.environmentVariable("MODRINTH_TOKEN"))
val curseForgeToken = providers.gradleProperty("publish.curseforge_token")
    .orElse(providers.gradleProperty("CURSEFORGE_TOKEN"))
    .orElse(providers.environmentVariable("CURSEFORGE_TOKEN"))
val modrinthId = property("publish.modrinth").toString().trim()
val curseForgeId = property("publish.curseforge").toString().trim()
val publishDryRun = providers.gradleProperty("publish.dry_run").map {
    when (it) {
        "true" -> true
        "false" -> false
        else -> throw GradleException("publish.dry_run must be true or false")
    }
}.orElse(!modrinthToken.isPresent || !curseForgeToken.isPresent)
val compatibleVersions = stonecutter.properties.rawOrNull("mod.mc_releases")
    ?.asList().orEmpty().map { it.toString() }

publishMods {
    file = tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar").flatMap { it.archiveFile }
    dryRun.set(publishDryRun)
    version = uploadVersion
    displayName = "${property("mod.name")} ${project.version} - Fabric ${stonecutter.current.version}"
    changelog = providers.fileContents(rootProject.layout.projectDirectory.file(property("publish.changelog_file").toString())).asText
    type = when (property("publish.release_type").toString().lowercase()) {
        "stable" -> STABLE
        "beta" -> BETA
        "alpha" -> ALPHA
        else -> throw GradleException("publish.release_type must be stable, beta, or alpha")
    }
    modLoaders.add("fabric")

    modrinth {
        projectId = modrinthId.ifBlank { "00000000" }
        accessToken = modrinthToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        environment = SERVER_ONLY
        requires("fabric-api")
        requires("fabric-language-kotlin")
    }
    curseforge {
        projectId = curseForgeId.ifBlank { "0" }
        accessToken = curseForgeToken
        compatibleVersions.forEach { minecraftVersions.add(it) }
        client = false
        server = true
        requires("fabric-api")
        requires("fabric-language-kotlin")
    }
}

tasks.matching { it.name == "publishModrinth" || it.name == "publishCurseforge" }.configureEach {
    doFirst {
        if (!publishDryRun.get()) {
            if (name == "publishModrinth") {
                check(modrinthId.isNotBlank()) { "Set publish.modrinth in stonecutter.properties.yaml before live publishing" }
                check(modrinthToken.isPresent && modrinthToken.get().isNotBlank()) {
                    "Set MODRINTH_TOKEN or publish.modrinth_token before live publishing"
                }
            } else {
                check(curseForgeId.isNotBlank()) { "Set publish.curseforge in stonecutter.properties.yaml before live publishing" }
                check(curseForgeToken.isPresent && curseForgeToken.get().isNotBlank()) {
                    "Set CURSEFORGE_TOKEN or publish.curseforge_token before live publishing"
                }
            }
        }
    }
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
