package io.github.uniclog.quicktools

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import org.cef.network.CefRequest
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants

class QuickToolsToolWindowFactory : ToolWindowFactory {
    private val LOG = Logger.getInstance(QuickToolsToolWindowFactory::class.java)

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            val label = JLabel("JCEF (Chromium) is not supported on this platform", SwingConstants.CENTER)
            val content = ContentFactory.getInstance().createContent(label, "", false)
            toolWindow.contentManager.addContent(content)
            return
        }
        
        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        val browser = JBCefBrowser("https://www.google.com")
        
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadingStateChange(browser: CefBrowser?, isLoading: Boolean, canGoBack: Boolean, canGoForward: Boolean) {
                LOG.info("JCEF Loading state changed: isLoading=$isLoading")
            }

            override fun onLoadError(browser: CefBrowser?, frame: CefFrame?, errorCode: org.cef.handler.CefLoadHandler.ErrorCode?, errorText: String?, failedUrl: String?) {
                LOG.error("JCEF Load Error: $errorText (code: $errorCode) for URL: $failedUrl")
            }
        }, browser.cefBrowser)

        // Toolbar for control
        val toolbar = JBPanel<JBPanel<*>>()
        val refreshButton = JButton("Refresh Google")
        refreshButton.addActionListener { browser.loadURL("https://www.google.com") }
        toolbar.add(refreshButton)
        
        val myIpButton = JButton("Load MyIP")
        myIpButton.addActionListener { browser.loadURL("https://myip.ru") }
        toolbar.add(myIpButton)
        
        panel.add(toolbar, BorderLayout.NORTH)
        panel.add(browser.component, BorderLayout.CENTER)
        
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
