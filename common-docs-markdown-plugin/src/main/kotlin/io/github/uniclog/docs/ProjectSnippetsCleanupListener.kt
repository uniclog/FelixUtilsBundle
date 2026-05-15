package io.github.uniclog.docs

import com.intellij.codeInsight.template.impl.TemplateSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

/**
 * Kotlin-версия слушателя для очистки шаблонов.
 */
class ProjectSnippetsCleanupListener : ProjectManagerListener {
    override fun projectClosing(project: Project) {
        val templateSettings = TemplateSettings.getInstance()
        templateSettings.templates
            .filter { it.groupName.startsWith(GROUP_PREFIX) }
            .forEach { templateSettings.removeTemplate(it) }
    }
}
