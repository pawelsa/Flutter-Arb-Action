package pl.digsa.flutter_arb_action

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartElementGenerator
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import pl.digsa.flutter_arb_action.settings.ArbPluginSettingsState


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

        val (import, extensionName) = project.readUserDefinedParametersSettings()
        element.addImport(import)
        stringElementToReplace.replaceWithNewReference("$contextParameterName.$extensionName.$localizationVariableName")
    }

    private fun getLocalizationValue(stringElementToReplace: DartStringLiteralExpression) =
        stringElementToReplace.text.let {
            "\"${it.substring(1, it.length - 1)}\""
        }


    private fun Project.addToArbFile(resourceName: String, value: String) {
        val jsonObject = arbTopLevelValue ?: return
        if (jsonObject !is JsonObject) return

        writeFile {
            JsonPsiUtil.addProperty(jsonObject, createJsonProperty(resourceName, value), false)
        }
    }

    private val Project.arbTopLevelValue: JsonValue?
        get() {
            val intlConfig = firstFileByName<YAMLFile>("l10n.yaml") ?: return null
            val arbFileName = getArbFileNameFromIntlConfig(intlConfig) ?: return null
            return firstFileByName<JsonFile>(arbFileName)?.topLevelValue
        }

    private fun getArbFileNameFromIntlConfig(intlConfig: YAMLFile): String? {
        val yamlProperties = (intlConfig.documents.firstOrNull()?.topLevelValue as YAMLMapping).keyValues
        val templateFile = yamlProperties.firstOrNull { it.keyText == "template-arb-file" }?.valueText ?: return null
        val templateDir = yamlProperties.firstOrNull { it.keyText == "arb-dir" }?.valueText ?: return null
        return "$templateDir/$templateFile"
    }

    private fun Project.readUserDefinedParametersSettings() =
        getService(ArbPluginSettingsState::class.java).state.run { importPath to extensionName }

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

    override fun startInWriteAction(): Boolean = false

}


