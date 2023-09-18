package pl.digsa.flutter_arb_action.autohinting

import com.intellij.ui.JBColor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.KeyboardFocusManager
import java.awt.RenderingHints
import java.awt.event.KeyEvent
import java.lang.Integer.min
import javax.swing.JTextField
import javax.swing.KeyStroke


class AutohintTextField(private val autocompletionTree: AutocompletionTree) : JTextField() {
    private var hint: String? = null

    init {
        val emptySet: Set<KeyStroke> = HashSet()
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet)
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, emptySet)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val newHint = autocompletionTree.findMatching(text)
        hint = newHint

        if (hasFocus() && text.isNotEmpty() && newHint != null) {
            val g2 = g.create() as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.color = JBColor.GRAY
            g2.font = font
            val endPosition = getFontMetrics(font).stringWidth(text)
            g2.drawString(
                newHint.substring(min(newHint.length, text.length)),
                endPosition + 13,
                20
            ) // Adjust position as needed
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
        text = hint
    }
}