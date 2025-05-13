import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.*

project.version = "0.0.1d"

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
    dependencies {
        classpath("org.openjfx:javafx-plugin:0.1.0")
    }
}
apply(plugin = "org.openjfx.javafxplugin")

javafx {
    modules("javafx.controls", "javafx.fxml")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()

    // For NBT and Minecraft
    maven {
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        url = uri("https://jitpack.io/")
    }
    maven {
        url = uri("https://maven.fabricmc.net/")
    }
//    maven {
//        url = uri("https://repo.opencollab.dev/maven-snapshots/")
//    }
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

    // NBT for server.dat
    implementation("com.github.Querz:NBT:6.1")

//    // Mapping reader
//    implementation("net.fabricmc:mapping-io:0.7.1")
//
//    // https://mvnrepository.com/artifact/com.mojang/logging
//    implementation("com.mojang:logging:1.5.10")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.16")

//    implementation("org.geysermc.mcprotocollib:protocol:1.21.5-SNAPSHOT")

    implementation("com.github.GeyserMC:MCProtocolLib:1.21.4-1")

    implementation("net.kyori:adventure-text-minimessage:4.19.0")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "net.coosanta.meldmc.Main"
}

val platforms = listOf("win", "mac", "linux")

platforms.forEach { platform ->
    tasks.register<ShadowJar>("shadowJar${platform.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}") {
        archiveClassifier.set(platform)
        group = "build"

        doFirst {
            javafx.setPlatform(platform)
        }

        // Includes only JavaFX modules in the shadow jar
        dependencies {
            include { dependency ->
                dependency.moduleGroup == "org.openjfx"
            }
        }

        manifest {
            attributes["Main-Class"] = "net.coosanta.meldmc.Main"
        }

        archiveFileName.set("meld-loader-${project.version}-$platform-javafx.jar")
    }
}


tasks.named<ShadowJar>("shadowJar") {
    val currentPlatform = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "mac"
        org.gradle.internal.os.OperatingSystem.current().isLinux -> "linux"
        else -> throw GradleException("Unsupported platform")
    }
    group = "build"
    archiveClassifier.set(currentPlatform)

    doFirst {
        javafx.setPlatform(currentPlatform)
    }

    dependencies {
        include { dependency ->
            dependency.moduleGroup == "org.openjfx"
        }
    }

    manifest {
        attributes["Main-Class"] = "net.coosanta.meldmc.Main"
    }

    archiveFileName.set("meld-loader-${project.version}-$currentPlatform-javafx.jar")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "net.coosanta.meldmc.Main"
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
