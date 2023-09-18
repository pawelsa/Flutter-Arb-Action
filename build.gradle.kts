plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin
    id("org.jetbrains.intellij") version Versions.intellij
}

repositories {
    mavenCentral()
}

group = ModuleConfig.group
version = ModuleConfig.version

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set(Versions.Intellij.version)
    type.set(Versions.Intellij.type) // Target IDE Platform

    plugins.set(ModuleConfig.IntellijPlugins.plugins)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = Versions.java
    }

    patchPluginXml {
        sinceBuild.set(Versions.Intellij.sinceBuild)
        untilBuild.set(Versions.Intellij.untilBuild)
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
