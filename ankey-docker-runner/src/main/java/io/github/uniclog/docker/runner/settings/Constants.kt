package io.github.uniclog.docker.runner.settings

object Constants {
    const val ANKEY_VER_11 = "1.11"
    const val ANKEY_VER_10 = "1.10"
    const val COMPOSE_FILE_NAME = "docker-compose.ankey.yml"
    const val COMPOSE_FILE_TEMPLATE = "docker/docker-compose.%s.yml"

    const val PORT_HTTP = 8080
    const val PORT_DEBUG = 5005
    const val PORT_JMX = 9010

    const val PORT_KAFKA = 9092
    const val PORT_KAFKA_UI = 8090

    const val PORT_POSTGRES = 5432

    const val PORT_OPENSEARCH_HTTP = 9200
    const val PORT_OPENSEARCH_TRANSPORT = 9300

    const val PORT_BPMN_HTTP = 10081
    const val PORT_BPMN_DEBUG = 10007
}
