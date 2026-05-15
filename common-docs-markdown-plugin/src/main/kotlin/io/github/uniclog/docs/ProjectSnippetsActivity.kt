package io.github.uniclog.docs

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.io.File

const val GROUP_PREFIX = "Project: "
private const val IDEA_LIVE_TEMPLATES = ".idea/liveTemplates"

class ProjectSnippetsActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        reloadTemplates(project)
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                if (events.any { it.path.contains(IDEA_LIVE_TEMPLATES) }) {
                    reloadTemplates(project)
                }
            }
        })
    }

    private fun reloadTemplates(project: Project) {
        val projectPath = project.basePath ?: return
        val templatesDir = File(projectPath, IDEA_LIVE_TEMPLATES)
        
        if (!templatesDir.exists() || !templatesDir.isDirectory) return

        val templateSettings = TemplateSettings.getInstance()

        templateSettings.templates
            .filter { it.groupName.startsWith(GROUP_PREFIX) }
            .forEach { templateSettings.removeTemplate(it) }

        val files = templatesDir.listFiles { _, name -> name.endsWith(".xml") } ?: return

        for (file in files) {
            try {
                val element = JDOMUtil.load(file)
                if (element.name == "templateSet") {
                    val groupName = element.getAttributeValue("group") ?: "Project"
                    val finalGroupName = GROUP_PREFIX + groupName

                    element.getChildren("template").forEach { templateElement ->
                        val name = templateElement.getAttributeValue("name") ?: return@forEach
                        val value = templateElement.getAttributeValue("value") ?: ""

                        val template = TemplateImpl(name, value, finalGroupName)
                        template.description = templateElement.getAttributeValue("description")

                        templateElement.getAttributeValue("toReformat")?.toBoolean()?.let {
                            template.isToReformat = it
                        }

                        templateElement.getChild("context")?.getChildren("option")?.forEach { option ->
                            val contextId = option.getAttributeValue("name")
                            val isEnabled = option.getAttributeValue("value")?.toBoolean() ?: true
                            
                            if (contextId != null) {
                                val ep = ExtensionPointName.create<TemplateContextType>("com.intellij.codeInsight.template.contextType")
                                ep.extensionList.find { it.contextId == contextId }?.let { type ->
                                    template.templateContext.setEnabled(type, isEnabled)
                                }
                            }
                        }
                        templateSettings.addTemplate(template)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
