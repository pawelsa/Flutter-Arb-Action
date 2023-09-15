package pl.digsa.flutter_arb_action.autohinting

import com.intellij.ui.JBColor
import java.awt.*
import java.awt.event.KeyEvent
import javax.swing.JTextField
import javax.swing.KeyStroke


class AutohintTextField(private var hints: List<String>) : JTextField() {

    init {
        val emptySet: Set<KeyStroke> = HashSet()
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet)
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptySet)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val hint = hints.firstOrNull { it.startsWith(text) }

        if (hasFocus() && text.isNotEmpty() && hint != null) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.color = JBColor.GRAY
            g2.font = font
            val endPosition = getFontMetrics(font).stringWidth(text)
            g2.drawString(hint.substring(text.length), endPosition + 13, 20) // Adjust position as needed
            g2.dispose()
        }
    }

    override fun processKeyEvent(e: KeyEvent?) {
        if (e?.keyCode == KeyEvent.VK_TAB) {
            triggerAutocompletion()
            e.consume()
        } else {
            super.processKeyEvent(e)
        }
    }

    private fun triggerAutocompletion() {
        text = hints.first()
    }
}