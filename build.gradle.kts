plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
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
        intellijIdea("2026.1")
        // testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        pluginModule(implementation(project(":plugin-a")))
        pluginModule(implementation(project(":plugin-b")))
        // pluginModule(implementation(project(":config-deployer")))
        pluginModule(implementation(project(":ankey-docker-runner")))
    }
}

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")

    dependencies {
        intellijPlatform {
            intellijIdea("2026.1")
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
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    tasks {
        patchPluginXml {
            sinceBuild.set("232")
            untilBuild.set("999.*")
        }
        withType<org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask> {
            coroutinesJavaAgentFile.set(layout.projectDirectory.file(".disabled-coroutines-javaagent.jar"))
        }
        withType<JavaCompile> {
            sourceCompatibility = "17"
            targetCompatibility = "17"
        }
    }

    if (name == "config-deployer") {
        val runIdeTask = tasks.named<org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask>("runIde")

        tasks.register<Exec>("runIdeViaBat") {
            group = "intellij platform"
            description = "Runs Config Deployer via idea.bat to avoid JavaExec launcher issues on Windows."

            dependsOn("prepareSandbox")

            doFirst {
                val runIde = runIdeTask.get()
                val ideaScript = runIde.platformPath.resolve("bin/idea.bat").toFile()

                workingDir = runIde.platformPath.toFile()
                commandLine(
                    ideaScript.absolutePath,
                    "-Didea.auto.reload.plugins=true",
                    "-Didea.classpath.index.enabled=false",
                    "-Didea.config.path=${runIde.sandboxConfigDirectory.get().asFile.absolutePath}",
                    "-Didea.is.internal=true",
                    "-Didea.log.path=${runIde.sandboxLogDirectory.get().asFile.absolutePath}",
                    "-Didea.plugin.in.sandbox.mode=true",
                    "-Didea.plugins.path=${runIde.sandboxPluginsDirectory.get().asFile.absolutePath}",
                    "-Didea.required.plugins.id=io.github.uniclog.FelixUtils",
                    "-Didea.system.path=${runIde.sandboxSystemDirectory.get().asFile.absolutePath}",
                )
            }
        }
    }
}

tasks.register<Zip>("buildBundle") {
    group = "build"
    description = "Builds a bundle containing plugins"

    archiveFileName.set("plugins-bundle.zip")
    destinationDirectory.set(layout.buildDirectory.dir("bundle"))

    val pluginA = tasks.getByPath(":plugin-a:buildPlugin")
    val pluginB = tasks.getByPath(":plugin-b:buildPlugin")
    val pluginC = tasks.getByPath(":ankey-docker-runner:buildPlugin")

    dependsOn(pluginA, pluginB, pluginC)

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
