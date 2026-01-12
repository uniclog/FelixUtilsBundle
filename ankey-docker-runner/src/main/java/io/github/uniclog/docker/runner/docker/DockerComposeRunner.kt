package io.github.uniclog.docker.runner.docker

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import io.github.uniclog.docker.runner.settings.Constants.COMPOSE_FILE_NAME
import java.io.File

object DockerComposeRunner {

    fun downCompose(project: Project, composeFilePath: String) {
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Docker Runner") ?: return

        toolWindow.activate {
            val console = getOrCreateDockerConsole(project, composeFilePath)
            console.clear()

            printColored(console, "Stopping docker compose...\n")

            ProgressManager.getInstance().run(
                object : Task.Backgroundable(project, "Docker runner", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val process = ProcessBuilder(
                            "docker", "compose",
                            "-f", composeFilePath,
                            "down",
                            "--rmi", "local",
                            "-v", "--remove-orphans"
                        )
                            .redirectErrorStream(true)
                            .start()

                        process.inputStream.bufferedReader().forEachLine {
                            printColored(console, "$it\n")
                        }

                        val exitCode = process.waitFor()
                        printColored(console, "\nFinished with exit code $exitCode\n")
                    }
                }
            )
        }
    }

    fun upCompose(project: Project, composeFilePath: String) {

        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Docker Runner") ?: return

        toolWindow.activate {
            val console = getOrCreateDockerConsole(project, composeFilePath)
            console.clear()

            printColored(console, "Starting docker compose...\n")

            ProgressManager.getInstance().run(
                object : Task.Backgroundable(project, "Docker runner", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val process = ProcessBuilder(
                            "docker", "compose",
                            "-f", composeFilePath,
                            "up", "-d", "--build", "--quiet-pull"
                        )
                            .redirectErrorStream(true)
                            .start()

                        process.inputStream.bufferedReader().forEachLine {
                            printColored(console, "$it\n")
                        }

                        val exitCode = process.waitFor()
                        printColored(console, "\nFinished with exit code $exitCode\n")
                    }
                }
            )
        }
    }

    fun stopCompose(project: Project, composeFilePath: String) {
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Docker Runner") ?: return

        toolWindow.activate {
            val console = getOrCreateDockerConsole(project, composeFilePath)
            console.clear()

            printColored(console, "Stopping docker compose...\n")

            ProgressManager.getInstance().run(
                object : Task.Backgroundable(project, "Docker runner", true) {
                    override fun run(indicator: ProgressIndicator) {
                        val process = ProcessBuilder(
                            "docker", "compose",
                            "-f", composeFilePath,
                            "stop"
                        )
                            .redirectErrorStream(true)
                            .start()

                        process.inputStream.bufferedReader().forEachLine {
                            printColored(console, "$it\n")
                        }

                        val exitCode = process.waitFor()
                        printColored(console, "\nFinished with exit code $exitCode\n")
                    }
                }
            )
        }
    }


    fun getOrCreateDockerConsole(project: Project, composeFilePath: String): ConsoleView {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Docker Runner")
            ?: error("Docker Runner ToolWindow not found")

        val stackName = getStackName(composeFilePath)

        val contentManager = toolWindow.contentManager

        val existingContent = contentManager.contents.firstOrNull { it.tabName == stackName }
        if (existingContent != null) {
            contentManager.setSelectedContent(existingContent)
            return existingContent.component as ConsoleView
        }

        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val content = ContentFactory.getInstance().createContent(console.component, stackName, false)

        contentManager.addContent(content)
        contentManager.setSelectedContent(content)
        return console
    }

    fun getStackName(composeFilePath: String = COMPOSE_FILE_NAME): String {
        val file = File(composeFilePath)
        if (!file.exists()) return "ankey_default"

        file.useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("name:")) {
                    return trimmed.removePrefix("name:").trim()
                }
            }
        }
        return "ankey_default"
    }

    fun printColored(console: ConsoleView, line: String) {
        val regexDone = Regex("DONE")
        val regexCached = Regex("CACHED")
        val regexCreating = Regex("Creating|Starting")
        val regexError = Regex("Failed")

        val type = when {
            regexDone.containsMatchIn(line) -> ConsoleViewContentType.NORMAL_OUTPUT
            regexCached.containsMatchIn(line) -> ConsoleViewContentType.SYSTEM_OUTPUT
            regexCreating.containsMatchIn(line) -> ConsoleViewContentType.LOG_WARNING_OUTPUT
            regexError.containsMatchIn(line) -> ConsoleViewContentType.ERROR_OUTPUT
            else -> ConsoleViewContentType.NORMAL_OUTPUT
        }
        ApplicationManager.getApplication().invokeLater {
            console.print(line, type)
        }
    }
}

