package io.github.uniclog.docker.runner.ui

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory
import io.github.uniclog.docker.runner.compose.ComposeMetadata

object DockerConsole {

    fun open(project: Project, composeFilePath: String): ConsoleView? {
        val toolWindow = ToolWindowManager.getInstance(project)
            .getToolWindow("Docker Runner") ?: return null

        val stackName = ComposeMetadata.getStackName(composeFilePath)
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

        toolWindow.activate { }
        return console
    }

    fun clear(console: ConsoleView) {
        ApplicationManager.getApplication().invokeLater {
            console.clear()
        }
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
