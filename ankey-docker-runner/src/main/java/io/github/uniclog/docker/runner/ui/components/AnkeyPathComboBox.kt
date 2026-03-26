package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.project.Project
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.model.ColorModel.CM_RED
import io.github.uniclog.docker.runner.model.ColorModel.CM_WHITE
import io.github.uniclog.docker.runner.service.AnkeyPathService
import io.github.uniclog.docker.runner.settings.AnkeySettings
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JList

class AnkeyPathComboBox(
    private val onChange: (AnkeyPath) -> Unit
) {
    val comboBox = JComboBox<AnkeyPath>().apply {
        prototypeDisplayValue = AnkeyPath(
            absolutePath = "some/very/long/path/to/project/module/ankey",
            root = ""
        )

        renderer = object : ColoredListCellRenderer<AnkeyPath>() {
            override fun customizeCellRenderer(
                list: JList<out AnkeyPath>,
                value: AnkeyPath?,
                index: Int,
                selected: Boolean,
                hasFocus: Boolean
            ) {
                if (value == null) return
                append(value.absolutePath)
                toolTipText = value.absolutePath
            }
        }
    }

    fun init(project: Project?) {
        val paths = AnkeyPathService.findAnkeyPaths(project)
        comboBox.model = DefaultComboBoxModel(paths.toTypedArray())

        restoreSaved(paths)
        updateTooltip()

        comboBox.addActionListener {
            updateTooltip()
            onChange(selected())
        }
    }

    fun selected(): AnkeyPath =
        comboBox.selectedItem as? AnkeyPath ?: AnkeyPath("", "")

    fun updateBackground(isValid: Boolean) {
        comboBox.background =
            if (isValid) CM_WHITE else JBColor(CM_RED, CM_RED)
    }

    private fun updateTooltip() {
        comboBox.toolTipText = selected().absolutePath.ifBlank { null }
    }

    private fun restoreSaved(paths: Set<AnkeyPath>) {
        val saved = AnkeySettings.instance.getSelectedPath()
        paths.firstOrNull {
            it.absolutePath == saved.absolutePath
        }?.let {
            comboBox.selectedItem = it
            updateBackground(true)
        } ?: run {
            updateBackground(false)
            comboBox.toolTipText = "Saved path does not exist!"
        }
    }
}
