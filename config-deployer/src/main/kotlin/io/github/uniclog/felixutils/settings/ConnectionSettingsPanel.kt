package io.github.uniclog.felixutils.settings

import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import io.github.uniclog.felixutils.model.EndpointConnection
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JPanel
import javax.swing.JTextField

class ConnectionSettingsPanel(
    private val title: String,
    initialConnection: EndpointConnection,
    private val defaultConnection: EndpointConnection,
    onTestConnection: (EndpointConnection) -> Unit
) {
    private val urlField = JTextField(initialConnection.url)
    private val userField = JTextField(initialConnection.username)
    private val passField = JPasswordField(initialConnection.password)
    private val testButton = JButton("Test Connection").apply {
        addActionListener { onTestConnection(readConnection()) }
    }
    private val resetButton = JButton("Reset Connection").apply {
        addActionListener { writeConnection(defaultConnection) }
    }
    private val actionsPanel = JPanel().apply {
        add(testButton)
        add(resetButton)
    }

    fun appendTo(builder: FormBuilder): FormBuilder {
        return builder
            .addLabeledComponent(JBLabel("$title URL:"), urlField, 1, false)
            .addLabeledComponent(JBLabel("$title Username:"), userField, 1, false)
            .addLabeledComponent(JBLabel("$title Password:"), passField, 1, false)
            .addComponent(actionsPanel, 1)
    }

    fun isModified(connection: EndpointConnection): Boolean {
        return readConnection() != connection
    }

    fun writeConnection(connection: EndpointConnection) {
        urlField.text = connection.url
        userField.text = connection.username
        passField.text = connection.password
    }

    fun readConnection(): EndpointConnection {
        return EndpointConnection(
            url = urlField.text,
            username = userField.text,
            password = String(passField.password)
        )
    }
}
