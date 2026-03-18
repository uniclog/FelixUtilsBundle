package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import io.github.uniclog.docker.runner.compose.VersionReader
import io.github.uniclog.docker.runner.compose.ComposeMetadata
import io.github.uniclog.docker.runner.docker.ComposeRunner
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.ui.dialog.AlertDialog
import io.github.uniclog.docker.runner.ui.dialog.ConfirmDialog
import io.github.uniclog.docker.runner.ui.generate.GenerateDialog
import javax.swing.*

class ComposeGeneratePanel(
    private val project: Project?,
    private val getAnkeyPath: () -> AnkeyPath,
    private val onGenerated: (Pair<String, String>) -> Unit
) {
    val panel = JPanel()
    private val infoLabel = JBLabel("Compose is generated!").apply {
        foreground = JBColor.GREEN
        isVisible = false
    }

    init {
        panel.layout = BoxLayout(panel, BoxLayout.X_AXIS)

        val button = JButton("Generate Compose File").apply {
            addActionListener {
                val ankeyPath = getAnkeyPath()

                val coreVersion = VersionReader.read(ankeyPath).getOrElse { error ->
                    AlertDialog.showErrorDialog("Docker Runner", error.message ?: "Unknown error")
                    return@addActionListener
                }

                /// choice configuration
                //val coreVersion = if (prepareVer.contains(ANKEY_VER_10)) ANKEY_VER_10 else ANKEY_VER_11
                val generateDialog = GenerateDialog(coreVersion = coreVersion)
                // show gen dialog
                if (!generateDialog.showAndGet())
                    return@addActionListener
                val services = generateDialog.getSelectedOptions()
                val prefix = generateDialog.getAnkeyPrefix()

                /// confirm
                val dialog = ConfirmDialog(
                    message = "Are you sure you want to generate a new compose file?<br/>The existing file will be overwritten.",
                    okActionText = "Generate"
                )
                if (!dialog.showAndGet())
                    return@addActionListener

                /// generate compose file
                ProgressManager.getInstance().run(
                    object : Task.Backgroundable(project, "Generate Docker Compose", true) {
                        override fun run(indicator: ProgressIndicator) {
                            indicator.isIndeterminate = true
                            indicator.text = "Generating compose file..."

                            if (DockerService.composeExists(ankeyPath)) {
                                val projectName = ComposeMetadata.getProjectName(ankeyPath.getComposePath())
                                if (ComposeRunner.hasRunningComposeContainers(projectName)) {
                                    SwingUtilities.invokeLater {
                                        Messages.showErrorDialog(
                                            "Docker containers for this compose file are running and must be stopped before generating a new compose file.",
                                            "Generate Docker Compose Error"
                                        )
                                    }
                                    return
                                }
                                //DockerService.downProcBackground(project, ankeyPath.getComposePath())
                            }

                            val generated = DockerService.generateCompose(ankeyPath, prefix, services)

                            if (generated == null) {
                                SwingUtilities.invokeLater {
                                    AlertDialog.showErrorDialog(
                                        "Docker Runner",
                                        "Unable to generate compose file. Check ports or docker project names."
                                    )
                                }
                                return
                            }

                            SwingUtilities.invokeLater {
                                showInfo()
                                onGenerated(Pair(prefix, ""))
                            }
                        }
                    }
                )
            }
        }

        panel.add(button)
        panel.add(Box.createHorizontalGlue())
        panel.add(infoLabel)
    }

    private fun showInfo() {
        infoLabel.isVisible = true
        val timer = Timer(2000) {
            infoLabel.isVisible = false
        }
        timer.isRepeats = false
        timer.start()
    }
}
