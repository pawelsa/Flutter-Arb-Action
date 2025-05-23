package pl.digsa.flutter_arb_action.autohinting

import com.intellij.ui.JBColor
import pl.digsa.flutter_arb_action.utils.getRestOfHintToShow
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField


class HintTextField(columns: Int) : JTextField(columns) {
    var hint: String? = null
        set(value) {
            if (value != field) {
                field = value
                repaint()
            }
        }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)

        if (!hasFocus()) return
        val hint = this.hint?.takeIf { it.isNotEmpty() } ?: return
        val toPrint = getRestOfHintToShow(text, hint).takeIf { it.isNotEmpty() } ?: return

        val graphics2D = (graphics.create() as Graphics2D).apply {
            setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            color = JBColor.GRAY
            font = font
        }
        val endPosition = getFontMetrics(font).stringWidth(text)

        graphics2D.drawString(
            toPrint,
            endPosition + 13,
            21
        )
        graphics2D.dispose()
    }
}
