import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

project.version = "0.0.1d"
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
    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.media", "javafx.fxml")
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = mainClassName
}

val supportedPlatforms: () -> List<String> = {
    listOf(
        "win",
        "mac",
        "mac-aarch64",
        "linux",
        "linux-aarch64"
    )
}

// Append `-Pplatform=<win|mac|mac-aarch64|linux|linux-aarch64>` if needed. No support for win-aarch64 yet.
tasks.named<ShadowJar>("shadowJar") {
    val configuredBuildPlatform: String = project.findProperty("platform") as? String
        ?: javafx.platform.classifier

    if (configuredBuildPlatform !in supportedPlatforms()) {
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
        create<MavenPublication>("maven") {
            groupId = "net.coosanta"
            artifactId = "meldmc"
            version = project.version.toString()
            from(components["java"])
        }
    }
}
