package pl.digsa.flutter_arb_action.autohinting

import java.awt.Graphics
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTextField


class MultipleHintTextField(private val hints: List<String>) : JTextField() {
    private val hintPopup: JPopupMenu = JPopupMenu()

    init {
        setupListeners()
    }

    private fun setupListeners() {
        addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode != KeyEvent.VK_ESCAPE) {
                    showHints()
                }
            }
        })
    }

    override fun processKeyEvent(e: KeyEvent?) {
        println(e?.keyCode)
        when (e?.keyCode) {
            KeyEvent.VK_TAB, KeyEvent.VK_ENTER -> {
                for (element in hintPopup.subElements) {
                    if (element is JMenuItem && element.isSelected) {
                        println("${element.isSelected} - ${element.text}")
                        text = element.text
                    }
                }
                e.consume()
            }

            KeyEvent.VK_UP, KeyEvent.VK_DOWN -> {
                hintPopup.let {
                    if (it.componentCount == 0) return
                    val index = it.selectionModel.selectedIndex.let { idx ->
                        when {
                            idx == -1 && e.keyCode == KeyEvent.VK_UP -> it.componentCount
                            idx == -1 && e.keyCode == KeyEvent.VK_DOWN -> 0
                            else -> idx
                        }
                    }
                    if (e.keyCode == KeyEvent.VK_DOWN) {
                        it.selectionModel.selectedIndex = (index + 1) % it.componentCount
                    } else if (e.keyCode == KeyEvent.VK_UP) {
                        it.selectionModel.selectedIndex = (index - 1 + it.componentCount) % it.componentCount
                    }
                }
            }

            KeyEvent.VK_ESCAPE -> {
                if (!hintPopup.isVisible) return super.processKeyEvent(e)
                hintPopup.removeAll()
                hintPopup.isVisible = false
            }

            else -> super.processKeyEvent(e)
        }
    }

    private fun showHints() {
        val userInput = text
        hintPopup.removeAll()

        if (userInput.isEmpty()) {
            hintPopup.isVisible = false
            return
        }

        hints
            .filter { it.contains(userInput) }
            .forEachIndexed { index, hint ->
                val menuItem = JMenuItem(hint)
                if (index == 0) {
                    menuItem.isSelected = true
                }
//                menuItem.addActionListener { _ -> text = hint }
                hintPopup.add(menuItem)
            }

        when {
            hintPopup.componentCount > 0 -> hintPopup.show(this, 0, height)
            else -> hintPopup.isVisible = false
        }
        this.requestFocus()
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        // Implement hint painting as needed, if desired
    }
}