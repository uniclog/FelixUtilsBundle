package io.github.uniclog.felixutils.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import io.github.uniclog.felixutils.model.EndpointConnection

@Service(Service.Level.APP)
@State(name = "io.github.uniclog.felixutils.uFelixUtilsSettings", storages = [Storage("uFelixUtilsSettings.xml")])
class FelixSettings : PersistentStateComponent<FelixSettings.State> {

    data class State(
        var url: String = "http://localhost:8080/ankey/config",
        var username: String = "ankey",
        var password: String = "ankey",

        var felixUrl: String = "http://localhost:8080/system/console/bundles",
        var felixUsername: String = "admin",
        var felixPassword: String = "admin"
    ) {
        fun ankeyConnection(): EndpointConnection {
            return EndpointConnection(url, username, password)
        }

        fun felixConnection(): EndpointConnection {
            return EndpointConnection(felixUrl, felixUsername, felixPassword)
        }
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): FelixSettings {
            return ApplicationManager.getApplication().getService(FelixSettings::class.java)
        }
    }
}
