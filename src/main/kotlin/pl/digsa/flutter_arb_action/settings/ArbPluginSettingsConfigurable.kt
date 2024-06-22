package pl.digsa.flutter_arb_action.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import pl.digsa.flutter_arb_action.settings.ArbPluginSettingsState.Companion.getSettingsInstance

class ArbPluginSettingsConfigurable(private val project: Project) : Configurable {

    private val panel: ArbPluginSettingsPanel by lazy { ArbPluginSettingsPanel() }
    override fun createComponent() = panel.panel

    override fun isModified(): Boolean {
        val settings = project.getSettingsInstance()
        return panel.importStatementValue.importToSettings() != settings.state.importPath || panel.extensionNameValue != settings.state.extensionName
    }

    override fun apply() {
        panel.apply()
        project.getSettingsInstance().state.apply {
            importPath = panel.importStatementValue.importToSettings()
            extensionName = panel.extensionNameValue
        }
    }

    override fun reset() {
        project.getSettingsInstance().state.apply {
            println(importPath.importToUi())
            panel.importStatementValue = importPath.importToUi()
            panel.extensionNameValue = extensionName
        }
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String = "Flutter Arb Action"

    private fun String.importToUi(): String = substring(8, length - 2)

    private fun String.importToSettings(): String = "import \'$this\';"

}