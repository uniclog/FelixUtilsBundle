package io.github.uniclog.plugina

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.ide.AppLifecycleListener

class PluginA : AppLifecycleListener {
    override fun appStarted() {
        println(">>> Plugin A started")
        ApplicationManager.getApplication().invokeLater {
            Messages.showMessageDialog(
                "Plugin A started!",
                "Plugin A",
                Messages.getInformationIcon()
            )
        }
    }
}