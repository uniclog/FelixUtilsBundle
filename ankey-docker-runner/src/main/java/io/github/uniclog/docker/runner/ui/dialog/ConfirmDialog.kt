package io.github.uniclog.docker.runner.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import java.awt.Component
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ConfirmDialog(val message: String, val okActionText: String) : DialogWrapper(true) {

    init {
        title = "Confirm Generation"
        init()
    }

    override fun createActions(): Array<Action> = arrayOf(okAction, cancelAction)

    override fun getOKAction(): Action {
        val action = super.getOKAction()
        action.putValue(Action.NAME, okActionText)
        return action
    }

    override fun getCancelAction(): Action {
        val action = super.getCancelAction()
        action.putValue(Action.NAME, "Cancel")
        return action
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(15, 20, 15, 20) // Отступы

        val messageLabel = JLabel(
            """
                <html>
                <div style="text-align:center">
                $message
                </div>
                </html>
                """.trimIndent()
        ).apply {
            alignmentX = Component.CENTER_ALIGNMENT
        }

        panel.add(messageLabel)
        return panel
    }
}