package io.github.uniclog.docker.runner.compose

import io.github.uniclog.docker.runner.model.AnkeyComponent.*
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.model.Constants.COMPOSE_FILE_NAME
import io.github.uniclog.docker.runner.model.Constants.COMPOSE_FILE_TEMPLATE
import io.github.uniclog.docker.runner.model.Constants.PORT_BPMN_DEBUG
import io.github.uniclog.docker.runner.model.Constants.PORT_BPMN_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_DEBUG
import io.github.uniclog.docker.runner.model.Constants.PORT_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_JMX
import io.github.uniclog.docker.runner.model.Constants.PORT_KAFKA
import io.github.uniclog.docker.runner.model.Constants.PORT_KAFKA_UI
import io.github.uniclog.docker.runner.model.Constants.PORT_OPENSEARCH_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_OPENSEARCH_TRANSPORT
import io.github.uniclog.docker.runner.model.Constants.PORT_POSTGRES
import io.github.uniclog.docker.runner.model.Placeholders

object ComposeGenerator {

    fun generate(
        ankeyPath: AnkeyPath,
        ankeyPrefix: String,
        services: List<AnkeyComponentState> = listOf(),
        isMapAnkeyVolume: Boolean = false
    ): String? {
        if (services.isEmpty()) {
            return null
        }

        // delete old data only after input validation
        ComposeFileWriter.deleteGeneratedFiles(
            ankeyPath.getComposeBasePath().removeSuffix(COMPOSE_FILE_NAME)
        )

        val prefix = if (ankeyPrefix.isEmpty()) "" else "-${ankeyPrefix}"
        val placeholders = buildPlaceholders(prefix, services) ?: return null

        val template = buildComposeTemplate(services, isMapAnkeyVolume)
        val compose = replacePlaceholders(template, placeholders)

        ComposeFileWriter.writeComposeFile(ankeyPath.getComposePath(), compose)
        ComposeFileWriter.copyDockerfiles(ankeyPath.getComposeBasePath(), services, isMapAnkeyVolume)

        return placeholders.num
    }

    private fun buildPlaceholders(
        prefix: String,
        services: List<AnkeyComponentState>
    ): Placeholders? {
        val basePorts = linkedSetOf<Int>()
        services.forEach { state ->
            when (state.component) {
                CORE -> {
                    basePorts.add(PORT_HTTP)
                    basePorts.add(PORT_DEBUG)
                    basePorts.add(PORT_JMX)
                }

                POSTGRES -> {
                    basePorts.add(PORT_POSTGRES)
                }

                KAFKA -> {
                    basePorts.add(PORT_KAFKA)
                }

                KAFKA_UI -> {
                    basePorts.add(PORT_KAFKA_UI)
                }

                BPMN -> {
                    basePorts.add(PORT_BPMN_HTTP)
                    basePorts.add(PORT_BPMN_DEBUG)
                }

                OPENSEARCH -> {
                    basePorts.add(PORT_OPENSEARCH_HTTP)
                    basePorts.add(PORT_OPENSEARCH_TRANSPORT)
                }

                else -> {}
            }
        }

        val allocation = PortAllocator(basePorts = basePorts.toList()).allocate(prefix) ?: return null

        val num = allocation.projectName
        val offset = allocation.offset

        return Placeholders(
            num = if (num == "") "" else num,

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

    fun buildComposeTemplate(selectedServices: List<AnkeyComponentState>, isMapAnkeyVolume: Boolean = false): String {
        val base = ComposeTemplateLoader.load(COMPOSE_FILE_TEMPLATE.format("base"))
        val network = ComposeTemplateLoader.load(COMPOSE_FILE_TEMPLATE.format("network"))

        val services = selectedServices
            .filter { it.component.fileName.isNotBlank() }
            .joinToString("\n\n") { state ->
                var template = ComposeTemplateLoader.load(COMPOSE_FILE_TEMPLATE.format(state.component.fileName))
                if (state.component == CORE && isMapAnkeyVolume) {
                    template = template.replace("# volumes:", "volumes:")
                    template = template.replace("#   - \"./ankey:/opt/ankey\"", "  - \"./ankey:/opt/ankey\"")
                }
                template
            }

        val compose = buildString {
            append(base.trimEnd())
            append("\n\n")
            append(services)
            append("\n\n")
            append(network)
        }

        return compose
    }

    fun replacePlaceholders(template: String, placeholders: Placeholders): String {
        var result = template
        placeholders.asMap().forEach { (key, value) ->
            result = result.replace("\${$key}", value)
        }
        return result
    }
}
