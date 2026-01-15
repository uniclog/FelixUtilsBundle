package io.github.uniclog.docker.runner.model

enum class AnkeyComponent(val fileName: String) {
    CORE("core"),
    POSTGRES("postgres"),
    OPENSEARCH("opensearch"),
    KAFKA("kafka"),
    KAFKA_UI("kafka-ui"),
    BPMN("bpmn"),
    NONE(""),
}

data class AnkeyComponentState(val component: AnkeyComponent, var isActivate: Boolean)