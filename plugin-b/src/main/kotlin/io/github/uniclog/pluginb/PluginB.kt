package io.github.uniclog.pluginb

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.ide.AppLifecycleListener

class PluginB : AppLifecycleListener {
    override fun appStarted() {
        println(">>> Plugin B started")
        ApplicationManager.getApplication().invokeLater {
            Messages.showMessageDialog(
                "Plugin B started!",
                "Plugin B",
                Messages.getInformationIcon()
            )
        }
    }
}