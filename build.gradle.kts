import java.io.FileInputStream
import java.util.*

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.1"
}

group = "pl.digsa"
version = "1.0-SNAPSHOT"

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("key.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.4")
    type.set("IC") // Target IDE Platform

    plugins.set(
        listOf(
            "Dart:222.4582",
            "com.localizely.flutter-intl:1.18.4-2022.2",
            "org.jetbrains.plugins.yaml:222.3739.67"
        )
    )
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(keystoreProperties["certificateChain"] as String)
        privateKey.set(keystoreProperties["privateKey"] as String)
        password.set(keystoreProperties["privateKeyPassword"] as String)
    }

    publishPlugin {
        token.set(keystoreProperties["publishToken"] as String)
    }
}
