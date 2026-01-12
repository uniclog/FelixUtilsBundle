package io.github.uniclog.docker.runner.model

import io.github.uniclog.docker.runner.settings.Constants

data class AnkeyPath(
    val absolutePath: String = "",
    val root: String = ""
) {
    val relativePath: String = absolutePath.removePrefix("$root/")

    fun getComposePath(): String {
        return absolutePath.removeSuffix("ankey") + Constants.COMPOSE_FILE_NAME
    }

    fun getComposeBasePath(): String {
        return absolutePath.removeSuffix("ankey")
    }

    override fun toString(): String = relativePath
    override fun hashCode(): Int = absolutePath.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnkeyPath

        return absolutePath == other.absolutePath
    }
}