plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // intellijIdea("2026.1")
        // testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // pluginModule(implementation(project(":plugin-a")))
        // pluginModule(implementation(project(":plugin-b")))
        pluginModule(implementation(project(":config-deployer")))
        pluginModule(implementation(project(":ankey-docker-runner")))
        pluginModule(implementation(project(":common-docs-markdown-plugin")))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    val platformVersion = project.findProperty("platformVersion")?.toString() ?: "2023.3.2"

    val (javaVersion, buildNumber) = when {
        platformVersion.startsWith("2021") -> JavaVersion.VERSION_11 to "212"
        platformVersion.startsWith("2026") -> JavaVersion.VERSION_21 to "261"
        else -> JavaVersion.VERSION_17 to "232" // Default for 2022-2025
    }

    dependencies {
        intellijPlatform {
            intellijIdea(platformVersion)
        }
    }

    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString()))
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

    val plugin1 = tasks.getByPath(":ankey-docker-runner:buildPlugin")
    val plugin2 = tasks.getByPath(":config-deployer:buildPlugin")

    dependsOn(plugin1, plugin2)

    from(
        zipTree(
            project(":plugin-a")
                .tasks.named("buildPlugin").get().outputs.files.singleFile
        )
    )
    from(
        zipTree(
            project(":plugin-b")
                .tasks.named("buildPlugin").get().outputs.files.singleFile
        )
    )
    from(
        zipTree(
            project(":ankey-docker-runner")
                .tasks.named("buildPlugin").get().outputs.files.singleFile
        )
    )
}
