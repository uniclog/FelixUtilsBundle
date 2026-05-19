plugins {
    id("java")
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.4"
}

intellij {
    version.set(project.findProperty("platformVersion")?.toString() ?: "2023.3.2")
}

group = "io.github.uniclog"
version = "1.0.1"

val platformVersion: String = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"

val javaVersion: JavaVersion = when {
    platformVersion.startsWith("2021") -> JavaVersion.VERSION_11
    platformVersion.startsWith("2023") -> JavaVersion.VERSION_17
    platformVersion.startsWith("2026") -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

val buildNumber: String = when {
    platformVersion.startsWith("2021") -> "212"
    platformVersion.startsWith("2026") -> "261"
    else -> "232"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.intellij")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    intellij {
        version.set(platformVersion)
        type.set("IU")
        downloadSources.set(true)
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
        }
    }

    tasks {
        patchPluginXml {
            sinceBuild.set(buildNumber)
            untilBuild.set("999.*")
        }
        withType<JavaCompile> {
            sourceCompatibility = javaVersion.toString()
            targetCompatibility = javaVersion.toString()
        }
    }
}

tasks.register<Zip>("buildBundle") {
    group = "build"
    description = "Builds a bundle containing plugins"
    archiveFileName.set("plugins-bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))
    val projects = listOf("ankey-docker-runner", "config-deployer", "common-docs-markdown-plugin")
    projects.forEach { name ->
        val buildPluginTask = project(":$name").tasks.named("buildPlugin")
        dependsOn(buildPluginTask)
        from(buildPluginTask.map { zipTree(it.outputs.files.singleFile) })
    }
}
