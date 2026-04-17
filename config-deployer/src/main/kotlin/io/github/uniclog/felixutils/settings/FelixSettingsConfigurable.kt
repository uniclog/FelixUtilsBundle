package io.github.uniclog.felixutils.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsContexts
import com.intellij.util.ui.FormBuilder
import io.github.uniclog.felixutils.model.EndpointConnection
import io.github.uniclog.felixutils.service.ConnectionValidator
import io.github.uniclog.felixutils.service.FelixHttpService
import javax.swing.JComponent
import javax.swing.JPanel

class FelixSettingsConfigurable : Configurable {
    private val settings = FelixSettings.getInstance()
    private val httpService = FelixHttpService()

    private var ankeyPanel: ConnectionSettingsPanel? = null
    private var felixPanel: ConnectionSettingsPanel? = null

    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "uFelixUtils"
    }

    override fun createComponent(): JComponent {
        val state = settings.state
        ankeyPanel = ConnectionSettingsPanel("Ankey", state.ankeyConnection(), ::testConnection)
        felixPanel = ConnectionSettingsPanel("Felix", state.felixConnection(), ::testConnection)

        return FormBuilder.createFormBuilder()
            .let { ankeyPanel!!.appendTo(it) }
            .addSeparator(10)
            .let { felixPanel!!.appendTo(it) }
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val state = settings.state
        return ankeyPanel?.isModified(state.ankeyConnection()) == true ||
                felixPanel?.isModified(state.felixConnection()) == true
    }

    override fun apply() {
        val state = settings.state

        ankeyPanel?.readConnection()?.let { connection ->
            state.url = connection.url
            state.username = connection.username
            state.password = connection.password
        }

        felixPanel?.readConnection()?.let { connection ->
            state.felixUrl = connection.url
            state.felixUsername = connection.username
            state.felixPassword = connection.password
        }
    }

    override fun reset() {
        val state = settings.state
        ankeyPanel?.writeConnection(state.ankeyConnection())
        felixPanel?.writeConnection(state.felixConnection())
    }

    private fun testConnection(connection: EndpointConnection) {
        val normalizedConnection = connection.copy(
            url = connection.url.trim(),
            username = connection.username.trim(),
            password = connection.password.trim()
        )
        val validationError = ConnectionValidator.validate(normalizedConnection)
        if (validationError != null) {
            Messages.showErrorDialog(validationError, "Connection Test Failed")
            return
        }

        try {
            val response = httpService.testConnection(normalizedConnection)

            Messages.showInfoMessage(
                "Response code: ${response.statusCode()}",
                "Connection Test"
            )
        } catch (ex: Exception) {
            Messages.showErrorDialog(
                ex.message ?: "Unknown error",
                "Connection Test Failed"
            )
        }
    }
}
