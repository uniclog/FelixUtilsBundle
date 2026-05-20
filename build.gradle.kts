val platformVersion: String = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"

val kotlinVersion = when {
    platformVersion.startsWith("2024") || platformVersion.startsWith("2025") || platformVersion.startsWith("2026") -> "2.1.10"
    else -> "1.9.22"
}

plugins {
    id("java")
    kotlin("jvm") version "2.1.10" // Upgrade to 2.1.10 for better 2026 support
    id("org.jetbrains.intellij") version "1.17.4"
}

intellij {
    version.set(platformVersion)
}

group = "io.github.uniclog"
version = "1.0.1"

val javaVersion: JavaVersion = when {
    platformVersion.startsWith("2021") -> JavaVersion.VERSION_11
    platformVersion.startsWith("2022") || platformVersion.startsWith("2023") -> JavaVersion.VERSION_17
    platformVersion.startsWith("2024") || platformVersion.startsWith("2025") || platformVersion.startsWith("2026") -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

val buildNumber: String = when {
    platformVersion.startsWith("2021") -> "212"
    platformVersion.startsWith("2022") -> "222"
    platformVersion.startsWith("2024") -> "241"
    platformVersion.startsWith("2025") -> "251"
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

    // Export variables to subprojects
    project.extra["buildNumber"] = buildNumber
    project.extra["javaVersion"] = javaVersion
    project.extra["platformVersion"] = platformVersion

    intellij {
        version.set(platformVersion)
        type.set("IU")
        downloadSources.set(true)
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.majorVersion))
        }
        // Ensure Kotlin compiler version is compatible with the platform
        compilerOptions {
            if (kotlinVersion.startsWith("1.")) {
                // Fallback for older platforms if needed, though 2.0.21 is generally backward compatible
            }
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
        // Disable searchable options for 2024.2+ to avoid build failures
        if (platformVersion.startsWith("2024") || platformVersion.startsWith("2025") || platformVersion.startsWith("2026")) {
            buildSearchableOptions {
                enabled = false
            }
        }
    }
}

tasks.register("buildBundle") {
    group = "build"
    description = "Prepares a local plugin repository"
    val majorVersion = platformVersion.substringBefore(".")
    val bundleDir = layout.buildDirectory.dir("bundle/$majorVersion")

    val projects = listOf("ankey-docker-runner", "config-deployer", "common-docs-markdown-plugin")

    projects.forEach { name ->
        dependsOn(project(":$name").tasks.named("buildPlugin"))
    }

    doLast {
        val xmlFile = bundleDir.get().file("updatePlugins.xml").asFile
        bundleDir.get().asFile.mkdirs()

        val xmlContent = StringBuilder("<plugins>\n")

        projects.forEach { name ->
            val subProject = project(":$name")
            val buildPluginTask = subProject.tasks.named<org.jetbrains.intellij.tasks.BuildPluginTask>("buildPlugin").get()
            val zipFile = buildPluginTask.outputs.files.singleFile

            copy {
                from(zipFile)
                into(bundleDir)
            }

            val pluginId = when (name) {
                "ankey-docker-runner" -> "docker-runner-plugin"
                "common-docs-markdown-plugin" -> "common-docs-markdown-plugin"
                "config-deployer" -> "io.github.uniclog.AnkeyConfigDeoloyer"
                else -> name
            }

            xmlContent.append("  <plugin id=\"$pluginId\" url=\"${zipFile.name}\" version=\"${subProject.version}\">\n")
            xmlContent.append("    <idea-version since-build=\"$buildNumber\" until-build=\"999.*\" />\n")
            xmlContent.append("    <name>${subProject.name}</name>\n")
            xmlContent.append("  </plugin>\n")
        }

        xmlContent.append("</plugins>")
        xmlFile.writeText(xmlContent.toString())

        println("Local repository created at: ${bundleDir.get().asFile.absolutePath}")
    }
}
