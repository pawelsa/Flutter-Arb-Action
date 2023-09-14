package pl.digsa.flutter_arb_action.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.project.Project

@State(
    name = "pl.digsa.flutter_arb_action",
)
@Service(Service.Level.PROJECT)
class ArbPluginSettingsState(private val project: Project) : PersistentStateComponent<ArbPluginSettingsState.State> {

    companion object {
        fun Project.getSettingsInstance(): ArbPluginSettingsState =
            getService(ArbPluginSettingsState::class.java)
    }

    class State {
        var importPath: String = "import \'package:\';"

        var extensionName: String = "text"
    }

    private var state: State = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }
}
