package io.github.uniclog.docker.runner.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.FormBuilder
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.AnkeyPathService
import io.github.uniclog.docker.runner.settings.AnkeySettings
import io.github.uniclog.docker.runner.ui.components.AnkeyPathComboBox
import io.github.uniclog.docker.runner.ui.components.ComposeGeneratePanel
import io.github.uniclog.docker.runner.ui.components.ComposeStatusPanel
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent

class RunDockerDialog(val event: AnActionEvent) : DialogWrapper(true) {

    companion object {
        const val UP_EXIT_CODE = 1000
        const val STOP_EXIT_CODE = 1001
        const val DOWN_EXIT_CODE = 1002
    }

    //private val prefixField = JTextField(AnkeySettings.instance.getAnkeyPrefix())
    //private val okAction = super.getOKAction()

    private val pathSelector = AnkeyPathComboBox(
        onChange = ::onAnkeyPathChanged
    )

    private val composeStatus = ComposeStatusPanel(
        getAnkeyPath = { pathSelector.selected() },
        upAction = okAction
    )

    private val customUpAction = object : DialogWrapperAction("Run Docker") {
        init {
            putValue(DEFAULT_ACTION, true)
            putValue(SMALL_ICON, AllIcons.Actions.Execute)
        }

        override fun doAction(e: ActionEvent?) {
            close(UP_EXIT_CODE)
        }
    }

    private val customStopAction = object : DialogWrapperAction("Stop Docker") {
        init {
            putValue(SMALL_ICON, AllIcons.Actions.Pause)
        }

        override fun doAction(e: ActionEvent?) {
            close(STOP_EXIT_CODE)
        }
    }

    private val customDownAction = object : DialogWrapperAction("Down Docker") {
        init {
            putValue(SMALL_ICON, AllIcons.General.Warning)
        }

        override fun doAction(e: ActionEvent?) {
            close(DOWN_EXIT_CODE)
        }
    }

    init {
        title = "Docker Runner"
        init()
    }

    override fun createActions(): Array<out Action?> {
        return arrayOf(
            customDownAction,
            customStopAction,
            customUpAction,
            cancelAction
        )
    }

    private fun onAnkeyPathChanged(path: AnkeyPath) {
        pathSelector.updateBackground(isValid = AnkeyPathService.isValid(path))
        composeStatus.update()
        AnkeySettings.instance.setSelectedPath(path)
    }

    // override fun getOKAction(): Action {
    //     okAction.apply { putValue(Action.NAME, "Run Docker") }
    //     close(UP_EXIT_CODE)
    //     return TODO("Provide the return value")
    // }

    override fun createCenterPanel(): JComponent {
        pathSelector.init(event.project)

        val generatePanel = ComposeGeneratePanel(
            getAnkeyPath = { pathSelector.selected() },
            onGenerated = { prefix ->
                AnkeySettings.instance.setAnkeyPrefix(prefix)
                composeStatus.update()
            }
        )

        composeStatus.update()

        return FormBuilder.createFormBuilder()
            .addLabeledComponent("Select ankey folder:", pathSelector.comboBox)
            .addSeparator()
            .addLabeledComponent("Docker compose:", generatePanel.panel)
            //.addLabeledComponent("Ankey prefix:", prefixField)
            .addLabeledComponent("", composeStatus.panel)
            .panel
    }

    fun getDialogData(): Map<String, String> {
        val selected = pathSelector.selected()
        // save
        AnkeySettings.instance.setSelectedPath(selected)

        return mapOf(
            "ankeyPath" to selected.absolutePath,
            "composePath" to selected.getComposePath()
        )
    }
}

