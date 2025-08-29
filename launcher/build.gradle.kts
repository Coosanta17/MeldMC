import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

project.version = "0.0.2"
val isRelease = false

val mainClassName = "net.coosanta.meldmc.Main"

plugins {
    java

    application

    `maven-publish`

    id("org.openjfx.javafxplugin") version "0.1.0"

    id("com.gradleup.shadow") version "8.3.6"
}

buildscript {
    repositories {
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
}
apply(plugin = "org.openjfx.javafxplugin")

javafx {
    version = "21.0.1"
    modules(
        "javafx.controls",
        "javafx.graphics",
        "javafx.base",
        "javafx.media",
        "javafx.fxml"
    )
}

repositories {
    mavenCentral()

    // For NBT and Minecraft
//    maven {
//        url = uri("https://libraries.minecraft.net")
//    }
    maven {
        url = uri("https://jitpack.io/")
    }
    maven {
        url = uri("https://maven.fabricmc.net/")
    }
    maven {
        url = uri("https://repo.opencollab.dev/maven-snapshots/")
    }
    maven {
        url = uri("https://repo.opencollab.dev/maven-releases/")
    }
    maven {
        url = uri("https://nexus.gluonhq.com/nexus/content/repositories/releases/")
    }
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // jitpack
    implementation("com.github.Querz:NBT:6.1")

//    // Mapping reader
//    implementation("net.fabricmc:mapping-io:0.7.1")
//
//    // https://mvnrepository.com/artifact/com.mojang/logging
//    implementation("com.mojang:logging:1.5.10")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.16")

//    implementation("org.geysermc.mcprotocollib:protocol:1.21.5-SNAPSHOT")

    // https://repo.opencollab.dev/maven-snapshots/ dependencies: https://repo.opencollab.dev/maven-releases/
    implementation("org.geysermc.mcprotocollib:protocol:1.21.6-SNAPSHOT")

    // https://mvnrepository.com/artifact/net.kyori/adventure-text-minimessage/4.19.0
    implementation("net.kyori:adventure-text-minimessage:4.19.0")

    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.1")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    implementation("com.fasterxml.jackson.core:jackson-core:2.19.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = mainClassName
}

val supportedPlatforms = listOf(
    "win",
    "mac",
    "mac-aarch64",
    "linux"
)

// Append `-Pplatform=<win|mac|mac-aarch64|linux>` if needed. No support for win-aarch64 or linux-aarch64 yet.
tasks.named<ShadowJar>("shadowJar") {
    val configuredBuildPlatform: String = project.findProperty("platform") as? String ?: javafx.platform.classifier

    if (configuredBuildPlatform !in supportedPlatforms) {
        throw IllegalArgumentException("Unsupported platform: $configuredBuildPlatform. Supported platforms are: $supportedPlatforms")
    }

    javafx.setPlatform(configuredBuildPlatform)

    group = "build"

    archiveClassifier.set(configuredBuildPlatform)

    doFirst {
        println("Building for $configuredBuildPlatform")
    }

    dependencies {
        include { it.moduleGroup == "org.openjfx" }
    }

    manifest {
        attributes["Main-Class"] = mainClassName
    }

    archiveFileName.set("meldmc-loader-${project.version}-$configuredBuildPlatform-javafx.jar")
}

val gradlew =
    if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("windows")) "gradlew.bat" else "./gradlew"

supportedPlatforms.forEach { platform ->
    tasks.register<Exec>("shadowJar_${platform.replace("-", "_")}") {
        group = "build"
        workingDir = rootProject.projectDir
        commandLine(gradlew, ":launcher:shadowJar", "-Pplatform=$platform")
    }
}

tasks.register("shadowJarAllPlatforms") {
    group = "build"
    dependsOn(supportedPlatforms.map { "shadowJar_${it.replace("-", "_")}" })
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = mainClassName
    }
    archiveFileName.set("meldmc-loader-${project.version}.jar")
}

publishing {
    repositories {
        maven {
            name = "snapshot"
            url = uri("https://repo.coosanta.net/snapshots")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
        maven {
            name = "releases"
            url = uri("https://repo.coosanta.net/releases")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenOnePlatform") {
            groupId = "net.coosanta"
            artifactId = "meldmc"
            version = project.version.toString()

            from(components["shadow"])

            val currentPlatform = project.findProperty("platform") as? String ?: javafx.platform.classifier
            artifact(
                layout.buildDirectory.file("versions/client-$currentPlatform.json").get().asFile
            ) {
                classifier = "client-$currentPlatform"
                extension = "json"
            }
        }
        create<MavenPublication>("mavenAllPlatforms") {
            groupId = "net.coosanta"
            artifactId = "meldmc"
            version = project.version.toString()

            supportedPlatforms.forEach { platform ->
                artifact(
                    layout.buildDirectory.file("libs/meldmc-loader-${project.version}-$platform-javafx.jar")
                        .get().asFile
                ) {
                    classifier = platform
                }

                artifact(
                    layout.buildDirectory.file("versions/client-$platform.json").get().asFile
                ) {
                    classifier = "client-$platform"
                    extension = "json"
                }
            }
        }
    }
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("generateLauncherJsons", "shadowJarAllPlatforms")
}

tasks.register("generateLauncherJsons") {
    dependsOn("shadowJarAllPlatforms")
    doLast {
        val version = project.version.toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'").format(Date())

        val seen = mutableSetOf<String>()
        val librariesJson = mutableListOf<Map<String, Any>>()

        val artifacts = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
        artifacts.forEach { artifact ->
            val group = artifact.moduleVersion.id.group
            val name = artifact.moduleVersion.id.name
            val projectVersion = artifact.moduleVersion.id.version
            val id = "$group:$name:$projectVersion"

            val excludedGroups = setOf(
                "ch.qos.logback",
                "org.openjfx",
                "io.netty"
            )

            // Special handling for mcprotocollib
            if (group == "org.geysermc.mcprotocollib" && name == "protocol" && projectVersion == "1.21.6-SNAPSHOT") {
                val url = getArtifactUrl(artifact)
                if (url == "https://repo.opencollab.dev/maven-snapshots/org/geysermc/mcprotocollib/protocol/1.21.6-SNAPSHOT/protocol-1.21.6-SNAPSHOT.jar") {
                    librariesJson.add(staticMcprotocollibJson())
                    return@forEach
                }
            }

            if (seen.add(id) && excludedGroups.none { group.startsWith(it) } && !name.startsWith("javafx-")) {
                librariesJson.add(
                    buildLibraryEntry(
                        artifact.file,
                        group,
                        name,
                        projectVersion,
                        getArtifactUrl(artifact)
                    )
                )
            }
        }

        supportedPlatforms.forEach { platform ->
            val localJar = layout.buildDirectory.file("libs/meldmc-loader-$version-$platform-javafx.jar").get().asFile
            val libsJson = librariesJson.toMutableList().also {
                it.add(
                    buildLibraryEntry(
                        localJar,
                        "net.coosanta",
                        "meldmc",
                        version,
                        "${getCoosantaRepoBase()}/net/coosanta/meldmc/$version/meldmc-$version-$platform.jar"
                    )
                )
            }

            val launcherJson = mapOf(
                "inheritsFrom" to "1.21.4",
                "id" to "meldmc-$version",
                "javaVersion" to mapOf("component" to "java-runtime-delta", "majorVersion" to 21),
                "libraries" to libsJson,
                "mainClass" to "net.coosanta.meldmc.Main",
//                "minimumLauncherVersion" to 21,
                "releaseTime" to timestamp,
                "time" to timestamp,
                "type" to "release"
            )

            val out = layout.buildDirectory.file("versions/client-$platform.json").get().asFile
            out.parentFile.mkdirs()
            out.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(launcherJson)))
            println("Generated client JSON for $platform at: ${out.absolutePath}")
        }
    }
}

// Add this helper function for static mcprotocollib JSON
fun staticMcprotocollibJson(): Map<String, Any> = mapOf(
    "name" to "org.geysermc.mcprotocollib:protocol:1.21.6-SNAPSHOT",
    "downloads" to mapOf(
        "artifact" to mapOf(
            "path" to "org/geysermc/mcprotocollib/protocol/1.21.6-SNAPSHOT/protocol-1.21.6-SNAPSHOT.jar",
            "sha1" to "ada983d6983a46f3b78318849f0641252b4fbabc",
            "size" to 1350227,
            "url" to "https://repo.opencollab.dev/maven-snapshots/org/geysermc/mcprotocollib/protocol/1.21.6-SNAPSHOT/protocol-1.21.6-20250702.161144-8.jar"
        )
    )
)

fun buildLibraryEntry(
    file: File,
    group: String,
    name: String,
    version: String,
    url: String
): Map<String, Any> {
    val path = "${group.replace('.', '/')}/$name/$version/$name-$version.jar"
    val sha1 = calculateSha1(file)
    val size = file.length()

    return mapOf(
        "name" to "$group:$name:$version",
        "downloads" to mapOf(
            "artifact" to mapOf(
                "path" to path,
                "sha1" to sha1,
                "size" to size,
                "url" to url
            )
        ),
    )
}

fun calculateSha1(file: File): String {
    val md = MessageDigest.getInstance("SHA-1")
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } > 0) {
            md.update(buffer, 0, read)
        }
    }
    return md.digest().joinToString("") { "%02x".format(it) }
}

fun getArtifactUrl(artifact: ResolvedArtifact): String {
    val group = artifact.moduleVersion.id.group
    val name = artifact.moduleVersion.id.name
    val version = artifact.moduleVersion.id.version
    val path = "${group.replace('.', '/')}/$name/$version/$name-$version.jar"

    for (repository in project.repositories) {
        if (repository is MavenArtifactRepository) {
            val baseUrl = repository.url.toString().removeSuffix("/")
            val jarUrl = "$baseUrl/$path"

            try {
                val connection = URI(jarUrl).toURL().openConnection()
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.connect()
                connection.inputStream.close()

                println("Found ${group}:${name}:${version} at $jarUrl")
                return jarUrl
            } catch (_: Exception) {
                continue
            }
        }
    }

    // fall back to pattern matching - stupid approach but ok.
    val baseUrl = when {
        // JavaFX modules - Maven Central
        group.startsWith("org.openjfx") -> "https://repo1.maven.org/maven2"

        // Geyser MC Protocol - OpenCollab snapshots
        group.startsWith("org.geysermc") -> "https://repo.opencollab.dev/maven-snapshots"

        // JitPack dependencies
        group.startsWith("com.github") -> "https://jitpack.io"

        // Fabric dependencies
        group.startsWith("net.fabricmc") -> "https://maven.fabricmc.net"

        // Kyori Adventure - Maven Central
        group.startsWith("net.kyori") -> "https://repo1.maven.org/maven2"

        // Logback - Maven Central
        group.startsWith("ch.qos.logback") -> "https://repo1.maven.org/maven2"

        // SLF4J - Maven Central
        group.startsWith("org.slf4j") -> "https://repo1.maven.org/maven2"

        // Jackson - Maven Central
        group.startsWith("com.fasterxml.jackson") -> "https://repo1.maven.org/maven2"

        // Guava - Maven Central
        group.startsWith("com.google.guava") -> "https://repo1.maven.org/maven2"

        // aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
        group.startsWith("net.coosanta") -> "https://repo.coosanta.net/releases"

        // Stupid
        else -> "https://repo1.maven.org/maven2"
    }

    val fallbackUrl = "$baseUrl/$path"
    println("Using fallback URL for ${group}:${name}:${version}: $fallbackUrl")
    return fallbackUrl
}

// Helper to get coosanta repo base URL
fun getCoosantaRepoBase(): String =
    if (isRelease) "https://repo.coosanta.net/releases" else "https://repo.coosanta.net/snapshots"
