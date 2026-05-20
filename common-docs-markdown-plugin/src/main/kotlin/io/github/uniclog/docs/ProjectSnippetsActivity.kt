package io.github.uniclog.docs

import com.intellij.codeInsight.template.TemplateContextType
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream

const val GROUP_PREFIX = "Custom: "
private const val IDEA_LIVE_TEMPLATES = ".idea/liveTemplates"

class ProjectSnippetsActivity : StartupActivity {

    override fun runActivity(project: Project) {
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
                templateElement.getAttributeValue("id")?.let { template.id = it }
                templateElement.getAttributeValue("toReformat")?.toBoolean()?.let { template.isToReformat = it }
                val shortenNames = templateElement.getAttributeValue("toShortenLongNames")
                    ?: templateElement.getAttributeValue("toShortenFQNames")
                shortenNames?.toBoolean()?.let { template.isToShortenLongNames = it }

                templateElement.getChildren("variable").forEach { varElement ->
                    val varName = varElement.getAttributeValue("name") ?: return@forEach
                    val exp = varElement.getAttributeValue("expression") ?: ""
                    val defaultValue = varElement.getAttributeValue("defaultValue") ?: ""
                    val alwaysStopAt = varElement.getAttributeValue("alwaysStopAt")?.toBoolean() ?: true
                    template.addVariable(varName, exp, defaultValue, alwaysStopAt)
                }

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
            template.id = tData["id"] as? String
            template.isToReformat = tData["reformat"] as? Boolean ?: false
            template.isToShortenLongNames = (tData["toShortenLongNames"] ?: tData["toShortenFQNames"]) as? Boolean ?: true

            val vars = (tData["vars"] ?: tData["variables"]) as? Map<String, Any>
            vars?.forEach { (varName, varConfig) ->
                var exp = ""
                var default = ""
                var skip = false

                if (varConfig is Map<*, *>) {
                    exp = (varConfig["exp"] ?: varConfig["expression"] ?: "").toString()
                    default = (varConfig["default"] ?: varConfig["defaultValue"] ?: varConfig["prompt"] ?: "").toString()
                    skip = (varConfig["skip"] ?: varConfig["skipIfDefined"]) as? Boolean ?: false
                } else if (varConfig is String) {
                    exp = varConfig
                }

                template.addVariable(varName, exp, default, !skip)
            }

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
        try {
            val area = ApplicationManager.getApplication().extensionArea
            val ep = area.getExtensionPoint<Any>("com.intellij.liveTemplateContext")
            for (item in ep.extensions) {
                try {
                    val itemClass = item.javaClass
                    val getContextIdMethod = try { itemClass.getMethod("getContextId") } catch (e: Exception) { null }
                    val itemId = getContextIdMethod?.invoke(item) as? String

                    if (itemId.equals(contextId, ignoreCase = true)) {
                        val contextType = if (item is TemplateContextType) {
                            item
                        } else {
                            val getInstanceMethod = try { itemClass.getMethod("getInstance") } catch (e: Exception) { null }
                            getInstanceMethod?.invoke(item) as? TemplateContextType
                        }

                        if (contextType != null) {
                            template.templateContext.setEnabled(contextType, isEnabled)
                            return
                        }
                    }
                } catch (e: Exception) {
                    // skip
                }
            }
        } catch (e: Throwable) {
            println("ProjectSnippetsActivity: Error in setContext: ${e.message}")
        }
    }
}
