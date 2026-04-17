package io.github.uniclog.felixutils.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import io.github.uniclog.felixutils.model.EndpointConnection
import io.github.uniclog.felixutils.model.UploadContext
import io.github.uniclog.felixutils.model.UploadTargetType
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class UploadServerDialog(
    project: Project?,
    private val uploadContext: UploadContext,
    initialConnection: EndpointConnection
) : DialogWrapper(project) {
    private val urlField = JTextField(initialConnection.url)
    private val usernameField = JTextField(initialConnection.username)
    private val passwordField = JPasswordField(initialConnection.password)

    init {
        title = "Upload to Server"
        init()
        okAction.putValue(NAME, "Upload")
    }

    fun connection(): EndpointConnection {
        return EndpointConnection(
            url = urlField.text.trim(),
            username = usernameField.text.trim(),
            password = String(passwordField.password).trim()
        )
    }

    override fun createCenterPanel(): JComponent {
        val targetName = when (uploadContext.targetType) {
            UploadTargetType.JSON -> "JSON config"
            UploadTargetType.BUNDLE -> "Bundle"
        }

        return FormBuilder.createFormBuilder()
            .addVerticalGap(8)
            .addLabeledComponent(JBLabel("$targetName: "), JTextField(uploadContext.file.name), false)
            .addLabeledComponent(JBLabel("URL:"), urlField, 1, false)
            .addLabeledComponent(JBLabel("Login:"), usernameField, 1, false)
            .addLabeledComponent(JBLabel("Password:"), passwordField, 1, false)
            .panel
    }
}
