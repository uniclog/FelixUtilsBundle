package io.github.uniclog.docker.runner.compose

import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File

object VersionReader {
    fun read(path: AnkeyPath): Result<String> {
        val versionFile = File("${path.absolutePath}/conf/version.json")
        if (!versionFile.exists()) {
            return Result.failure(
                IllegalArgumentException("Version file not found: ${path.absolutePath}/conf/version.json")
            )
        }

        val versionText = versionFile.readText()
        val versionMatch = Regex(""""version"\s*:\s*"([^"]+)"""")
            .find(versionText)
            ?: return Result.failure(
                IllegalArgumentException("Invalid version file format: ${path.absolutePath}/conf/version.json")
            )

        val version = versionMatch.groupValues[1]
        return Result.success(version.takeWhile { it.isDigit() || it == '.' })
    }
}
