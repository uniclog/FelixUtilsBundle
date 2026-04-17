package io.github.uniclog.felixutils.dialog

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import io.github.uniclog.felixutils.model.EndpointConnection
import io.github.uniclog.felixutils.model.UploadContext
import io.github.uniclog.felixutils.model.UploadTargetType
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPasswordField
import javax.swing.JTextField

class UploadServerDialog(
    project: Project?,
    private val uploadContext: UploadContext,
    initialConnection: EndpointConnection
) : DialogWrapper(project) {
    private val resourceNameField = JTextField(uploadContext.file.nameWithoutExtension)
    private val urlField = JTextField(initialConnection.url)
    private val usernameField = JTextField(initialConnection.username)
    private val passwordField = JPasswordField(initialConnection.password)

    init {
        title = "Upload to Server"
        init()
        okAction.putValue(Action.NAME, "Upload")
    }

    fun connection(): EndpointConnection {
        return EndpointConnection(
            url = urlField.text.trim(),
            username = usernameField.text.trim(),
            password = String(passwordField.password).trim()
        )
    }

    fun jsonResourceName(): String {
        return resourceNameField.text.trim()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()
            .addVerticalGap(8)

        when (uploadContext.targetType) {
            UploadTargetType.JSON -> {
                builder.addLabeledComponent(JBLabel("JSON Config: "), resourceNameField, false)
            }

            UploadTargetType.BUNDLE -> {
                builder.addLabeledComponent(JBLabel("Bundle: "), JTextField(uploadContext.file.name), false)
            }
        }

        return builder
            .addLabeledComponent(JBLabel("URL:"), urlField, 1, false)
            .addLabeledComponent(JBLabel("Login:"), usernameField, 1, false)
            .addLabeledComponent(JBLabel("Password:"), passwordField, 1, false)
            .panel
    }
}
