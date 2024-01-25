package pl.digsa.flutter_arb_action.utils

import com.intellij.json.psi.JsonObject
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.codeStyle.CodeStyleManager

internal fun reformatJsonFile(
    project: Project,
    editor: Editor,
    obj: JsonObject
) {
    val pointer = SmartPointerManager.createPointer<JsonObject>(obj)
    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
    val element = pointer.element ?: return
    val codeStyleManager = CodeStyleManager.getInstance(project)
    codeStyleManager.reformatText(
        element.containingFile,
        setOf(element.textRange)
    )
}