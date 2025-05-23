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

    private val textField = HintTextField(20)
    private val suggestionList = JBList<String>()
    private val model = DefaultListModel<String>()
    private var selectedIndex = -1

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
        if (model.size == 0) return

        selectedIndex = (selectedIndex + direction).coerceIn(0, model.size - 1)
        suggestionList.selectedIndex = selectedIndex
        suggestionList.ensureIndexIsVisible(selectedIndex)
        updateHint()
    }

    private fun addSuggestionListSelectionListener() {
        suggestionList.addListSelectionListener {
            selectedIndex = when {
                !suggestionList.isSelectionEmpty -> suggestionList.selectedIndex
                else -> -1
            }
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
        textField.hint = when {
            selectedIndex in 0 until model.size -> suggestionList.selectedValue
            model.size > 0 -> model.getElementAt(0)
            else -> null
        }
    }

    private fun acceptSuggestion() {
        val input = textField.text
        val hint = when {
            selectedIndex in 0 until model.size -> suggestionList.selectedValue
            selectedIndex == -1 && !suggestionList.isEmpty -> model.getElementAt(0)
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

        if (input.isEmpty()) {
            textField.hint = null
            setErrorText(null)
            return
        }

        setErrorText(if (trie.keyExists(input)) "Key '$input' already exists!" else null)

        val suggestNextParts = trie.getNextSuggestions(input)
        suggestNextParts.forEach { model.addElement(it) }
        selectedIndex = -1
        updateHint()
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
