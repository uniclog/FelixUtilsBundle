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
        println("ProjectSnippetsActivity: Starting for project ${project.name}")
        reloadTemplates(project)
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val relevantEvents = events.filter { it.path.contains(IDEA_LIVE_TEMPLATES) }
                if (relevantEvents.isNotEmpty()) {
                    println("ProjectSnippetsActivity: VFS change detected in ${relevantEvents.map { it.path }}")
                    reloadTemplates(project)
                }
            }
        })
    }

    private fun reloadTemplates(project: Project) {
        val projectPath = project.basePath ?: return
        val templatesDir = File(projectPath, IDEA_LIVE_TEMPLATES)
        
        println("ProjectSnippetsActivity: Reloading templates from ${templatesDir.absolutePath}")

        if (!templatesDir.exists() || !templatesDir.isDirectory) {
            println("ProjectSnippetsActivity: Directory not found: ${templatesDir.absolutePath}")
            return
        }

        val templateSettings = TemplateSettings.getInstance()

        val removedCount = templateSettings.templates
            .filter { it.groupName.startsWith(GROUP_PREFIX) }
            .onEach { templateSettings.removeTemplate(it) }
            .size
        
        if (removedCount > 0) {
            println("ProjectSnippetsActivity: Removed $removedCount existing project templates")
        }

        val files = templatesDir.listFiles { _, name -> name.endsWith(".xml") } ?: return
        println("ProjectSnippetsActivity: Found ${files.size} XML files")

        for (file in files) {
            try {
                println("ProjectSnippetsActivity: Processing file: ${file.name}")
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
                                val contextType = ep.extensionList.find { it.contextId.equals(contextId, ignoreCase = true) }
                                if (contextType != null) {
                                    template.templateContext.setEnabled(contextType, isEnabled)
                                    println("ProjectSnippetsActivity: Set context '$contextId' for template '$name'")
                                } else {
                                    println("ProjectSnippetsActivity: WARNING - Context '$contextId' NOT FOUND for template '$name'")
                                }
                            }
                        }
                        templateSettings.addTemplate(template)
                        println("ProjectSnippetsActivity: Added template '$name' to group '$finalGroupName'")
                    }
                }
            } catch (e: Exception) {
                println("ProjectSnippetsActivity: Error loading file ${file.name}: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
