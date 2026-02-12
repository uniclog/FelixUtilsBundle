package io.github.uniclog.docker.runner.docker

import io.github.uniclog.docker.runner.docker.DockerComposeRunner.dockerProjectExists
import io.github.uniclog.docker.runner.model.AnkeyComponent
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.model.Placeholders
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_10
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_11
import io.github.uniclog.docker.runner.settings.Constants.COMPOSE_FILE_NAME
import io.github.uniclog.docker.runner.settings.Constants.COMPOSE_FILE_TEMPLATE
import io.github.uniclog.docker.runner.settings.Constants.PORT_DEBUG
import io.github.uniclog.docker.runner.settings.Constants.PORT_HTTP
import io.github.uniclog.docker.runner.settings.Constants.PORT_JMX
import io.github.uniclog.docker.runner.settings.Constants.PORT_KAFKA
import io.github.uniclog.docker.runner.settings.Constants.PORT_KAFKA_UI
import io.github.uniclog.docker.runner.settings.Constants.PORT_BPMN_DEBUG
import io.github.uniclog.docker.runner.settings.Constants.PORT_BPMN_HTTP
import io.github.uniclog.docker.runner.settings.Constants.PORT_OPENSEARCH_HTTP
import io.github.uniclog.docker.runner.settings.Constants.PORT_OPENSEARCH_TRANSPORT
import io.github.uniclog.docker.runner.settings.Constants.PORT_POSTGRES
import io.github.uniclog.docker.runner.ui.dialog.AlertDialog
import java.io.File
import java.net.ServerSocket

object ComposeFileGenerator {

    fun generate(
        ankeyPath: AnkeyPath,
        ankeyPrefix: String,
        services: List<AnkeyComponentState> = listOf()
    ): String? {
        // del old data
        deleteDockerFilesFiles(ankeyPath.getComposeBasePath().removeSuffix(COMPOSE_FILE_NAME))

        val placeholders = buildPlaceholders(ankeyPrefix)

        /// null check
        if (services.isEmpty() || getVersion(ankeyPath) == null)
            return null

        val template = buildComposeTemplate(services)
        val compose = replacePlaceholders(template, placeholders)

        File(ankeyPath.getComposePath()).writeText(compose)
        copyDockerfiles(ankeyPath.getComposeBasePath(), services)

        return placeholders.num
    }

    fun copyDockerfiles(basePath: String, services: List<AnkeyComponentState>) {
        File("$basePath/docker").mkdirs()

        val paths = mutableMapOf(
            "ankey/run.sh" to "ankey/run.sh",
            "ankey/backup.sh" to "ankey/backup.sh"
        )
        // @todo добавить ожидание добавленных сервисов
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
    }

    fun deleteDockerFilesFiles(basePath: String) {
        File("$basePath/docker").deleteRecursively()
        File("$basePath/pgdata").deleteRecursively()
        File("$basePath/ankey/run.sh").delete()
        File("$basePath/ankey/backup.sh").delete()
        File("$basePath/ankey/felix-cache").deleteRecursively()
        File("$basePath/ankey/db/postgresql/scripts/init2.sql").delete()
        File("$basePath/$COMPOSE_FILE_NAME").delete()
    }

    fun getVersion(path: AnkeyPath): String? {
        val ankeyPath = path.absolutePath

        val versionFile = File("$ankeyPath/conf/version.json")
        if (!versionFile.exists()) {
            AlertDialog.showErrorDialog(
                "Docker Runner",
                "Version file not found: \n${ankeyPath}/conf/version.json"
            )
            return null
        }

        val versionText = versionFile.readText()

        val versionMatch = Regex(""""version"\s*:\s*"([^"]+)"""")
            .find(versionText)
        if (versionMatch == null) {
            AlertDialog.showErrorDialog(
                "Docker Runner",
                "Invalid version file format: \n${ankeyPath}/conf/version.json"
            )
            return null
        }
        val version = versionMatch.groupValues[1]
        return version.takeWhile { it.isDigit() || it == '.' }
    }

    private fun buildPlaceholders(configurationName: String): Placeholders {
        var offset = 0

        fun isPortUsed(port: Int): Boolean =
            try {
                ServerSocket(port).use { false }
            } catch (_: Exception) {
                true
            }

        val basePorts = listOf(
            PORT_HTTP,
            PORT_DEBUG,
            PORT_JMX,
            PORT_KAFKA,
            PORT_KAFKA_UI,
            PORT_POSTGRES,
            PORT_BPMN_HTTP,
            PORT_BPMN_DEBUG,
            PORT_OPENSEARCH_HTTP,
            PORT_OPENSEARCH_TRANSPORT
        )

        fun normalizeDockerName(name: String): String =
            name
                .lowercase()
                .replace(Regex("[^a-z0-9_.-]"), "-")
                .replace(Regex("[-_.]{2,}"), "-")
                .trim('-', '_', '.')

        fun dockerNameWithOffset(name: String): String =
            normalizeDockerName(name.lowercase() + if (offset == 0) "" else "-$offset")

        while (true) {
            val portsToCheck = basePorts.map { it + offset }
            if (portsToCheck.all {
                    !isPortUsed(it) && !dockerProjectExists(dockerNameWithOffset(configurationName))
                }) {
                break
            }
            offset++
        }

        val num = dockerNameWithOffset(configurationName)

        return Placeholders(
            num = if (num == "" && configurationName == "") "" else "-$num",

            portHttp = (PORT_HTTP + offset).toString(),
            portDebug = (PORT_DEBUG + offset).toString(),
            portJmx = (PORT_JMX + offset).toString(),

            portKafka = (PORT_KAFKA + offset).toString(),
            portKafkaUi = (PORT_KAFKA_UI + offset).toString(),
            portBpmnHttp = (PORT_BPMN_HTTP + offset).toString(),
            portBpmnDebug = (PORT_BPMN_DEBUG + offset).toString(),

            portPostgres = (PORT_POSTGRES + offset).toString(),

            portOpensearch = (PORT_OPENSEARCH_HTTP + offset).toString(),
            portOpensearch2 = (PORT_OPENSEARCH_TRANSPORT + offset).toString(),
        )
    }

    fun buildComposeTemplate(selectedServices: List<AnkeyComponentState>): String {
        val base = loadComposeTemplate(COMPOSE_FILE_TEMPLATE.format("base"))
        val network = loadComposeTemplate(COMPOSE_FILE_TEMPLATE.format("network"))

        val services = selectedServices
            .filter { it.component.fileName.isNotBlank() }
            .joinToString("\n\n")
            { loadComposeTemplate(COMPOSE_FILE_TEMPLATE.format(it.component.fileName)) }

        val compose = buildString {
            append(base.trimEnd())
            append("\n\n")
            append(services)
            append("\n\n")
            append(network)
        }

        return compose
    }

    private fun loadComposeTemplate(path: String): String {
        val stream = this::class.java.classLoader.getResourceAsStream(path)
            ?: error("Resource not found: $path")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    fun replacePlaceholders(template: String, placeholders: Placeholders): String {
        var result = template
        placeholders.asMap().forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        return result
    }
}
