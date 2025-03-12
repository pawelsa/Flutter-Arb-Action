package pl.digsa.flutter_arb_action.autohinting2

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBList
import pl.digsa.flutter_arb_action.autohinting.HintTextField
import java.awt.BorderLayout
import java.awt.KeyboardFocusManager
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

class TranslationKeyDialog(project: Project, existingKeys: Set<String>) : DialogWrapper(project) {

    private val textField = HintTextField(20)
    private val suggestionList = JBList<String>()
    private val trie = buildTrieFromKeys(existingKeys)
    private val model = DefaultListModel<String>()
    private var selectedIndex = -1

    init {
        title = "Enter Translation Key"
        init()
        setupAutocomplete()
    }

    private fun buildTrieFromKeys(keys: Set<String>): KeyTrie {
        val trie = KeyTrie()
        keys.forEach { trie.insert(it) }
        return trie
    }

    private fun suggestNextParts(input: String, trie: KeyTrie): List<String> {
        return trie.getNextSuggestions(input)
    }

    private fun setupAutocomplete() {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(p0: javax.swing.event.DocumentEvent) {
                updateSuggestions()
            }
        })

        textField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet())
//todo zrobić tak że jeśli jest hint, ale nie jest zaznaczony i ktoś kliknie tab to powinno go zaakceptować, albo automatycznie jak użytkownik wpisuje to zaznaczać pierwszy hint
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                println(KeyEvent.getKeyText(e.keyCode))
                when (e.keyCode) {
                    KeyEvent.VK_DOWN -> navigateSuggestions(1)
                    KeyEvent.VK_UP -> navigateSuggestions(-1)
                    KeyEvent.VK_ENTER, KeyEvent.VK_TAB -> {
                        e.consume()
                        acceptSuggestion()
                    }
                }
            }
        })

        suggestionList.addListSelectionListener {
            if (!suggestionList.isSelectionEmpty) {
                selectedIndex = suggestionList.selectedIndex
            }
        }
        suggestionList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_TAB) {
                    e.consume()
                    acceptSuggestion()
                }
            }
        })
    }

    private fun navigateSuggestions(direction: Int) {
        if (model.size == 0) return

        selectedIndex = (selectedIndex + direction).coerceIn(0, model.size - 1)
        suggestionList.selectedIndex = selectedIndex
        suggestionList.ensureIndexIsVisible(selectedIndex)
    }

    private fun acceptSuggestion() {
        if (selectedIndex in 0 until model.size) {
            val input = textField.text
            val nextPart = suggestionList.selectedValue
            textField.text = input + nextPart.removePrefix(input) // Append missing part
            selectedIndex = -1
            updateSuggestions()
        }
    }

    private fun updateSuggestions() {
        val input = textField.text
        model.clear()

        if (trie.keyExists(input)) {
            setErrorText("Key '$input' already exists!")
        } else {
            setErrorText(null)
        }

        val suggestNextParts = suggestNextParts(input, trie)
        suggestNextParts.firstOrNull()?.let {
            textField.hint = it
        }
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
