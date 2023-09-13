package pl.digsa.flutter_arb_action

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonElementGenerator
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartElementGenerator
import com.localizely.flutter.intl.files.ArbFileType


@Suppress("IntentionDescriptionNotFoundInspection")
class ReplaceStringWithTranslationIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String = "Flutter resources"

    override fun getText(): String = "Move to arb file"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        getStringLiteralExpression(element) != null && getBuildContextParameter(element) != null

    private fun getStringLiteralExpression(element: PsiElement): DartStringLiteralExpression? =
        PsiTreeUtil.getParentOfType(element, DartStringLiteralExpression::class.java)

    private fun getBuildContextParameter(element: PsiElement): DartSimpleFormalParameter? {
        val methodDeclaration = PsiTreeUtil.getParentOfType(element, DartMethodDeclaration::class.java)
        return methodDeclaration?.formalParameterList?.normalFormalParameterList?.firstOrNull {
            it.simpleFormalParameter?.type?.simpleType?.text == "BuildContext"
        }?.simpleFormalParameter
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val contextParameterName = getBuildContextParameter(element)?.componentName?.text ?: return
        val stringElementToReplace = getStringLiteralExpression(element) ?: return
        val localizationValue = getLocalizationValue(stringElementToReplace)

        val localizationVariableName = getNewVariableName() ?: return

        project.addToArbFile(localizationVariableName, localizationValue)

        element.addImport("import 'package:app/extensions/context_extensions.dart';")
        stringElementToReplace.replaceWithNewReference("$contextParameterName.text.$localizationVariableName")
    }

    private fun getLocalizationValue(stringElementToReplace: DartStringLiteralExpression) =
        stringElementToReplace.text.let {
            "\"${it.substring(1, it.length - 1)}\""
        }

    private fun Project.addToArbFile(resourceName: String, value: String) {
        val file = FileTypeIndex.getFiles(ArbFileType(), GlobalSearchScope.projectScope(this)).firstOrNull() ?: return
        val arbFile: JsonFile =
            PsiManager.getInstance(this).findFile(file)?.let { if (it is JsonFile) it else null } ?: return
        val jsonObject = arbFile.topLevelValue
        if (jsonObject !is JsonObject) return

        writeFile {
            JsonPsiUtil.addProperty(jsonObject, createJsonProperty(resourceName, value), false)
        }
    }

    private fun Project.createJsonProperty(
        resourceName: String,
        value: String
    ) = JsonElementGenerator(this).createProperty(resourceName, value)

    private fun DartStringLiteralExpression.replaceWithNewReference(reference: String) = this.project.writeFile {
        val replacementVersion: DartExpression = DartElementGenerator.createExpressionFromText(
            this.project,
            reference
        ) ?: return@writeFile
        this.replace(replacementVersion)
    }

    private fun PsiElement.addImport(import: String): Unit = this.project.writeFile {
        val dartFile = this.parentOfType<DartFile>() ?: return@writeFile
        val importStatements = dartFile.childrenOfType<DartImportStatement>()
        if (importStatements.any { it.text == import }) return@writeFile

        val lastImportStatement = importStatements.last()
        val importStatement = DartElementGenerator.createDummyFile(
            this.project,
            import
        ).firstChild
        dartFile.addAfter(importStatement, lastImportStatement)
    }

    private fun getNewVariableName(): String? {
        lateinit var resourceName: Cell<JBTextField>
        val panel = panel {
            row {
                resourceName = textField().label("Variable name", LabelPosition.TOP).focused().validation {
                    if (it.text.isEmpty()) return@validation ValidationInfo("Field cannot be empty")
                    if (it.text.trim()
                            .contains(" ")
                    ) return@validation ValidationInfo("Field cannot contain white spaces")
                    null
                }
            }
        }

        val popup = PanelDialog(panel, "Name arb variable")
        popup.setOkText("Apply")
        val isGenerate = popup.showAndGet()
        return if (isGenerate) resourceName.component.text.trim() else null
    }

    private fun Project.writeFile(action: () -> Unit) = WriteCommandAction.runWriteCommandAction(this, action)

    override fun startInWriteAction(): Boolean = false
}

