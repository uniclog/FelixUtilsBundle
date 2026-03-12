package io.github.uniclog.docker.runner.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea

class PortsDialog(text: String) : DialogWrapper(true) {
    private val textArea = JTextArea(20, 70).apply {
        this.text = text
        lineWrap = true
        wrapStyleWord = true
        caretPosition = 0
        isEditable = false
    }

    init {
        title = "Compose Ports"
        init()
    }

    override fun createActions(): Array<out Action?> =
        arrayOf(cancelAction)

    override fun getCancelAction(): Action {
        val action = super.getCancelAction()
        action.putValue(Action.NAME, "Close")
        return action
    }

    override fun createCenterPanel(): JComponent =
        JScrollPane(textArea)
}
