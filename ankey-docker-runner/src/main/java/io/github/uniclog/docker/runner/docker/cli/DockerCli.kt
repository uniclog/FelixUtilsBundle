package io.github.uniclog.docker.runner.docker.cli

import java.util.concurrent.TimeUnit

object DockerCli {

    fun run(
        command: List<String>,
        onLine: ((String) -> Unit)? = null
    ): Int {

        onLine?.invoke("\n\n")
        onLine?.invoke("Docker CLI\n")
        onLine?.invoke("Running: ${formatCommand(command)}\n\n")

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        process.inputStream.bufferedReader().forEachLine { line ->
            onLine?.invoke("$line\n")
        }

        return process.waitFor()
    }

    fun runAndCollect(
        command: List<String>,
        timeoutSeconds: Long = 5
    ): String? {
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            return null
        }

        return process.inputStream.bufferedReader().readText().trim()
    }

    private fun formatCommand(command: List<String>): String {
        return command.joinToString(" ") {
            if (it.contains(' ') || it.contains('"')) {
                "\"${it.replace("\"", "\\\"")}\""
            } else {
                it
            }
        }
    }
}
