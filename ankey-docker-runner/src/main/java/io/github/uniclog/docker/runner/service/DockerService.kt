package io.github.uniclog.docker.runner.service

import com.intellij.openapi.project.Project
import io.github.uniclog.docker.runner.docker.ComposeFileGenerator
import io.github.uniclog.docker.runner.docker.DockerComposeRunner
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File
import java.util.concurrent.CompletableFuture

object DockerService {

    private fun validateComposePath(composePath: String): Result<Unit> {
        if (composePath.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Compose file path is not specified")
            )
        }
        if (!File(composePath).exists()) {
            return Result.failure(
                IllegalArgumentException("Compose file not found: $composePath")
            )
        }
        return Result.success(Unit)
    }

    private fun resultFromExitCode(exitCode: Int): Result<Unit> {
        return if (exitCode == 0) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("Docker compose finished with exit code $exitCode"))
        }
    }

    fun up(project: Project, composePath: String): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        val future = CompletableFuture<Result<Unit>>()
        DockerComposeRunner.upCompose(project, composePath) { exitCode ->
            future.complete(resultFromExitCode(exitCode))
        }
        return future
    }

    fun stop(project: Project, composePath: String): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        val future = CompletableFuture<Result<Unit>>()
        DockerComposeRunner.stopCompose(project, composePath) { exitCode ->
            future.complete(resultFromExitCode(exitCode))
        }
        return future
    }

    fun down(project: Project, composePath: String): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        val future = CompletableFuture<Result<Unit>>()
        DockerComposeRunner.downCompose(project, composePath) { exitCode ->
            future.complete(resultFromExitCode(exitCode))
        }
        return future
    }

    fun downProcBackground(project: Project, composePath: String): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        val future = CompletableFuture<Result<Unit>>()
        DockerComposeRunner.downCompose(project, composePath, true) { exitCode ->
            future.complete(resultFromExitCode(exitCode))
        }
        return future
    }

    fun composeExists(path: AnkeyPath): Boolean {
        return File(path.getComposePath()).exists()
    }

    fun generateCompose(path: AnkeyPath, prefix: String, services: List<AnkeyComponentState>): String? {
        return ComposeFileGenerator.generate(path, prefix, services)
    }

}
