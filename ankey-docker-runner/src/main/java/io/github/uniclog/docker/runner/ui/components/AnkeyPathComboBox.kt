package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.model.ColorModel.CM_RED
import io.github.uniclog.docker.runner.model.ColorModel.CM_WHITE
import io.github.uniclog.docker.runner.service.AnkeyPathService
import io.github.uniclog.docker.runner.settings.AnkeySettings
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class AnkeyPathComboBox(
    private val onChange: (AnkeyPath) -> Unit
) {
    val comboBox = JComboBox<AnkeyPath>()

    fun init(project: Project?) {
        val paths = AnkeyPathService.findAnkeyPaths(project)
        comboBox.model = DefaultComboBoxModel(paths.toTypedArray())

        restoreSaved(paths)

        comboBox.addActionListener {
            onChange(selected())
        }
    }

    fun selected(): AnkeyPath =
        comboBox.selectedItem as? AnkeyPath ?: AnkeyPath("", "")

    fun updateBackground(isValid: Boolean) {
        comboBox.background =
            if (isValid) CM_WHITE else JBColor(CM_RED, CM_RED)
    }

    private fun restoreSaved(paths: Set<AnkeyPath>) {
        val saved = AnkeySettings.instance.getSelectedPath()
        paths.firstOrNull {
            it.absolutePath == saved.absolutePath
        } ?.let {
            comboBox.selectedItem = it
            updateBackground(true)
        } ?: run {
            updateBackground(false)
            comboBox.toolTipText = "Saved path does not exist!"
        }
    }
}
