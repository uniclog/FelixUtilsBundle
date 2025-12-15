package io.github.uniclog.docker.runner

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages

class Activator : AppLifecycleListener {
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