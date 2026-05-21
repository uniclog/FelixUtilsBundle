package io.github.uniclog.quicktools

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import javax.swing.JLabel
import javax.swing.SwingConstants

class QuickToolsToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            val label = JLabel("JCEF (Chromium) is not supported on this platform", SwingConstants.CENTER)
            val content = ContentFactory.getInstance().createContent(label, "", false)
            toolWindow.contentManager.addContent(content)
            return
        }
        
        val browser = JBCefBrowser("https://myip.ru")
        
        // Создаем действие обновления для тулбара окна
        val refreshAction = object : AnAction("Refresh", "Reload page", AllIcons.Actions.Refresh) {
            override fun actionPerformed(e: AnActionEvent) {
                browser.loadURL("https://myip.ru")
            }
        }
        
        // Добавляем действие в заголовок Tool Window (справа сверху)
        toolWindow.setTitleActions(listOf(refreshAction))
        
        // Также можно добавить в контекстное меню (шестеренка)
        val actionGroup = DefaultActionGroup()
        actionGroup.add(refreshAction)
        toolWindow.setAdditionalGearActions(actionGroup)

        val content = ContentFactory.getInstance().createContent(browser.component, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
