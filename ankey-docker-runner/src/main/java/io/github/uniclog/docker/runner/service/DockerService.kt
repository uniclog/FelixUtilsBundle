package io.github.uniclog.docker.runner.service

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import io.github.uniclog.docker.runner.docker.ComposeFileGenerator
import io.github.uniclog.docker.runner.docker.DockerComposeRunner
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File

object DockerService {

    fun up(project: Project, composePath: String): Result<Unit> {
        if (composePath.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Compose file path is not specified")
            )
        }

        DockerComposeRunner.upCompose(project, composePath)
        return Result.success(Unit)
    }

    fun stop(project: Project, composePath: String): Result<Unit> {
        if (composePath.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Compose file path is not specified")
            )
        }

        DockerComposeRunner.stopCompose(project, composePath)
        return Result.success(Unit)
    }

    fun down(project: Project, composePath: String): Result<Unit> {
        if (composePath.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Compose file path is not specified")
            )
        }

        DockerComposeRunner.downCompose(project, composePath)
        return Result.success(Unit)
    }

    fun downProcBackground(project: Project, composePath: String): Result<Unit> {
        if (composePath.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Compose file path is not specified")
            )
        }

        DockerComposeRunner.downCompose(project, composePath, true)
        return Result.success(Unit)
    }

    fun composeExists(path: AnkeyPath): Boolean {
        return File(path.getComposePath()).exists()
    }

    fun generateCompose(path: AnkeyPath, prefix: String, services: List<AnkeyComponentState>): String? {
        return ComposeFileGenerator.generate(path, prefix, services)
    }

}