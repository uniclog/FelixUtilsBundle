package io.github.uniclog.quicktools

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JButton
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
        
        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        val browser = JBCefBrowser("https://myip.ru")
        
        // Toolbar for control
        val toolbar = JBPanel<JBPanel<*>>()
        val refreshButton = JButton("Refresh")
        refreshButton.addActionListener { browser.loadURL("https://myip.ru") }
        toolbar.add(refreshButton)
        
        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(browser.component, BorderLayout.CENTER)
        
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
