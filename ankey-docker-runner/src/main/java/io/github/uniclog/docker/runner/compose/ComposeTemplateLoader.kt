package io.github.uniclog.docker.runner.compose

object ComposeTemplateLoader {
    fun load(path: String): String {
        val stream = this::class.java.classLoader.getResourceAsStream(path)
            ?: error("Resource not found: $path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
