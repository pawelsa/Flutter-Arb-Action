package pl.digsa.flutter_arb_action

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.lang.dart.psi.DartExpression
import com.jetbrains.lang.dart.psi.DartFile
import com.jetbrains.lang.dart.psi.DartImportStatement
import com.jetbrains.lang.dart.psi.DartMethodDeclaration
import com.jetbrains.lang.dart.psi.DartSimpleFormalParameter
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression
import com.jetbrains.lang.dart.util.DartElementGenerator


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

        val localizationVariableName = getNewVariableName() ?: return

        element.addImport("import 'package:app/extensions/context_extensions.dart';")
        stringElementToReplace.replaceWithNewReference("$contextParameterName.text.$localizationVariableName")
    }

    private fun DartStringLiteralExpression.replaceWithNewReference(reference: String    ) {
        val replacementVersion: DartExpression = DartElementGenerator.createExpressionFromText(
            this.project,
            reference
        ) ?: return
        this.replace(replacementVersion)
    }

    private fun PsiElement.addImport(import: String) {
        val importStatement =
            DartElementGenerator.createDummyFile(
                project,
                import
            ).firstChild
        val dartFile = parentOfType<DartFile>()
        val lastImportStatement = dartFile?.childrenOfType<DartImportStatement>()?.last()
        dartFile?.addAfter(importStatement, lastImportStatement)
    }

    private fun getNewVariableName(): String? {
        lateinit var resourceName: Cell<JBTextField>
        val panel = panel {
            row {
                resourceName = textField().label("Variable name", LabelPosition.TOP)
            }
        }

        val popup = PanelDialog(panel, "Name arb variable")
        popup.setOkText("Apply")
        val isGenerate = popup.showAndGet()
        return if(isGenerate) resourceName.component.text else null
    }

    override fun startInWriteAction(): Boolean = false
}
