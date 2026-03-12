package io.github.uniclog.docker.runner.compose

import io.github.uniclog.docker.runner.model.AnkeyComponent
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.settings.Constants.COMPOSE_FILE_NAME
import java.io.File

object ComposeFileWriter {

    fun writeComposeFile(path: String, content: String) {
        File(path).writeText(content)
    }

    fun copyDockerfiles(basePath: String, services: List<AnkeyComponentState>) {
        File("$basePath/docker").mkdirs()

        val paths = mutableMapOf(
            "ankey/run.sh" to "ankey/run.sh",
            "ankey/backup.sh" to "ankey/backup.sh"
        )
        // @todo РґРѕР±Р°РІРёС‚СЊ РѕР¶РёРґР°РЅРёРµ РґРѕР±Р°РІР»РµРЅРЅС‹С… СЃРµСЂРІРёСЃРѕРІ
        services.forEach {
            when (it.component) {
                AnkeyComponent.CORE -> {
                    paths["docker/Dockerfile-ankey"] = "docker/Dockerfile-ankey"
                }
                AnkeyComponent.POSTGRES -> {
                    paths["docker/Dockerfile-postgres"] = "docker/Dockerfile-postgres"
                }
                AnkeyComponent.OPENSEARCH -> {
                    paths["docker/Dockerfile-opensearch"] = "docker/Dockerfile-opensearch"
                }
                AnkeyComponent.KAFKA -> {
                    paths["docker/Dockerfile-kafka"] = "docker/Dockerfile-kafka"
                }
                AnkeyComponent.BPMN -> {
                    paths["docker/Dockerfile-bpmn"] = "docker/Dockerfile-bpmn"
                    paths["bpmn/init2.sql"] = "ankey/db/postgresql/scripts/init2.sql"
                }
                else -> {}
            }
        }
        paths.forEach { (source, target) ->
            javaClass.classLoader.getResourceAsStream(source)
                ?.use { input ->
                    val targetFile = File(basePath, target)
                    targetFile.parentFile?.mkdirs()
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        }

        if (services.any { it.component == AnkeyComponent.POSTGRES }) {
            val sourceFile = File(basePath, "ankey/db/postgresql/scripts/aftercreateuser.sql")
            val targetFile = File(basePath, "docker/aftercreateuser.sql")
            targetFile.parentFile?.mkdirs()
            if (sourceFile.exists()) {
                sourceFile.inputStream().use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                targetFile.writeText("-- generated placeholder: source not found\n")
            }
        }

        val initTargetFile = File(basePath, "docker/init2.sql")
        initTargetFile.parentFile?.mkdirs()
        if (services.any { it.component == AnkeyComponent.BPMN }) {
            val resource = javaClass.classLoader.getResourceAsStream("bpmn/init2.sql")
            if (resource != null) {
                resource.use { input ->
                    initTargetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                initTargetFile.writeText("-- generated placeholder: resource not found\n")
            }
        } else {
            if (!initTargetFile.exists()) {
                initTargetFile.writeText("-- generated placeholder: not required for this setup\n")
            }
        }
    }

    fun deleteGeneratedFiles(basePath: String) {
        File("$basePath/docker").deleteRecursively()
        File("$basePath/pgdata").deleteRecursively()
        File("$basePath/ankey/run.sh").delete()
        File("$basePath/ankey/backup.sh").delete()
        File("$basePath/ankey/felix-cache").deleteRecursively()
        File("$basePath/ankey/db/postgresql/scripts/init2.sql").delete()
        File("$basePath/$COMPOSE_FILE_NAME").delete()
    }
}
