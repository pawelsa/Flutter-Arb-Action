package pl.digsa.flutter_arb_action.settings

import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import javax.swing.JTextField

class ArbPluginSettingsPanel {

    private lateinit var importStatement: JTextField
    private lateinit var extensionName: JTextField

    val panel: DialogPanel by lazy {
        panel {
            row {
                importStatement = JTextField()
                cell(importStatement).align(AlignX.FILL)
                    .label("Path to the extension function", LabelPosition.TOP)
                    .focused()
            }
            row {
                extensionName = JTextField()
                cell(extensionName).label("Name of the extension parameter", LabelPosition.TOP)

            }
        }
    }

    fun apply() {
        when {
            importStatementValue.isEmpty() -> throw ConfigurationException("Import statement cannot be empty")
            importStatementValue.trim()
                .contains(" ") -> throw ConfigurationException("Import statement cannot contain white spaces")

            !importStatementValue.matches(Regex("^package:[^/]+(/[^/]+)+.dart$")) -> throw ConfigurationException("Incorrect pattern of import statement. Should be \"package:path/to/extension.dart\"")
        }
        when {
            extensionNameValue.isEmpty() -> throw ConfigurationException("Extension name cannot be empty")
            extensionNameValue.trim()
                .contains(" ") -> throw ConfigurationException("Extension name cannot contain white spaces")
        }
    }

    var importStatementValue: String
        get() = importStatement.text
        set(value) {
            importStatement.text = value
        }

    var extensionNameValue: String
        get() = extensionName.text
        set(value) {
            extensionName.text = value
        }

}