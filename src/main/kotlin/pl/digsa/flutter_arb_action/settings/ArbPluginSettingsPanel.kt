package pl.digsa.flutter_arb_action.settings

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JPanel
import javax.swing.JTextField

class ArbPluginSettingsPanel {

    private lateinit var importStatement: Cell<JTextField>
    private lateinit var extensionName: Cell<JTextField>

    val panel: JPanel by lazy {
        panel {
            row {
                importStatement = textField()
                    .horizontalAlign(HorizontalAlign.FILL)
                    .label("Path to the extension function", LabelPosition.TOP)
                    .focused()
                    .validation {
                        if (it.text.isEmpty()) {
                            return@validation ValidationInfo("Field cannot be empty")
                        }
                        if (it.text.trim().contains(" ")) {
                            return@validation ValidationInfo("Field cannot contain white spaces")
                        }
                        if (!it.text.matches(Regex("^package:[^/]+(/[^/]+)+.dart$"))) {
                            return@validation ValidationInfo("Incorrect pattern. Should be \"package:path/to/extension.dart\"")
                        }
                        null
                    }
            }
            row {
                extensionName = textField()
                    .label("Name of the extension parameter", LabelPosition.TOP)
                    .focused()
                    .validation {
                        if (it.text.isEmpty()) {
                            return@validation ValidationInfo("Field cannot be empty")
                        }
                        if (it.text.trim().contains(" ")) {
                            return@validation ValidationInfo("Field cannot contain white spaces")
                        }
                        null
                    }
            }
        }
    }

    var importStatementValue: String
        get() = importStatement.component.text
        set(value) {
            importStatement.component.text = value
        }

    var extensionNameValue: String
        get() = extensionName.component.text
        set(value) {
            extensionName.component.text = value
        }

}