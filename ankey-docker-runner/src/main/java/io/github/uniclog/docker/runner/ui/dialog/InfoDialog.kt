package io.github.uniclog.docker.runner.ui.dialog

import com.intellij.openapi.ui.DialogWrapper
import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea

class InfoDialog(ankeyPath: AnkeyPath?) : DialogWrapper(true) {
    private val textArea = JTextArea(40, 90).apply {
        fun readCompose(): String {
            if (ankeyPath != null) {
                val compose = File(ankeyPath.getComposePath())
                if (compose.exists()) {
                    return compose.readText()
                }
            }
            return "Compose file not found!"
        }
        text = readCompose()

        lineWrap = true
        wrapStyleWord = true
        caretPosition = 0
        isEditable = false
    }

    init {
        title = "Info"
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