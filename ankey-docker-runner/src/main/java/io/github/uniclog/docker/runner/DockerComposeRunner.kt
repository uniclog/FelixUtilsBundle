package io.github.uniclog.docker.runner

import java.io.File

object DockerComposeRunner {
    fun runCompose() {
        val process = ProcessBuilder("docker-compose", "up", "-d")
            .directory(File("/path/to/compose/project")) // путь к docker-compose.yml
            .inheritIO()
            .start()
        process.waitFor()
    }
}