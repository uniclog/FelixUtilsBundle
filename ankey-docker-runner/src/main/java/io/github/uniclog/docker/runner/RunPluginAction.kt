package io.github.uniclog.docker.runner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.github.uniclog.docker.runner.model.InputDialog

class RunPluginAction : AnAction("Run Plugin") {
    override fun actionPerformed(e: AnActionEvent) {
        val dialog = InputDialog()
        if (dialog.showAndGet()) {
            val inputData = dialog.getData()
            PluginLogic.run(inputData)
        }
    }
}