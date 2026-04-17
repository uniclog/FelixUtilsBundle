package io.github.uniclog.felixutils.service

import io.github.uniclog.felixutils.model.EndpointConnection

object ConnectionValidator {
    fun validate(connection: EndpointConnection): String? {
        if (connection.url.isBlank()) {
            return "URL cannot be empty."
        }

        if (!connection.url.startsWith("http://") && !connection.url.startsWith("https://")) {
            return "Invalid URL, must start with http:// or https://"
        }

        val hasUsername = connection.username.isNotBlank()
        val hasPassword = connection.password.isNotBlank()
        if (hasUsername.xor(hasPassword)) {
            return "Username and password must both be filled."
        }

        return null
    }
}
