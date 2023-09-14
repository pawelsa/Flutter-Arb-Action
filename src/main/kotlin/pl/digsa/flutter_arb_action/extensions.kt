package pl.digsa.flutter_arb_action

import com.intellij.json.psi.JsonElementGenerator
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression
import com.jetbrains.lang.dart.util.DartElementGenerator

inline fun <reified R> Sequence<*>.firstIsInstanceOrNull(): R? {
    for (element in this) if (element is R) return element
    return null
}

inline fun <reified T : PsiFile> Project.firstFileByName(name: String): T? {
    val virtualFile =
        FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.projectScope(this)).firstOrNull() ?: return null
    return PsiManager.getInstance(this).findFile(virtualFile)?.let { if (it is T) it else null }
}

fun Project.createJsonProperty(
    resourceName: String,
    value: String
) = JsonElementGenerator(this).createProperty(resourceName, value)

fun Project.writeFile(action: () -> Unit) = WriteCommandAction.runWriteCommandAction(this, action)

fun DartStringLiteralExpression.replaceWithNewReference(reference: String) = this.project.writeFile {
    val replacementVersion: DartExpression = DartElementGenerator.createExpressionFromText(
        this.project,
        reference
    ) ?: return@writeFile
    this.replace(replacementVersion)
}