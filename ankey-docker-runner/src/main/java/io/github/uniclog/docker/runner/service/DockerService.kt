package io.github.uniclog.docker.runner.service

import io.github.uniclog.docker.runner.compose.ComposeGenerator
import io.github.uniclog.docker.runner.compose.ComposeMetadata
import io.github.uniclog.docker.runner.docker.ComposeRunner
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

object DockerService {
    private val executor = Executors.newCachedThreadPool()

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

    fun up(
        composePath: String,
        onLine: ((String) -> Unit)? = null
    ): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        return CompletableFuture.supplyAsync({
            val projectName = ComposeMetadata.getProjectName(composePath)
            val hasRunningContainers = ComposeRunner.hasRunningComposeContainers(projectName)

            val exitCode = if (hasRunningContainers) {
                ComposeRunner.restartCompose(composePath, onLine)
            } else {
                ComposeRunner.upCompose(composePath, onLine)
            }
            resultFromExitCode(exitCode)
        }, executor)
    }

    fun stop(
        composePath: String,
        onLine: ((String) -> Unit)? = null
    ): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        return CompletableFuture.supplyAsync({
            val exitCode = ComposeRunner.stopCompose(composePath, onLine)
            resultFromExitCode(exitCode)
        }, executor)
    }

    fun down(
        composePath: String,
        onLine: ((String) -> Unit)? = null
    ): CompletableFuture<Result<Unit>> {
        val precheck = validateComposePath(composePath)
        if (precheck.isFailure) {
            return CompletableFuture.completedFuture(precheck)
        }

        return CompletableFuture.supplyAsync({
            val exitCode = ComposeRunner.downCompose(composePath, onLine)
            resultFromExitCode(exitCode)
        }, executor)
    }

    fun composeExists(path: AnkeyPath): Boolean {
        return File(path.getComposePath()).exists()
    }

    fun generateCompose(path: AnkeyPath, prefix: String, services: List<AnkeyComponentState>): String? {
        return ComposeGenerator.generate(path, prefix, services)
    }

}
