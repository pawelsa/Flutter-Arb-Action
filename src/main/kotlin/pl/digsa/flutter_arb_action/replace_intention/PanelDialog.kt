package pl.digsa.flutter_arb_action.replace_intention

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent

class PanelDialog(private val panel: JComponent, text: String) : DialogWrapper(true) {
    init {
        title = text
        init()
    }

    override fun createCenterPanel() = panel

    fun setOkText(text: String) = setOKButtonText(text)
}