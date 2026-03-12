package io.github.uniclog.docker.runner.model

import io.github.uniclog.docker.runner.model.Constants.PORT_DEBUG
import io.github.uniclog.docker.runner.model.Constants.PORT_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_JMX
import io.github.uniclog.docker.runner.model.Constants.PORT_KAFKA
import io.github.uniclog.docker.runner.model.Constants.PORT_KAFKA_UI
import io.github.uniclog.docker.runner.model.Constants.PORT_BPMN_DEBUG
import io.github.uniclog.docker.runner.model.Constants.PORT_BPMN_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_OPENSEARCH_HTTP
import io.github.uniclog.docker.runner.model.Constants.PORT_OPENSEARCH_TRANSPORT
import io.github.uniclog.docker.runner.model.Constants.PORT_POSTGRES

data class Placeholders(
    var num: String = "",
    var portHttp: String = "$PORT_HTTP",
    var portDebug: String = "$PORT_DEBUG",
    var portJmx: String = "$PORT_JMX",
    var portPostgres: String = "$PORT_POSTGRES",
    var portKafka: String = "$PORT_KAFKA",
    var portKafkaUi: String = "$PORT_KAFKA_UI",
    var portBpmnHttp: String = "$PORT_BPMN_HTTP",
    var portBpmnDebug: String = "$PORT_BPMN_DEBUG",
    var portOpensearch: String = "$PORT_OPENSEARCH_HTTP",
    var portOpensearch2: String = "$PORT_OPENSEARCH_TRANSPORT",
) {
    fun asMap(): Map<String, String> = mapOf(
        "NUM" to num,
        "PORT_HTTP" to portHttp,
        "PORT_DEBUG" to portDebug,
        "PORT_JMX" to portJmx,
        "PORT_POSTGRES" to portPostgres,
        "PORT_KAFKA" to portKafka,
        "PORT_KAFKA_UI" to portKafkaUi,
        "PORT_BPMN_HTTP" to portBpmnHttp,
        "PORT_BPMN_DEBUG" to portBpmnDebug,
        "PORT_OPENSEARCH" to portOpensearch,
        "PORT_OPENSEARCH_2" to portOpensearch2,
    )
}
