package pl.digsa.flutter_arb_action

import com.intellij.json.psi.JsonElementGenerator
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression
import com.jetbrains.lang.dart.util.DartElementGenerator

fun Project.createJsonProperty(
    resourceName: String,
    value: String
) = JsonElementGenerator(this).createProperty(resourceName, value)

fun Project.createJsonComma() = JsonElementGenerator(this).createComma()

fun Project.writeFile(action: () -> Unit) = WriteCommandAction.runWriteCommandAction(this, action)

fun DartStringLiteralExpression.replaceWithNewReference(reference: String) = this.project.writeFile {
    val replacementVersion: DartExpression = DartElementGenerator.createExpressionFromText(
        this.project,
        reference
    ) ?: return@writeFile
    this.replace(replacementVersion)
}

inline fun PsiElement.iterateOverSiblings(action: (PsiElement) -> Unit) {
    var element: PsiElement? = firstChild
    while (element != null) {
        action(element)
        element = element.nextSibling
    }
}

internal fun String.ignoreToolsSymbol() = if (startsWith("@")) drop(1) else this

internal fun String.splitCamelCase() = split("(?=[A-Z])".toRegex())