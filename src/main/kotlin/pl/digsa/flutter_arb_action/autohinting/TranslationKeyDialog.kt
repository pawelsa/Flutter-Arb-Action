package pl.digsa.flutter_arb_action.autohinting

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBList
import pl.digsa.flutter_arb_action.utils.getRestOfHintToShow
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class TranslationKeyDialog(project: Project, keyTrie: KeyTrie) : DialogWrapper(project) {

    private val textField = HintTextField(20)
    private val suggestionList = JBList<String>()
    private val trie = keyTrie
    private val model = DefaultListModel<String>()
    private var selectedIndex = -1

    init {
        title = "Enter Translation Key"
        init()
        setupAutocomplete()
    }

    private fun setupAutocomplete() {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(p0: javax.swing.event.DocumentEvent) {
                updateSuggestions()
            }
        })

        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet())
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_DOWN -> {
                        navigateSuggestions(1)
                        e.consume()
                    }

                    KeyEvent.VK_UP -> {
                        navigateSuggestions(-1)
                        e.consume()
                    }
                    KeyEvent.VK_TAB -> {
                        acceptSuggestion()
                        e.consume()
                    }

                    KeyEvent.VK_ENTER -> doOKAction()
                }
            }
        })

        suggestionList.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet())
        suggestionList.addListSelectionListener {
            if (!suggestionList.isSelectionEmpty) {
                selectedIndex = suggestionList.selectedIndex
//                textField.hint = suggestionList.selectedValue
            }
        }
        suggestionList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_TAB) {
                    acceptSuggestion()
                    e.consume()
                }
            }
        })
    }

    private fun navigateSuggestions(direction: Int) {
        if (model.size == 0) return

        selectedIndex = (selectedIndex + direction).coerceIn(0, model.size - 1)
        suggestionList.selectedIndex = selectedIndex
        suggestionList.ensureIndexIsVisible(selectedIndex)
        textField.hint = suggestionList.selectedValue
    }

    private fun acceptSuggestion() {
        val input = textField.text
        val hint = when {
            selectedIndex in 0 until model.size -> suggestionList.selectedValue
            selectedIndex == -1 && !suggestionList.isEmpty -> suggestionList.getModel().getElementAt(0)
            else -> return
        }

        val toAppend = getRestOfHintToShow(input, hint)
        textField.text = input + toAppend
        selectedIndex = -1
        updateSuggestions()
    }

    private fun updateSuggestions() {
        val input = textField.text
        model.clear()

        when {
            trie.keyExists(input) -> setErrorText("Key '$input' already exists!")
            else -> setErrorText(null)
        }

        val suggestNextParts = trie.getNextSuggestions(input)
        textField.hint = suggestNextParts.firstOrNull()
        suggestNextParts.forEach { model.addElement(it) }
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(textField, BorderLayout.NORTH)
        suggestionList.model = model
        panel.add(JScrollPane(suggestionList), BorderLayout.CENTER)
        textField.requestFocusInWindow()
        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = textField

    fun getResult(): String? = if (isOK) textField.text else null
}
