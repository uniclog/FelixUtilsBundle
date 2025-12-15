package io.github.uniclog.docker.runner

object PluginLogic {
    fun run(data: Map<String, String>) {
        println("Running plugin with data: $data")
        DockerComposeRunner.runCompose()
    }
}