package io.github.uniclog.felixutils.service

import io.github.uniclog.felixutils.model.EndpointConnection
import java.nio.charset.StandardCharsets
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Base64

class FelixHttpService(
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(DEFAULT_TIMEOUT)
        .build()
) {
    fun sendJson(connection: EndpointConnection, payload: ByteArray): HttpResponse<String> {
        val request = requestBuilder(connection)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofByteArray(payload))
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun sendJson(connection: EndpointConnection, payload: String): HttpResponse<String> {
        return sendJson(connection, payload.toByteArray(StandardCharsets.UTF_8))
    }

    fun sendBundle(
        connection: EndpointConnection,
        fileName: String,
        fileContent: ByteArray
    ): HttpResponse<String> {
        val boundary = "FelixUtilsBoundary${System.currentTimeMillis()}"
        val request = requestBuilder(connection)
            .header("Content-Type", "multipart/form-data; boundary=$boundary")
            .POST(HttpRequest.BodyPublishers.ofByteArray(bundleMultipartBody(boundary, fileName, fileContent)))
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    fun testConnection(connection: EndpointConnection): HttpResponse<String> {
        val request = requestBuilder(connection)
            .GET()
            .build()

        return client.send(request, HttpResponse.BodyHandlers.ofString())
    }

    private fun requestBuilder(connection: EndpointConnection): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(connection.url))
            .timeout(DEFAULT_TIMEOUT)

        if (connection.username.isNotBlank() || connection.password.isNotBlank()) {
            val auth = "${connection.username}:${connection.password}"
            val basicAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            builder.header("Authorization", "Basic $basicAuth")
        }

        return builder
    }

    private fun bundleMultipartBody(boundary: String, fileName: String, fileContent: ByteArray): ByteArray {
        val lineBreak = "\r\n"
        val parts = mutableListOf<ByteArray>()

        fun addField(name: String, value: String) {
            parts += "--$boundary$lineBreak".toByteArray(StandardCharsets.UTF_8)
            parts += "Content-Disposition: form-data; name=\"$name\"$lineBreak$lineBreak"
                .toByteArray(StandardCharsets.UTF_8)
            parts += "$value$lineBreak".toByteArray(StandardCharsets.UTF_8)
        }

        addField("action", "install")
        addField("bundlestart", "true")
        parts += "--$boundary$lineBreak".toByteArray(StandardCharsets.UTF_8)
        parts += "Content-Disposition: form-data; name=\"bundlefile\"; filename=\"$fileName\"$lineBreak"
            .toByteArray(StandardCharsets.UTF_8)
        parts += "Content-Type: application/java-archive$lineBreak$lineBreak".toByteArray(StandardCharsets.UTF_8)
        parts += fileContent
        parts += lineBreak.toByteArray(StandardCharsets.UTF_8)
        parts += "--$boundary--$lineBreak".toByteArray(StandardCharsets.UTF_8)

        val totalSize = parts.sumOf { it.size }
        val body = ByteArray(totalSize)
        var offset = 0
        for (part in parts) {
            part.copyInto(body, destinationOffset = offset)
            offset += part.size
        }
        return body
    }

    companion object {
        private val DEFAULT_TIMEOUT: Duration = Duration.ofMillis(3000)
    }
}
