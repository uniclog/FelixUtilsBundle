package io.github.uniclog.docker.runner.docker

import io.github.uniclog.docker.runner.docker.cli.DockerCli

object ComposeRunner {

    fun downCompose(
        composeFilePath: String,
        onLine: ((String) -> Unit)? = null
    ): Int {
        onLine?.invoke("Stopping docker compose...")
        return DockerCli.run(
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "down", "--rmi", "local", "-v", "--remove-orphans"
            ),
            onLine = onLine
        )
    }

    fun upCompose(
        composeFilePath: String,
        onLine: ((String) -> Unit)? = null
    ): Int {
        onLine?.invoke("Building docker images...")
        val buildExit = DockerCli.run(
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "build", "--no-cache",
                // "--quiet"
            ),
            onLine = onLine
        )
        if (buildExit != 0) return buildExit

        onLine?.invoke("Starting docker compose...")
        return DockerCli.run(
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "up", "-d"
            ),
            onLine = onLine
        )
    }

    fun stopCompose(
        composeFilePath: String,
        onLine: ((String) -> Unit)? = null
    ): Int {
        onLine?.invoke("Stopping docker compose...")
        return DockerCli.run(
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "stop"
            ),
            onLine = onLine
        )
    }

    fun restartCompose(
        composeFilePath: String,
        onLine: ((String) -> Unit)? = null
    ): Int {
        onLine?.invoke("Restarting docker compose...")
        return DockerCli.run(
            command = listOf(
                "docker", "compose",
                "-f", composeFilePath,
                "restart"
            ),
            onLine = onLine
        )
    }

    fun hasRunningComposeContainers(projectName: String?): Boolean {
        if (projectName == null) return false

        val output = DockerCli.runAndCollect(
            command = listOf(
                "docker", "ps",
                "--filter", "label=com.docker.compose.project=$projectName",
                "--format", "{{.Names}}"
            ),
            timeoutSeconds = 5
        ) ?: return false

        return output.isNotBlank()
    }

    fun dockerProjectExists(projectName: String): Boolean {
        val output = DockerCli.runAndCollect(
            command = listOf(
                "docker", "network", "ls",
                "--filter", "name=^ankey${projectName}_",
                "--format", "{{.Name}}"
                // "docker", "ps",
                // "--filter", "label=com.docker.compose.project=$projectName",
                // "--format", "{{.Names}}"
            ),
            timeoutSeconds = 5
        ) ?: return false

        return output.isNotBlank()
    }
}
