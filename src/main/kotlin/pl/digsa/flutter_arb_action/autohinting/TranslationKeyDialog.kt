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
import javax.swing.event.DocumentEvent

class TranslationKeyDialog(project: Project, private val trie: KeyTrie) : DialogWrapper(project) {

    private val textField = HintTextField()
    private val model = DefaultListModel<String>()
    private val suggestionList = JBList<String>(model)

    init {
        title = "Enter Translation Key"
        init()
        setupAutocomplete()
    }

    private fun setupAutocomplete() {
        addTextFieldListener()
        addTextFieldKeyListener()

        addSuggestionListSelectionListener()
        addSuggestionListKeyListener()
    }

    private fun addTextFieldListener() {
        textField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(p0: DocumentEvent) = updateSuggestions()
        })
    }

    private fun addTextFieldKeyListener() {
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

                    KeyEvent.VK_ENTER -> {
                        doOKAction()
                        e.consume()
                    }
                }
            }
        })
    }

    private fun navigateSuggestions(direction: Int) {
        if (model.isEmpty) return

        val newIndex = (suggestionList.selectedIndex + direction).coerceIn(0, model.size - 1)
        suggestionList.selectedIndex = newIndex
        suggestionList.ensureIndexIsVisible(newIndex)
        updateHint()
    }

    private fun addSuggestionListSelectionListener() {
        suggestionList.addListSelectionListener {
            updateHint()
        }
    }

    private fun addSuggestionListKeyListener() {
        suggestionList.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, emptySet())
        suggestionList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER || e.keyCode == KeyEvent.VK_TAB) {
                    acceptSuggestion()
                    e.consume()
                }
            }
        })
    }

    private fun updateHint() {
        textField.hint = suggestionList.selectedValue
            ?: model.takeIf { !suggestionList.isEmpty }?.getElementAt(0)
    }

    private fun acceptSuggestion() {
        val input = textField.text
        val hint = suggestionList.selectedValue
            ?: model.takeIf { !suggestionList.isEmpty }?.getElementAt(0)
            ?: return

        val toAppend = getRestOfHintToShow(input, hint)
        textField.text = input + toAppend
        suggestionList.clearSelection()
        updateSuggestions()
    }

    private fun updateSuggestions() {
        val input = textField.text
        model.clear()

        if (input.isEmpty()) {
            textField.hint = null
            setErrorText(null)
            return
        }

        setErrorText(if (trie.keyExists(input)) "Key '$input' already exists!" else null)

        val suggestNextParts = trie.getNextSuggestions(input)
        suggestNextParts.forEach { model.addElement(it) }
        suggestionList.clearSelection()
        updateHint()
    }

    override fun createCenterPanel(): JComponent = JPanel(BorderLayout()).apply {
        add(textField, BorderLayout.NORTH)
        add(JScrollPane(suggestionList), BorderLayout.CENTER)
    }

    override fun getPreferredFocusedComponent(): JComponent = textField

    fun getResult(): String? = if (isOK) textField.text else null
}
