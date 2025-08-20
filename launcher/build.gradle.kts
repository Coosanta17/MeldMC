import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.json.JsonOutput
import java.net.URI
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

project.version = "0.0.1e"
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
    version = "21.0.2"
    modules(
        "javafx.controls",
        "javafx.graphics",
        "javafx.base",
        "javafx.media",
        "javafx.fxml"
    )
}

repositories {
    // Use Maven Central for resolving dependencies.
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
    "linux",
    "linux-aarch64"
)

// Append `-Pplatform=<win|mac|mac-aarch64|linux|linux-aarch64>` if needed. No support for win-aarch64 yet.
tasks.named<ShadowJar>("shadowJar") {
    val configuredBuildPlatform: String = project.findProperty("platform") as? String
        ?: javafx.platform.classifier

    if (configuredBuildPlatform !in supportedPlatforms) {
        throw IllegalArgumentException("Unsupported platform: $configuredBuildPlatform. Supported platforms are: $supportedPlatforms")
    }

    javafx.setPlatform(configuredBuildPlatform)

    group = "build"

    archiveClassifier.set(configuredBuildPlatform)

    doFirst {
        println("Building for $configuredBuildPlatform")
    }


// TODO: Use libraries instead of packaging everything into one

    dependencies {
        include { it.moduleGroup == "org.openjfx" }
    }

//    minimize()

    manifest {
        attributes["Main-Class"] = mainClassName
    }

    archiveFileName.set("meld-loader-${project.version}-$configuredBuildPlatform-javafx.jar")
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
        create<MavenPublication>("mavenSnapshot") {
            val platform = (project.findProperty("platform") as? String)
                ?: javafx.platform.classifier

            groupId = "net.coosanta"
            artifactId = "meldmc"
            version = project.version.toString()

            from(components["shadow"])
        }
    }
}

tasks.register("generateLauncherJson") {
    doLast {
        val libraries = mutableListOf<String>()
        val librariesJson = mutableListOf<Map<String, Any>>()

        // Get all resolved configurations
        project.configurations.filter { config ->
            // Only consider configurations that can be resolved
            config.isCanBeResolved
        }.forEach { config ->
            try {
                config.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                    val group = artifact.moduleVersion.id.group
                    val name = artifact.moduleVersion.id.name
                    val version = artifact.moduleVersion.id.version
                    val libraryName = "$group:$name:$version"

                    if (!libraries.contains(libraryName)) {
                        libraries.add(libraryName)

                        val file = artifact.file
                        val path = "${group.replace('.', '/')}/$name/$version/$name-$version.jar"
                        val sha1 = calculateSha1(file)
                        val size = file.length()

                        val url = getArtifactUrl(artifact)

                        val libraryJson = mapOf(
                            "downloads" to mapOf(
                                "artifact" to mapOf(
                                    "path" to path,
                                    "sha1" to sha1,
                                    "size" to size,
                                    "url" to url
                                )
                            ),
                            "name" to libraryName
                        )

                        librariesJson.add(libraryJson)
                    }
                }
            } catch (e: Exception) {
                println("Couldn't resolve configuration ${config.name}: ${e.message}")
            }
        }

        // Build the complete launcher JSON
        val version = project.version.toString()
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'").format(Date())

        val gameArguments = listOf(
            "--username",
            "\${auth_player_name}",
            "--version",
            "\${version_name}",
            "--gameDir",
            "\${game_directory}",
            "--assetsDir",
            "\${assets_root}",
            "--assetIndex",
            "\${assets_index_name}",
            "--uuid",
            "\${auth_uuid}",
            "--accessToken",
            "\${auth_access_token}",
            "--clientId",
            "\${clientid}",
            "--xuid",
            "\${auth_xuid}",
            "--userType",
            "\${user_type}",
            "--versionType",
            "\${version_type}",
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "is_demo_user" to true
                        )
                    )
                ),
                "value" to "--demo"
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "has_custom_resolution" to true
                        )
                    )
                ),
                "value" to listOf(
                    "--width",
                    "\${resolution_width}",
                    "--height",
                    "\${resolution_height}"
                )
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "has_quick_plays_support" to true
                        )
                    )
                ),
                "value" to listOf(
                    "--quickPlayPath",
                    "\${quickPlayPath}"
                )
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "is_quick_play_singleplayer" to true
                        )
                    )
                ),
                "value" to listOf(
                    "--quickPlaySingleplayer",
                    "\${quickPlaySingleplayer}"
                )
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "is_quick_play_multiplayer" to true
                        )
                    )
                ),
                "value" to listOf(
                    "--quickPlayMultiplayer",
                    "\${quickPlayMultiplayer}"
                )
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "features" to mapOf(
                            "is_quick_play_realms" to true
                        )
                    )
                ),
                "value" to listOf(
                    "--quickPlayRealms",
                    "\${quickPlayRealms}"
                )
            )
        )

        val jvmArguments = listOf(
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "os" to mapOf(
                            "name" to "osx"
                        )
                    )
                ),
                "value" to listOf(
                    "-XstartOnFirstThread"
                )
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "os" to mapOf(
                            "name" to "windows"
                        )
                    )
                ),
                "value" to "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
            ),
            mapOf(
                "rules" to listOf(
                    mapOf(
                        "action" to "allow",
                        "os" to mapOf(
                            "arch" to "x86"
                        )
                    )
                ),
                "value" to "-Xss1M"
            ),
            "-Djava.library.path=\${natives_directory}",
            "-Djna.tmpdir=\${natives_directory}",
            "-Dorg.lwjgl.system.SharedLibraryExtractPath=\${natives_directory}",
            "-Dio.netty.native.workdir=\${natives_directory}",
            "-Dminecraft.launcher.brand=\${launcher_name}",
            "-Dminecraft.launcher.version=\${launcher_version}",
            "-cp",
            "\${classpath}"
        )

        val launcherJson = mapOf(
            "arguments" to mapOf(
                "game" to gameArguments,
                "jvm" to jvmArguments
            ),
            "assetIndex" to mapOf(
                "id" to "26", // FIXME
                "sha1" to "placeholder_sha1",
                "size" to 0,
                "totalSize" to 0,
                "url" to "placeholder_url"
            ),
            "assets" to "26", // Placeholder
            "complianceLevel" to 1,
            "downloads" to mapOf(
                "client" to mapOf(
                    "sha1" to "placeholder_sha1",
                    "size" to 0,
                    "url" to "https://repo.coosanta.net/snapshots/net/coosanta/meldmc/$version/meldmc-$version.jar"
                )
            ),
            "id" to version,
            "javaVersion" to mapOf(
                "component" to "java-runtime-delta",
                "majorVersion" to 21
            ),
            "libraries" to librariesJson,
            "logging" to mapOf(
                "client" to mapOf(
                    "argument" to "-Dlog4j.configurationFile=\${path}",
                    "file" to mapOf(
                        "id" to "client-log4j2.xml",
                        "sha1" to "placeholder_sha1",
                        "size" to 0,
                        "url" to "placeholder_url"
                    ),
                    "type" to "log4j2-xml"
                )
            ),
            "mainClass" to "net.minecraft.client.main.Main", // Update this as needed
            "minimumLauncherVersion" to 21,
            "releaseTime" to currentTime,
            "time" to currentTime,
            "type" to "release"
        )

        // Write JSON to file
        val gson = JsonOutput.prettyPrint(JsonOutput.toJson(launcherJson))
        val outputFile = File("$buildDir/versions/client.json")
        outputFile.parentFile.mkdirs()
        outputFile.writeText(gson)

        println("Client JSON generated at: ${outputFile.absolutePath}")
    }
}

// Helper function to calculate SHA1 hash
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
        group.startsWith("net.coosanta") -> "https://repo.coosanta.net/snapshots"

        // Stupid
        else -> "https://repo1.maven.org/maven2"
    }

    val fallbackUrl = "$baseUrl/$path"
    println("Using fallback URL for ${group}:${name}:${version}: $fallbackUrl")
    return fallbackUrl
}
