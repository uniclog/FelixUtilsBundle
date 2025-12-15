package io.github.uniclog.docker.runner.model

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField

class InputDialog : DialogWrapper(true) {
    private val mainPanel = JPanel()
    private val field1 = JTextField()
    private val field2 = JTextField()

    init {
        title = "Docker Runner"
        init()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)
        // mainPanel.add(JLabel("Field 1:"))
        // mainPanel.add(field1)
        // mainPanel.add(JLabel("Field 2:"))
        // mainPanel.add(field2)
    }

    override fun createCenterPanel(): JComponent = mainPanel

    fun getData(): Map<String, String> {
        return mapOf(
            "field1" to field1.text,
            "field2" to field2.text
        )
    }
}