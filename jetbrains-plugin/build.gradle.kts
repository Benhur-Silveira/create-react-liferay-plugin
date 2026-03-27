plugins {
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

import org.gradle.api.tasks.Copy

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion")
        )
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = "241"
            untilBuild = "261.*"
        }
    }
}

kotlin {
    jvmToolchain(21)
}

val vendorEngine by tasks.registering(Copy::class) {
    val rootDirPath = rootProject.projectDir.parentFile.toPath()
    val source = rootDirPath.resolve("node_modules/@liferay-react").toFile()
    from(source)
    into(layout.buildDirectory.dir("generated/runner/node_modules/@liferay-react"))
}

val copyRunnerScripts by tasks.registering(Copy::class) {
    from("src/main/resources/runner")
    into(layout.buildDirectory.dir("generated/runner"))
}

tasks.named("prepareSandbox") {
    dependsOn(vendorEngine, copyRunnerScripts)
    doLast {
        val generatedRunner = layout.buildDirectory.dir("generated/runner").get().asFile
        val sandboxRoot = layout.buildDirectory.dir("idea-sandbox").get().asFile
        val expectedDirs = listOf(
            sandboxRoot.resolve("plugins/${rootProject.name}"),
            sandboxRoot.resolve("plugins/${providers.gradleProperty("pluginName").get()}")
        )

        val dynamicPluginDirs = sandboxRoot
            .listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.resolve("plugins") }
            ?.filter { it.isDirectory }
            ?.flatMap { pluginsDir ->
                pluginsDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
            }
            ?: emptyList()

        (expectedDirs + dynamicPluginDirs).distinct().forEach { pluginDir ->
            copy {
                from(generatedRunner)
                into(pluginDir.resolve("modules/runner"))
            }
        }
    }
}

