package io.github.uniclog.docker.runner.compose

import java.io.File

object ComposeMetadata {

    fun getProjectName(composeFilePath: String): String? {
        val file = File(composeFilePath)
        if (!file.exists()) return null

        file.useLines { lines ->
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("name:")) {
                    return trimmed.removePrefix("name:").trim()
                }
            }
        }
        return null
    }

    fun getStackName(composeFilePath: String): String {
        return getProjectName(composeFilePath) ?: "ankey_default"
    }
}
