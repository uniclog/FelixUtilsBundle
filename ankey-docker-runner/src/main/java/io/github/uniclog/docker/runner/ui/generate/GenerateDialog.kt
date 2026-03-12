package io.github.uniclog.docker.runner.ui.generate

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import io.github.uniclog.docker.runner.model.AnkeyComponent
import io.github.uniclog.docker.runner.model.AnkeyComponentState
import io.github.uniclog.docker.runner.settings.AnkeySettings
import io.github.uniclog.docker.runner.model.Constants.ANKEY_VER_11
import java.awt.Component
import javax.swing.Action
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class GenerateDialog(private val coreVersion: String) : DialogWrapper(true) {

    private val coreOpt = JBCheckBox("Ankey Core")
    private val pgsqlOpt = JBCheckBox("PostgreSQL")
    private val osrchOpt = JBCheckBox("OpenSearch")
    private val kafkaOpt = JBCheckBox("Kafka")
    private val kafkaUiOpt = JBCheckBox("Kafka UI")
    private val bpmnOpt = JBCheckBox("BPMN Service")
    private val demoOpt = JBCheckBox("Demo Data")

    private val prefixField = JBTextField(AnkeySettings.instance.getAnkeyPrefix())

    init {
        title = "Docker Runner Configuration"
        initOptions()
        init()
    }

    override fun getOKAction(): Action =
        super.getOKAction().apply {
            putValue(Action.NAME, "Generate")
        }

    private fun initOptions() {
        coreOpt.apply {
            isSelected = true
            //isEnabled = false
        }
        pgsqlOpt.apply {
            isSelected = true
            //isEnabled = false
        }
        osrchOpt.apply {
            isSelected = true
            //isEnabled = false
        }

        if (coreVersion == ANKEY_VER_11) {
            kafkaOpt.apply {
                isSelected = true
                //isEnabled = false
            }
        }

        /** TBD **/
        /*bpmnOpt.apply {
            isSelected = false
            isEnabled = false
            toolTipText = "TBD"
        }*/
        demoOpt.apply {
            isSelected = false
            isEnabled = false
            toolTipText = "TBD"
        }
    }

    override fun createCenterPanel(): JComponent {
        return FormBuilder.createFormBuilder()
            .addSeparator(12)
            .addComponent(header())
            .addSeparator(12)
            .addComponent(coreGroup())
            .addSeparator(10)
            .addComponent(optionalGroup())
            .addSeparator(10)
            .addComponent(
                group(
                    title = "Ankey prefix",
                    description = "Prefix added to the container name",
                    components = listOf(prefixField)
                )
            )
            .panel
    }

    private fun header(): JComponent =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT

            add(JBLabel("<html><b>Ankey Core $coreVersion</b><br /><br /><b>Select Ankey services</b></html>"))
            add(JBLabel("Choose which services will be included in docker-compose").apply {
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            })
        }

    private fun coreGroup(): JComponent =
        group(
            title = "Core services",
            description = "",
            components = listOf(coreOpt, pgsqlOpt, osrchOpt, kafkaOpt)
        )

    private fun optionalGroup(): JComponent =
        group(
            title = "Optional services",
            description = "Additional services for development and testing",
            components = listOf(kafkaUiOpt, bpmnOpt, demoOpt)
        )

    private fun group(
        title: String,
        description: String,
        components: List<JComponent>
    ): JComponent =
        JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = Component.LEFT_ALIGNMENT
            border = JBUI.Borders.empty(8, 0)

            add(JBLabel("<html><b>$title</b></html>"))
            add(JBLabel(description).apply {
                border = JBUI.Borders.empty(0, 0, 6, 0)
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
            })

            components.forEach {
                it.alignmentX = Component.LEFT_ALIGNMENT
                add(it)
            }
        }

    fun getSelectedOptions(): List<AnkeyComponentState> =
        listOf(
            AnkeyComponentState(AnkeyComponent.CORE, coreOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.POSTGRES, pgsqlOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.OPENSEARCH, osrchOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.KAFKA, kafkaOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.KAFKA_UI, kafkaUiOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.BPMN, bpmnOpt.isSelected),
            AnkeyComponentState(AnkeyComponent.NONE, demoOpt.isSelected),
        )
            .filter { it.isActivate }

    fun getAnkeyPrefix(): String = prefixField.text
}

/*
data class SelectedOptions(
    val coreOpt: Boolean,
    val pgsqlOpt: Boolean,
    val osrchOpt: Boolean,
    val kafkaOpt: Boolean,
    val kafkaUiOpt: Boolean,
    val bpmnOpt: Boolean,
    val demoOpt: Boolean
)*/
