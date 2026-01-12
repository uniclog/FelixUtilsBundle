package io.github.uniclog.docker.runner.ui.components

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import io.github.uniclog.docker.runner.docker.ComposeFileGenerator
import io.github.uniclog.docker.runner.model.AnkeyPath
import io.github.uniclog.docker.runner.service.DockerService
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_10
import io.github.uniclog.docker.runner.settings.Constants.ANKEY_VER_11
import io.github.uniclog.docker.runner.ui.dialog.ConfirmDialog
import io.github.uniclog.docker.runner.ui.generate.GenerateDialog
import javax.swing.*

class ComposeGeneratePanel(
    private val getPath: () -> AnkeyPath,
    //private val prefix: () -> String,
    private val onGenerated: (String) -> Unit
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
                val prepareVer = ComposeFileGenerator.getComposeTemplateName(getPath())
                    ?: return@addActionListener

                /// choice configuration
                val coreVersion = if (prepareVer.contains(ANKEY_VER_10)) ANKEY_VER_10 else ANKEY_VER_11
                val generateDialog = GenerateDialog(coreVersion = coreVersion)
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

                /// добавить выборку сервисов
                DockerService.generateCompose(getPath(), prefix, services)
                    ?: return@addActionListener

                showGenerated()
                onGenerated(prefix)
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
        timer.start()
    }
}