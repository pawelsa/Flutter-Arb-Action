import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin
    id("org.jetbrains.intellij.platform") version Versions.intellij
}

repositories {
    mavenCentral()
}

intellijPlatform {
    pluginConfiguration {
        id = ModuleConfig.id
        version = ModuleConfig.version

        ideaVersion {
            sinceBuild = Versions.Intellij.sinceBuild
            untilBuild = Versions.Intellij.untilBuild
        }
    }

    pluginVerification {
        cliPath = file("/Users/pawel/Downloads/verifier-cli-1.386-all.jar")
        ides {
            select {
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity, IntelliJPlatformType.AndroidStudio)
                sinceBuild = Versions.Intellij.sinceBuild
                untilBuild = Versions.Intellij.untilBuild
            }
        }
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }
}
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(Versions.Intellij.type, Versions.Intellij.version)

        bundledPlugin(ModuleConfig.IntellijPlugins.json)
        ModuleConfig.IntellijPlugins.plugins.forEach { (id, version) ->
            plugin(id, version)
        }

        jetbrainsRuntime()
    }
}