package io.github.uniclog.docker.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
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
                runInBackground(project, composePath) { p, path -> DockerService.up(p, path) }
            }
            STOP_EXIT_CODE -> {
                val composePath = dialog.getDialogData()["composePath"].orEmpty()
                runInBackground(project, composePath) { p, path -> DockerService.stop(p, path) }
            }
            DOWN_EXIT_CODE -> {
                val composePath = dialog.getDialogData()["composePath"].orEmpty()
                runInBackground(project, composePath) { p, path -> DockerService.down(p, path) }
            }
        }
    }

    private fun runInBackground(
        project: Project,
        composePath: String,
        action: (Project, String) -> java.util.concurrent.CompletableFuture<Result<Unit>>
    ) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Docker Runner", true) {
                override fun run(indicator: ProgressIndicator) {
                    indicator.isIndeterminate = true
                    indicator.text = "Running docker compose..."

                    val result = try {
                        action(project, composePath).join()
                    } catch (e: Exception) {
                        Result.failure(e)
                    }

                    result.onFailure { showError(project, it.message) }
                }
            }
        )
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
