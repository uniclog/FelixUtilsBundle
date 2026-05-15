package io.github.uniclog.docs

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.TemplateContextTypes
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

const val GROUP_PREFIX = "Project: "
private const val IDEA_LIVE_TEMPLATES = ".idea/liveTemplates"

class ProjectSnippetsActivity : ProjectActivity {

    override suspend fun execute(project: Project) {
        println("ProjectSnippetsActivity: Starting for project ${project.name}")
        reloadTemplates(project)
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val relevantEvents = events.filter { 
                    it.path.contains(IDEA_LIVE_TEMPLATES) && 
                    (it.path.endsWith(".xml") || it.path.endsWith(".yaml") || it.path.endsWith(".yml"))
                }
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

        val files = templatesDir.listFiles { _, name -> 
            name.endsWith(".xml") || name.endsWith(".yaml") || name.endsWith(".yml") 
        } ?: return
        println("ProjectSnippetsActivity: Found ${files.size} template files")

        for (file in files) {
            try {
                println("ProjectSnippetsActivity: Processing file: ${file.name}")
                if (file.name.endsWith(".xml")) {
                    loadXmlTemplates(file, templateSettings)
                } else {
                    loadYamlTemplates(file, templateSettings)
                }
            } catch (e: Exception) {
                println("ProjectSnippetsActivity: Error loading file ${file.name}: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadXmlTemplates(file: File, templateSettings: TemplateSettings) {
        val element = JDOMUtil.load(file)
        if (element.name == "templateSet") {
            val groupName = element.getAttributeValue("group") ?: "Project"
            val finalGroupName = GROUP_PREFIX + groupName

            element.getChildren("template").forEach { templateElement ->
                val name = templateElement.getAttributeValue("name") ?: return@forEach
                val value = templateElement.getAttributeValue("value") ?: ""
                val template = TemplateImpl(name, value, finalGroupName)
                
                template.description = templateElement.getAttributeValue("description")
                templateElement.getAttributeValue("toReformat")?.toBoolean()?.let { template.isToReformat = it }
                templateElement.getAttributeValue("toShortenFQNames")?.toBoolean()?.let { template.isToShortenLongNames = it }

                templateElement.getChild("context")?.getChildren("option")?.forEach { option ->
                    val contextId = option.getAttributeValue("name")
                    val isEnabled = option.getAttributeValue("value")?.toBoolean() ?: true
                    setContext(template, contextId, isEnabled)
                }
                templateSettings.addTemplate(template)
                println("ProjectSnippetsActivity: Added XML template '$name' to group '$finalGroupName'")
            }
        }
    }

    private fun loadYamlTemplates(file: File, templateSettings: TemplateSettings) {
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(FileInputStream(file)) ?: return
        
        val groupName = data["group"] as? String ?: "Project"
        val finalGroupName = GROUP_PREFIX + groupName
        val templates = data["templates"] as? List<Map<String, Any>> ?: return

        for (tData in templates) {
            val name = tData["name"] as? String ?: continue
            val value = tData["value"] as? String ?: ""
            val template = TemplateImpl(name, value, finalGroupName)

            template.description = tData["description"] as? String
            template.isToReformat = tData["reformat"] as? Boolean ?: false
            template.isToShortenLongNames = tData["shortenFQNames"] as? Boolean ?: true

            val contexts = tData["context"] as? Map<String, Boolean>
            contexts?.forEach { (contextId, isEnabled) ->
                setContext(template, contextId, isEnabled)
            }
            
            templateSettings.addTemplate(template)
            println("ProjectSnippetsActivity: Added YAML template '$name' to group '$finalGroupName'")
        }
    }

    private fun setContext(template: TemplateImpl, contextId: String?, isEnabled: Boolean) {
        if (contextId == null) return
        val contextType = TemplateContextTypes.getAllContextTypes().find { it.contextId.equals(contextId, ignoreCase = true) }
        if (contextType != null) {
            template.templateContext.setEnabled(contextType, isEnabled)
            // println("ProjectSnippetsActivity: Set context '$contextId' for template '${template.key}'")
        } else {
            println("ProjectSnippetsActivity: WARNING - Context '$contextId' NOT FOUND for template '${template.key}'")
        }
    }
}
