package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import io.github.uniclog.docker.runner.compose.ComposeFileWriter
import io.github.uniclog.docker.runner.compose.ComposeMetadata
import io.github.uniclog.docker.runner.docker.ComposeRunner
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.ui.dialog.ConfirmDialog
import io.github.uniclog.docker.runner.ui.dialog.PortsDialog
import java.io.File
import javax.swing.*

class ComposeStatusPanel(
    private val project: Project?,
    private val getAnkeyPath: () -> AnkeyPath,
    private val upAction: Action
) {
    val panel = JPanel()
    private val statusLabel = JBLabel()

    private val showButton = JButton("Ports").apply { isEnabled = false }
    private val openButton = JButton("Open").apply { isEnabled = false }
    private val deleteButton = JButton("Delete").apply { isEnabled = false }

    init {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)
        panel.add(showButton)
        panel.add(openButton)
        panel.add(deleteButton)
        panel.add(Box.createHorizontalGlue())
        panel.add(statusLabel)

        showButton.addActionListener {
            /// @todo проверить на пустой comboBox
            val path = getAnkeyPath().getComposePath()
            val portsByService = readComposePorts(path)
            if (portsByService.isEmpty()) {
                Messages.showInfoMessage(
                    "No ports found in compose file.",
                    "Compose Ports"
                )
                return@addActionListener
            }

            val text = buildString {
                portsByService.forEach { (service, ports) ->
                    append(service).append(":\n")
                    ports.forEach { port ->
                        append("  ").append(port).append("\n")
                    }
                    append("\n")
                }
            }
            PortsDialog(text).show()
        }
        openButton.addActionListener {
            /// @todo проверить на пустой comboBox
            val path = getAnkeyPath().getComposePath()

            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path)
                ?: return@addActionListener

            val project = ProjectManager.getInstance().openProjects.firstOrNull()
                ?: return@addActionListener

            FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
        deleteButton.addActionListener {
            val ankeyPath = getAnkeyPath()
            val dialog = ConfirmDialog(
                message = "Are you sure you want to delete this file?<br/>This action cannot be undone.",
                okActionText = "Delete"
            )

            if (!dialog.showAndGet())
                return@addActionListener

            if (DockerService.composeExists(ankeyPath)) {
                val projectName = ComposeMetadata.getProjectName(ankeyPath.getComposePath())
                if (ComposeRunner.hasRunningComposeContainers(projectName)) {
                    Messages.showErrorDialog(
                        "Docker containers for this compose file are running and must be stopped before generating a new compose file.",
                        "Generate Docker Compose Error"
                    )
                    return@addActionListener
                }
                //DockerService.downProcBackground(project, ankeyPath.getComposePath())
            }
            deleteButton.isEnabled = false
            ProgressManager.getInstance().run(
                object : Task.Backgroundable(project, "Delete Docker Compose Files", true) {
                    override fun run(indicator: ProgressIndicator) {
                        indicator.isIndeterminate = true
                        indicator.text = "Deleting generated files..."
                        ComposeFileWriter.deleteGeneratedFiles(ankeyPath.getComposeBasePath())
                    }

                    override fun onThrowable(error: Throwable) {
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(
                                project,
                                error.message ?: "Failed to delete generated files.",
                                "Delete Docker Compose Error"
                            )
                        }
                    }

                    override fun onFinished() {
                        ApplicationManager.getApplication().invokeLater {
                            update()
                        }
                    }
                }
            )
            //AnkeyPathService.deleteFile(getAnkeyPath())
        }
    }

    fun update() {
        val exists = DockerService.composeExists(path = getAnkeyPath())
        upAction.isEnabled = exists

        if (exists) {
            statusLabel.text = "Compose file exists!"
            statusLabel.foreground = JBColor.GREEN
        } else {
            statusLabel.text = "Compose file not found!"
            statusLabel.foreground = JBColor.RED
        }

        showButton.isEnabled = exists
        openButton.isEnabled = exists
        deleteButton.isEnabled = exists
    }

    private fun readComposePorts(path: String): Map<String, List<String>> {
        val file = File(path)
        if (!file.exists()) return emptyMap()

        val result = linkedMapOf<String, MutableList<String>>()
        var currentService: String? = null
        var inPorts = false

        file.useLines { lines ->
            lines.forEach { raw ->
                val line = raw.replace("\t", "    ")
                val trimmed = line.trim()

                if (line.startsWith("  ") && !line.startsWith("    ") && trimmed.endsWith(":")) {
                    val key = trimmed.removeSuffix(":")
                    currentService = if (key in setOf("services", "networks", "volumes")) null else key
                    inPorts = false
                    return@forEach
                }

                if (currentService != null && line.startsWith("    ports:")) {
                    inPorts = true
                    return@forEach
                }

                if (inPorts && currentService != null && trimmed.startsWith("-")) {
                    val spec = trimmed.removePrefix("-").trim().trim('"', '\'')
                    val parts = spec.split(":")
                    val normalized = if (parts.size >= 2) {
                        val hostRaw = parts[parts.size - 2].trim()
                        val contRaw = parts[parts.size - 1].trim()
                        val host = hostRaw.substringBefore("/").trim()
                        val cont = contRaw.substringBefore("/").trim()
                        if (host.matches(Regex("\\d+")) && cont.matches(Regex("\\d+"))) {
                            formatPort(currentService!!, host, cont)
                        } else {
                            spec
                        }
                    } else {
                        spec
                    }
                    result.getOrPut(currentService!!) { mutableListOf() }.add(normalized)
                    return@forEach
                }

                if (inPorts && line.startsWith("    ") && !line.startsWith("      ") && trimmed.endsWith(":") && trimmed != "ports:") {
                    inPorts = false
                }
            }
        }

        return result
    }

    private fun formatPort(service: String, host: String, container: String): String {
        val role = when (container) {
            "8080" -> if (service.contains("kafka-ui")) "KAFKA_UI_HTTP" else "HTTP"
            "8081" -> "BPMN_HTTP"
            "5005" -> if (service.contains("bpmn")) "BPMN_DEBUG" else "DEBUG"
            "9010" -> "JMX"
            "5432" -> "POSTGRES"
            "9092" -> "KAFKA"
            "9200" -> "OPENSEARCH_HTTP"
            "9300" -> "OPENSEARCH_TRANSPORT"
            else -> null
        }

        return if (role == null) {
            "$host -> $container"
        } else {
            "$role $host -> $container"
        }
    }
}
