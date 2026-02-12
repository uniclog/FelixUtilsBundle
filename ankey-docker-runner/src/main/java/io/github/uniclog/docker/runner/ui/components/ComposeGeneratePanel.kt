package io.github.uniclog.docker.runner.ui.components

import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import io.github.uniclog.docker.runner.docker.ComposeFileGenerator
import io.github.uniclog.docker.runner.docker.DockerComposeRunner.getComposeProjectName
import io.github.uniclog.docker.runner.docker.DockerComposeRunner.hasRunningComposeContainers
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_10
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_11
import io.github.uniclog.docker.runner.ui.dialog.ConfirmDialog
import io.github.uniclog.docker.runner.ui.generate.GenerateDialog
import javax.swing.*

class ComposeGeneratePanel(
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

                val coreVersion = ComposeFileGenerator.getVersion(ankeyPath)
                    ?: return@addActionListener

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

                /// down docker if exists
                if (DockerService.composeExists(ankeyPath)) {
                    val projectName = getComposeProjectName(ankeyPath.getComposePath())
                    if (hasRunningComposeContainers(projectName)) {
                        Messages.showErrorDialog(
                            "Docker containers for this compose file are running and must be stopped before generating a new compose file.",
                            "Generate Docker Compose Error"
                        )
                        return@addActionListener
                    }
                    //DockerService.downProcBackground(project, ankeyPath.getComposePath())
                }

                fun generateSeed(length: Int = 4): String {
                    val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
                    return (1..length)
                        .map { chars.random() }
                        .joinToString("")
                }
                val seed = generateSeed(4)

                /// generate compose file
                DockerService.generateCompose(ankeyPath, prefix + '_' + seed, services)
                    ?: return@addActionListener

                showGenerated()
                onGenerated(Pair(prefix, seed))
            }
        }

        panel.add(button)
        panel.add(Box.createHorizontalGlue())
        panel.add(infoLabel)
    }

    private fun showGenerated() {
        infoLabel.isVisible = true
        val timer = Timer(2000) {
            infoLabel.isVisible = false
        }
        timer.isRepeats = false
        timer.start()
    }
}