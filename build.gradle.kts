plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.2.4")
        pluginModule(implementation(project(":plugin-a")))
        pluginModule(implementation(project(":plugin-b")))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    dependencies {
        intellijPlatform {
            intellijIdea("2025.2.4")
        }
    }

    repositories {
        mavenCentral()
        intellijPlatform.defaultRepositories()
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    tasks {
        patchPluginXml {
            sinceBuild.set("251")
            untilBuild.set("999.*")
        }
    }
}

tasks.register<Zip>("buildBundle") {
    group = "build"
    description = "Builds a bundle containing plugin-a and plugin-b"

    archiveFileName.set("bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))

    val pluginA = tasks.getByPath(":plugin-a:buildPlugin")
    val pluginB = tasks.getByPath(":plugin-b:buildPlugin")

    dependsOn(pluginA, pluginB)

    from(zipTree(project(":plugin-a")
        .tasks.named("buildPlugin").get().outputs.files.singleFile))
    from(zipTree(project(":plugin-b")
        .tasks.named("buildPlugin").get().outputs.files.singleFile))
}