package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import io.github.uniclog.docker.runner.docker.ComposeFileGenerator
import io.github.uniclog.docker.runner.docker.DockerComposeRunner.getComposeProjectName
import io.github.uniclog.docker.runner.docker.DockerComposeRunner.hasRunningComposeContainers
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.AnkeyPathService
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.ui.dialog.ConfirmDialog
import io.github.uniclog.docker.runner.ui.dialog.InfoDialog
import javax.swing.*

class ComposeStatusPanel(
    private val getAnkeyPath: () -> AnkeyPath,
    private val upAction: Action
) {
    val panel = JPanel()
    private val statusLabel = JBLabel()

    private val showButton = JButton("Show").apply { isEnabled = false }
    private val openButton = JButton("Open").apply { isEnabled = false }
    private val deleteButton = JButton("Delete").apply { isEnabled = false }

    init {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(showButton)
        panel.add(openButton)
        panel.add(deleteButton)
        panel.add(Box.createHorizontalGlue())
        panel.add(statusLabel)

        showButton.addActionListener {
            /// @todo проверить на пустой comboBox
            InfoDialog(getAnkeyPath()).show()
        }
        openButton.addActionListener {
            /// @todo проверить на пустой comboBox
            val path = getAnkeyPath().getComposePath()

            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
                ?: return@addActionListener

            val project = ProjectManager.getInstance().openProjects.firstOrNull()
                ?: return@addActionListener

            FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
        deleteButton.addActionListener {
            val dialog = ConfirmDialog(
                message = "Are you sure you want to delete this file?<br/>This action cannot be undone.",
                okActionText = "Delete"
            )

            if (!dialog.showAndGet())
                return@addActionListener

            if (DockerService.composeExists(getAnkeyPath())) {
                val projectName = getComposeProjectName(getAnkeyPath().getComposePath())
                if (hasRunningComposeContainers(projectName)) {
                    Messages.showErrorDialog(
                        "Docker containers for this compose file are running and must be stopped before generating a new compose file.",
                        "Generate Docker Compose Error"
                    )
                    return@addActionListener
                }
                //DockerService.downProcBackground(project, ankeyPath.getComposePath())
            }
            ComposeFileGenerator.deleteDockerFilesFiles(getAnkeyPath().getComposeBasePath())
            //AnkeyPathService.deleteFile(getAnkeyPath())
            update()
        }
    }

    fun update() {
        val exists = DockerService.composeExists(path = getAnkeyPath())
        upAction.isEnabled = exists

        if (exists) {
            statusLabel.text = "Compose file exists!"
            statusLabel.foreground = JBColor.GREEN
        } else {
            statusLabel.text = "Compose file not found!"
            statusLabel.foreground = JBColor.RED
        }

        showButton.isEnabled = exists
        openButton.isEnabled = exists
        deleteButton.isEnabled = exists
    }
}
