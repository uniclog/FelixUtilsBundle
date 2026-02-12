package io.github.uniclog.docker.runner.docker

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import io.github.uniclog.docker.runner.settings.Constants.COMPOSE_FILE_NAME
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object DockerComposeRunner {

    fun downCompose(
        project: Project,
        composeFilePath: String,
        asyncTask: Boolean = false,
        onFinished: (Int) -> Unit = {}
    ) {
        runCompose(
            project = project,
            composeFilePath = composeFilePath,
            title = "Docker runner",
            startMessage = "Stopping docker compose...",
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "down", "--rmi", "local", "-v", "--remove-orphans"
            ),
            asyncTask = asyncTask,
            onFinished = onFinished
        ) {
                ComposeFileGenerator.deleteDockerFilesFiles(composeFilePath.removeSuffix(COMPOSE_FILE_NAME))
        }
    }

    fun upCompose(
        project: Project,
        composeFilePath: String,
        asyncTask: Boolean = false,
        onFinished: (Int) -> Unit = {}
    ) {
        val startedUpStep = AtomicBoolean(false)
        runCompose(
            project = project,
            composeFilePath = composeFilePath,
            title = "Docker runner",
            startMessage = "Building docker images (no cache)...",
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "build", "--no-cache", "--quiet"
            ),
            asyncTask = asyncTask,
            onFinished = { exitCode ->
                if (!startedUpStep.get()) {
                    onFinished(exitCode)
                }
            }
        ) {
            startedUpStep.set(true)
            runCompose(
                project = project,
                composeFilePath = composeFilePath,
                title = "Docker runner",
                startMessage = "Starting docker compose...",
                command = listOf(
                    "docker", "compose",
                    "-f", composeFilePath,
                    "up", "-d"
                ),
                asyncTask = asyncTask,
                onFinished = onFinished,
                clearConsole = false
            ) {}
        }
    }

    fun stopCompose(
        project: Project,
        composeFilePath: String,
        asyncTask: Boolean = false,
        onFinished: (Int) -> Unit = {}
    ) =
        runCompose(
            project = project,
            composeFilePath = composeFilePath,
            title = "Docker runner",
            startMessage = "Stopping docker compose...",
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "stop"
            ),
            asyncTask = asyncTask,
            onFinished = onFinished
        ) {

        }

    fun restartCompose(
        project: Project,
        composeFilePath: String,
        asyncTask: Boolean = false,
        onFinished: (Int) -> Unit = {}
    ) =
        runCompose(
            project = project,
            composeFilePath = composeFilePath,
            title = "Docker runner",
            startMessage = "Restarting docker compose...",
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "restart"
            ),
            asyncTask = asyncTask,
            onFinished = onFinished
        ) {

        }

    private fun runCompose(
        project: Project,
        composeFilePath: String,
        title: String,
        startMessage: String,
        command: List<String>,
        asyncTask: Boolean,
        onFinished: (Int) -> Unit,
        clearConsole: Boolean = true,
        onSuccessAction: () -> Unit
    ) {
        val runTask: (ConsoleView?) -> Unit = { console ->
            ProgressManager.getInstance().run(
                object : Task.Backgroundable(project, title, true) {

                    private var exitCode: Int = -1

                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = true
                        indicator.text = startMessage

                        val process = ProcessBuilder(command)
                            .redirectErrorStream(true)
                            .start()

                        process.inputStream.bufferedReader().forEachLine {
                            if (indicator.isCanceled) {
                                process.destroy()
                                return@forEachLine
                            }
                            console?.let { c -> printColored(c, "$it\n") }
                        }

                        exitCode = process.waitFor()
                    }

                    override fun onSuccess() {
                        val ok = exitCode == 0
                        console?.let {
                            printColored(it, "\nFinished with exit code $exitCode\n")
                        }
                        notify(
                            project,
                            if (ok) "Docker Compose finished" else "Docker Compose failed",
                            "Exit code: $exitCode",
                            if (ok) NotificationType.INFORMATION else NotificationType.ERROR
                        )
                        if (ok) {
                            onSuccessAction.invoke()
                        }
                        onFinished(exitCode)
                    }

                    override fun onThrowable(error: Throwable) {
                        notify(
                            project,
                            "Docker Compose error",
                            error.message ?: "Unknown error",
                            NotificationType.ERROR
                        )
                        onFinished(-1)
                    }
                }
            )
        }

        if (asyncTask) {
            runTask(null)
            return
        }

        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Docker Runner") ?: return

        toolWindow.activate {
            val console = getOrCreateDockerConsole(project, composeFilePath)
            if (clearConsole) {
                console.clear()
            }
            printColored(console, "$startMessage\n")
            runTask(console)
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

    private fun notify(
        project: Project,
        title: String,
        content: String,
        type: NotificationType
    ) {
        Notifications.Bus.notify(
            Notification("Docker Runner", title, content, type),
            project
        )
    }

    fun getComposeProjectName(composePath: String): String? {
        File(composePath).useLines { lines ->
            lines.forEach {
                val trimmed = it.trim()
                if (trimmed.startsWith("name:")) {
                    return trimmed.removePrefix("name:").trim()
                }
            }
        }
        return null
    }

    fun hasRunningComposeContainers(projectName: String?): Boolean {
        if (projectName == null)
            return false

        val process = ProcessBuilder(
            "docker", "ps",
            "--filter", "label=com.docker.compose.project=$projectName",
            "--format", "{{.Names}}"
        ).start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()

        return output.isNotBlank()
    }

    fun dockerProjectExists(projectName: String): Boolean {
        val process = ProcessBuilder(
            "docker", "network", "ls",
            "--filter", "name=^${projectName}_",
            "--format", "{{.Name}}"
        )
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        return output.isNotEmpty()
    }
}

