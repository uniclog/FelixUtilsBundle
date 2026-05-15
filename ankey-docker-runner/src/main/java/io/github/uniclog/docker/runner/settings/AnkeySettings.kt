package io.github.uniclog.docker.runner.settings

import com.intellij.openapi.components.*
import io.github.uniclog.docker.runner.model.AnkeyPath

@State(
    name = "io.github.uniclog.idea.plugin.docker.runner",
    storages = [Storage("ankey-settings.xml")]
)
@Service
class AnkeySettings : PersistentStateComponent<AnkeySettings.State> {

    class State(
        var selectedPath: String = "",
        var selectedPathRoot: String = "",
        var ankeyPrefix: String = "",
        var isMapAnkeyVolume: Boolean = false,
    )

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getSelectedPath(): AnkeyPath = AnkeyPath(state.selectedPath, state.selectedPathRoot)
    fun setSelectedPath(path: AnkeyPath) {
        state.selectedPath = path.absolutePath
        state.selectedPathRoot = path.root
    }

    fun getAnkeyPrefix(): String = state.ankeyPrefix
    fun setAnkeyPrefix(prefix: String) {
        state.ankeyPrefix = prefix
    }

    fun isMapAnkeyVolume(): Boolean = state.isMapAnkeyVolume
    fun setMapAnkeyVolume(isMap: Boolean) {
        state.isMapAnkeyVolume = isMap
    }

    companion object {
        val instance: AnkeySettings
            get() = service()
    }
}