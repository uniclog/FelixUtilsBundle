package io.github.uniclog.felixutils.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.application.ApplicationManager
import io.github.uniclog.felixutils.dialog.UploadServerDialog
import io.github.uniclog.felixutils.model.EndpointConnection
import io.github.uniclog.felixutils.model.UploadContext
import io.github.uniclog.felixutils.model.UploadTargetType
import io.github.uniclog.felixutils.service.ConnectionValidator
import io.github.uniclog.felixutils.service.FelixHttpService
import io.github.uniclog.felixutils.service.UploadContextResolver
import io.github.uniclog.felixutils.settings.FelixSettings
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.net.http.HttpResponse

class UploadToServerAction : AnAction() {
    private val contextResolver = UploadContextResolver()
    private val httpService = FelixHttpService()

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(event: AnActionEvent) {
        val context = contextResolver.resolve(event) ?: return
        val settings = FelixSettings.getInstance()
        val dialog = UploadServerDialog(
            project = event.project,
            uploadContext = context,
            initialConnection = initialConnection(context.targetType)
        )

        if (!dialog.showAndGet()) {
            return
        }

        val connection = dialog.connection()
        val jsonResourceName = dialog.jsonResourceName()
        val validationError = ConnectionValidator.validate(connection)
        if (validationError != null) {
            Messages.showErrorDialog(validationError, "Upload to Server")
            return
        }
        if (context.targetType == UploadTargetType.JSON && jsonResourceName.isBlank()) {
            Messages.showErrorDialog("JSON config name must not be blank", "Upload to Server")
            return
        }

        saveConnection(settings, context.targetType, connection)
        upload(event, context, connection, jsonResourceName)
    }

    override fun update(event: AnActionEvent) {
        val context = contextResolver.resolve(event)
        event.presentation.isEnabledAndVisible = context != null
    }

    private fun initialConnection(targetType: UploadTargetType): EndpointConnection {
        val state = FelixSettings.getInstance().state
        return when (targetType) {
            UploadTargetType.JSON -> state.ankeyConnection()
            UploadTargetType.BUNDLE -> state.felixConnection()
        }
    }

    private fun saveConnection(
        settings: FelixSettings,
        targetType: UploadTargetType,
        connection: EndpointConnection
    ) {
        val state = settings.state
        when (targetType) {
            UploadTargetType.JSON -> {
                state.url = connection.url
                state.username = connection.username
                state.password = connection.password
            }

            UploadTargetType.BUNDLE -> {
                state.felixUrl = connection.url
                state.felixUsername = connection.username
                state.felixPassword = connection.password
            }
        }
    }

    private fun upload(
        event: AnActionEvent,
        context: UploadContext,
        connection: EndpointConnection,
        jsonResourceName: String
    ) {
        val project = event.project
        val uploadDisplayName = when (context.targetType) {
            UploadTargetType.JSON -> jsonResourceName
            UploadTargetType.BUNDLE -> context.file.name
        }
        object : Task.Backgroundable(project, "Uploading ${context.file.name}", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Uploading ${context.file.name}"
                runCatching {
                    val response = when (context.targetType) {
                        UploadTargetType.JSON -> httpService.sendJson(
                            connection.withJsonFileName(jsonResourceName),
                            context.file.contentsToByteArray()
                        )
                        UploadTargetType.BUNDLE -> httpService.sendBundle(
                            connection,
                            context.file.name,
                            context.file.contentsToByteArray()
                        )
                    }
                    showResult(response, context, uploadDisplayName)
                }.onFailure { error ->
                    showError(error.message ?: "Unknown error")
                }
            }
        }.queue()
    }

    private fun showResult(response: HttpResponse<String>, context: UploadContext, uploadDisplayName: String) {
        val statusCode = response.statusCode()
        if (statusCode in 200..299) {
            showInfo("$uploadDisplayName uploaded successfully.")
            return
        }
        if (context.targetType == UploadTargetType.BUNDLE && statusCode in 300..399) {
            val redirectLocation = response.headers().firstValue("Location").orElse("")
            if (redirectLocation.contains("/system/console")) {
                val message = buildString {
                    append(uploadDisplayName)
                    append(" uploaded successfully.")
                }
                showInfo(message)
                return
            }
        }

        val responseBody = response.body().trim().take(500)
        val redirectLocation = response.headers().firstValue("Location").orElse("").trim()
        val message = buildString {
            append("Upload failed.")
            if (redirectLocation.isNotEmpty()) {
                append("\nRedirect: ")
                append(redirectLocation)
            }
            if (responseBody.isNotEmpty()) {
                append("\n\n")
                append(responseBody)
            }
        }
        showError(message)
    }

    private fun showInfo(message: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showInfoMessage(message, "Upload to Server")
        }
    }

    private fun showError(message: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(message, "Upload to Server")
        }
    }

    private fun EndpointConnection.withJsonFileName(fileNameWithoutExtension: String): EndpointConnection {
        val normalizedBaseUrl = url.trimEnd('/')
        val encodedFileName = URLEncoder.encode(fileNameWithoutExtension, StandardCharsets.UTF_8)
            .replace("+", "%20")
        return copy(url = "$normalizedBaseUrl/$encodedFileName")
    }
}
