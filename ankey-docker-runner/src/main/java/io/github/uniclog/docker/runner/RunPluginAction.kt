package io.github.uniclog.docker.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.ui.RunDockerDialog
import io.github.uniclog.docker.runner.ui.RunDockerDialog.Companion.DOWN_EXIT_CODE
import io.github.uniclog.docker.runner.ui.RunDockerDialog.Companion.STOP_EXIT_CODE
import io.github.uniclog.docker.runner.ui.RunDockerDialog.Companion.UP_EXIT_CODE

class RunPluginAction : AnAction("Run Plugin") {


    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        val dialog = RunDockerDialog(event)
        dialog.show()
        //if (!dialog.showAndGet()) return
        when(dialog.exitCode) {
            UP_EXIT_CODE -> {
                val composePath = dialog.getDialogData()["composePath"].orEmpty()
                DockerService
                    .up(project, composePath)
                    .thenAccept { result -> result.onFailure { showError(project, it.message) } }
            }
            STOP_EXIT_CODE -> {
                val composePath = dialog.getDialogData()["composePath"].orEmpty()
                DockerService
                    .stop(project, composePath)
                    .thenAccept { result -> result.onFailure { showError(project, it.message) } }
            }
            DOWN_EXIT_CODE -> {
                val composePath = dialog.getDialogData()["composePath"].orEmpty()
                DockerService
                    .down(project, composePath)
                    .thenAccept { result -> result.onFailure { showError(project, it.message) } }
            }
        }
    }

    private fun showError(project: Project, message: String?) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(
                project,
                message ?: "Unknown error",
                "Run Plugin"
            )
        }
    }
}
