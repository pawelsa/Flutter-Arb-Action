package pl.digsa.flutter_arb_action.replace_intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartElementGenerator
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import pl.digsa.flutter_arb_action.*
import pl.digsa.flutter_arb_action.autohinting2.TranslationKeyDialog
import pl.digsa.flutter_arb_action.settings.ArbPluginSettingsState
import pl.digsa.flutter_arb_action.utils.reformatJsonFile


@Suppress("IntentionDescriptionNotFoundInspection")
class ReplaceStringWithTranslationIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String = "Flutter resources"

    override fun getText(): String = "Move to arb file"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        element.findStringLiteralExpressionInParentOrNull() != null && element.findBuildContextInParentOrNull() != null

    private fun PsiElement.findStringLiteralExpressionInParentOrNull(): DartStringLiteralExpression? =
        PsiTreeUtil.getParentOfType(this, DartStringLiteralExpression::class.java)

    private fun PsiElement.findBuildContextInParentOrNull(): DartSimpleFormalParameter? {
        val methodDeclaration = PsiTreeUtil.getParentOfType(this, DartMethodDeclaration::class.java)
        return methodDeclaration?.formalParameterList?.normalFormalParameterList?.firstOrNull {
            it.simpleFormalParameter?.type?.simpleType?.text == "BuildContext"
        }?.simpleFormalParameter
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val contextParameterName = element.findBuildContextInParentOrNull()?.componentName?.text ?: return
        val stringElementToReplace = element.findStringLiteralExpressionInParentOrNull() ?: return
        val arbContent = project.getArbObjectOrNull() ?: return

        val refactorArguments = getRefactorArguments(stringElementToReplace) ?: return

        project.modifyArbFileContent(editor, arbContent, refactorArguments)

        val (import, extensionName) = project.readUserDefinedParametersSettings()
        element.addImport(import)
        val replacementReference = "$contextParameterName.$extensionName.${refactorArguments.variableWithParameters}"
        stringElementToReplace.replaceWithNewReference(replacementReference)
    }

    private fun Project.modifyArbFileContent(
        editor: Editor?,
        arbContent: JsonObject,
        refactorArguments: RefactorArguments
    ) = writeFile {
        addPropertyToArbFile(arbContent, refactorArguments.variableName, refactorArguments.arbValue)
        if (refactorArguments.arbTemplate != null) {
            addPropertyToArbFile(arbContent, refactorArguments.arbTemplateName, refactorArguments.arbTemplate)
        }
        editor?.let {
            reformatJsonFile(this, it, arbContent)
        }
    }

    private fun getRefactorArguments(
        stringElementToReplace: DartStringLiteralExpression,
    ): RefactorArguments? {
        val project = stringElementToReplace.project
        val variableName = project.getNewVariableName() ?: return null
        var parameterCount = 1
        var arbValue = ""
        val parameters = mutableListOf<String>()
        val templates = mutableListOf<String>()

        stringElementToReplace.iterateOverSiblings { element ->

            when (element) {
                is DartShortTemplateEntry -> {
                    val parameterName = "p${parameterCount++}"

                    element.expression?.text?.let(parameters::add)
                    arbValue += "{$parameterName}"
                    templates += parameterName
                }

                is DartLongTemplateEntry -> {
                    val parameterName = "p${parameterCount++}"

                    element.expression?.text?.let(parameters::add)
                    arbValue += "{$parameterName}"
                    templates += parameterName
                }

                else -> arbValue += element.text
            }
        }

        val methodParameters = if (parameters.isEmpty()) "" else "(${parameters.joinToString()})"
        val arbTemplateValue = if (templates.isNotEmpty()) {
            val defineTemplates = templates.joinToString { "\"$it\": {}" }
            "{ \"placeholders\": { $defineTemplates }}"
        } else null
        arbValue = "\"${arbValue.substring(1, arbValue.length - 1)}\""
        return RefactorArguments(arbValue, arbTemplateValue, variableName, methodParameters)
    }


    private fun Project.addPropertyToArbFile(
        jsonObject: JsonObject,
        resourceName: String,
        value: String,
    ) {
        val jsonProperty = createJsonProperty(resourceName, value)
        val addedProperty = addJsonProperty(jsonObject, jsonProperty)
        addCommaIfNecessary(jsonObject, addedProperty)
    }

    private fun Project.addCommaIfNecessary(
        jsonObject: JsonObject,
        addedProperty: PsiElement
    ): PsiElement? {
        val lastProperty = jsonObject.lastChild
        if (lastProperty != addedProperty) {
            val jsonComma = createJsonComma()
            jsonObject.addAfter(jsonComma, addedProperty)
            return jsonComma
        }
        return null
    }

    private fun addJsonProperty(
        jsonObject: JsonObject,
        jsonProperty: JsonProperty,
    ): PsiElement {
        val propertyName = jsonProperty.name.ignoreToolsSymbol()
        val elementToPlaceBefore =
            jsonObject.propertyList.firstOrNull { it.name > propertyName }
        return if (elementToPlaceBefore != null) {
            jsonObject.addBefore(jsonProperty, elementToPlaceBefore)
        } else {
            JsonPsiUtil.addProperty(jsonObject, jsonProperty, false)
        }
    }

    private fun Project.getArbObjectOrNull(): JsonObject? {
        val intlConfig = firstFileByName<YAMLFile>("l10n.yaml") ?: return null
        val arbFileName = getArbFileNameFromIntlConfig(intlConfig) ?: return null
        return firstFileByName<JsonFile>(arbFileName)?.topLevelValue?.let { if (it is JsonObject) it else null }
    }

    private fun getArbFileNameFromIntlConfig(intlConfig: YAMLFile): String? {
        val yamlProperties = (intlConfig.documents.firstOrNull()?.topLevelValue as YAMLMapping).keyValues
        val templateFile = yamlProperties.firstOrNull { it.keyText == "template-arb-file" }?.valueText ?: return null
        val templateDir = yamlProperties.firstOrNull { it.keyText == "arb-dir" }?.valueText ?: return null
        return "$templateDir/$templateFile"
    }

    private fun Project.readUserDefinedParametersSettings() =
        getService(ArbPluginSettingsState::class.java).state

    private fun PsiElement.addImport(import: String): Unit = this.project.writeFile {
        val dartFile = this.parentOfType<DartFile>() ?: return@writeFile
        val importStatements = dartFile.childrenOfType<DartImportStatement>()
        if (importStatements.any { it.text == import }) return@writeFile

        val importStatement = DartElementGenerator.createDummyFile(
            this.project,
            import
        ).firstChild
        if (importStatements.isEmpty()) {

            val partOfStatement = dartFile.childrenOfType<DartPartOfStatement>().firstOrNull()
            if (partOfStatement == null) {
                dartFile.addBefore(importStatement, dartFile.firstChild)
            } else {
                findLibraryFileAndAddThereAnImportStatement(partOfStatement, import, importStatement)
            }
        } else {
            val lastImportStatement = importStatements.last()
            dartFile.addAfter(importStatement, lastImportStatement)
        }
    }

    private fun findLibraryFileAndAddThereAnImportStatement(
        partOfStatement: DartPartOfStatement,
        import: String,
        importStatement: PsiElement
    ) {
        val libraryStatement = partOfStatement.childrenOfType<DartLibraryId>().firstOrNull()?.reference?.resolve()
            ?.parentOfType<DartLibraryStatement>() ?: return

        val importStatements = libraryStatement.containingFile.childrenOfType<DartImportStatement>()
        if (importStatements.any { it.text == import }) return

        if (importStatements.isEmpty()) {
            libraryStatement.parent.addAfter(importStatement, libraryStatement)
        } else {
            libraryStatement.addAfter(importStatement, importStatements.last())
        }
    }

    private fun Project.getNewVariableName(): String? {
        val arbFile = getArbFileOrNull()
        val autocompletionTree = mutableSetOf<String>()
        val topLevelValue = arbFile?.topLevelValue
        if (topLevelValue is JsonObject) {
            autocompletionTree.addAll(topLevelValue.propertyList.map { it.name })
        }
        val dialog = TranslationKeyDialog(this, autocompletionTree)
        dialog.beforeShowCallback()
        val isGenerate = dialog.showAndGet()
        return if (isGenerate) dialog.getResult()?.trim() else null
    }

    private fun Project.getArbFileOrNull(): JsonFile? {
        val intlConfig = firstFileByName<YAMLFile>("l10n.yaml") ?: return null
        val arbFileName = getArbFileNameFromIntlConfig(intlConfig) ?: return null
        return firstFileByName<JsonFile>(arbFileName)
    }

    override fun startInWriteAction(): Boolean = false

}

data class RefactorArguments(
    val arbValue: String,
    val arbTemplate: String?,
    val variableName: String,
    val methodParameters: String
) {
    val arbTemplateName = "@${variableName}"
    val variableWithParameters = "${variableName}${methodParameters}"
}
