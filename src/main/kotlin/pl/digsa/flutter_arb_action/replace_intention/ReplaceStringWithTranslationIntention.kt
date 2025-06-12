package pl.digsa.flutter_arb_action.replace_intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.parentOfType
import com.jetbrains.lang.dart.psi.*
import com.jetbrains.lang.dart.util.DartElementGenerator
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import pl.digsa.flutter_arb_action.*
import pl.digsa.flutter_arb_action.autohinting.ProjectTranslationBuilder
import pl.digsa.flutter_arb_action.autohinting.TranslationKeyDialog
import pl.digsa.flutter_arb_action.utils.reformatJsonFile

class ReplaceStringWithTranslationIntention : PsiElementBaseIntentionAction(), IntentionAction {
    override fun getFamilyName(): String = "Flutter resources"

    override fun getText(): String = "Move to arb file"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        element.findStringLiteralExpressionInParentOrNull() != null && element.findBuildContextInParentOrNull() != null && editor?.virtualFile?.let {
            project.getL10nProperties(
                it
            )
        } != null

    private fun PsiElement.findStringLiteralExpressionInParentOrNull(): DartStringLiteralExpression? =
        PsiTreeUtil.getParentOfType(this, DartStringLiteralExpression::class.java)

    private fun PsiElement.findBuildContextInParentOrNull(): DartSimpleFormalParameter? {
        val methodDeclaration = PsiTreeUtil.getParentOfType(this, DartMethodDeclaration::class.java)
        return methodDeclaration?.formalParameterList?.normalFormalParameterList?.firstOrNull {
            it.simpleFormalParameter?.type?.simpleType?.text == BUILD_CONTEXT
        }?.simpleFormalParameter
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        editor ?: return

        val contextParameterName = element.findBuildContextInParentOrNull()?.componentName?.text ?: return
        val stringElementToReplace = element.findStringLiteralExpressionInParentOrNull() ?: return
        val l10nProperties = project.getL10nProperties(editor.virtualFile ?: return) ?: return

        val variableName = project.getNewVariableName(l10nProperties.arbFile) ?: return

        val refactorArguments = getRefactorArguments(stringElementToReplace, variableName) ?: return

        val jsonFile = PsiManager.getInstance(project).findFile(l10nProperties.arbFile) as? JsonFile ?: return
        val jsonObject = jsonFile.topLevelValue as? JsonObject ?: return
        project.modifyArbFileContent(editor, jsonObject, refactorArguments)

        element.addImport(l10nProperties.importPath)
        val replacementReference =
            "$contextParameterName.${l10nProperties.extensionName}.${refactorArguments.variableWithParameters}"
        stringElementToReplace.replaceWithNewReference(replacementReference)
    }

    private fun Project.getL10nProperties(currentFile: VirtualFile): L10nProperties? {
        var dir = currentFile.parent
        while (dir != null) {
            val l10nFile = dir.findChild("l10n.yaml")
            if (l10nFile != null && !l10nFile.isDirectory) {
                val psiFile = PsiManager.getInstance(this).findFile(l10nFile) as? YAMLFile ?: return null
                val yamlProperties = psiFile.documents.firstOrNull()?.topLevelValue as? YAMLMapping ?: return null
                val yaml = L10nYamlProperties.fromYamlMapping(yamlProperties) ?: return null
                val arbFile = dir.findFileByRelativePath(yaml.arbFilePath) ?: return null
                return L10nProperties(arbFile, yaml.importPath, yaml.extensionName)
            }
            dir.findChild("pubspec.yaml")?.let { return null }
            dir = dir.parent
        }
        return null
    }

    private fun Project.modifyArbFileContent(
        editor: Editor?,
        arbContent: JsonObject,
        refactorArguments: RefactorArguments
    ) = writeFile {
        addPropertyToArbFile(arbContent, refactorArguments.variableName, refactorArguments.arbValue)
        refactorArguments.arbTemplate?.let {
            addPropertyToArbFile(arbContent, refactorArguments.arbTemplateName, it)
        }
        editor?.let { reformatJsonFile(this, it, arbContent) }
    }

    private fun getRefactorArguments(
        stringElementToReplace: DartStringLiteralExpression,
        variableName: String
    ): RefactorArguments? {
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

    private fun Project.getNewVariableName(arbFile: VirtualFile): String? {
        val trie = ProjectTranslationBuilder.getTrie(this, arbFile)
        val dialog = TranslationKeyDialog(this, trie)
        dialog.beforeShowCallback()
        val isGenerate = dialog.showAndGet()
        return if (isGenerate) dialog.getResult()?.trim() else null
    }

    override fun startInWriteAction(): Boolean = false

    companion object {
        private const val BUILD_CONTEXT = "BuildContext"
    }
}

private data class RefactorArguments(
    val arbValue: String,
    val arbTemplate: String?,
    val variableName: String,
    val methodParameters: String
) {
    val arbTemplateName = "@${variableName}"
    val variableWithParameters = "${variableName}${methodParameters}"
}

data class L10nYamlProperties(
    val arbFilePath: String,
    val importPath: String,
    val extensionName: String,
) {
    companion object {
        private const val TEMPLATE_ARBITRARY_FILE = "template-arb-file"
        private const val ARB_DIR = "arb-dir"
        private const val EXTENSION_IMPORT_PATH = "extension-import-path"
        private const val EXTENSION_NAME = "extension-name"

        fun fromYamlMapping(mapping: YAMLMapping): L10nYamlProperties? {
            val keys = mapping.keyValues

            val templateArbFile = keys.firstOrNull { it.keyText == TEMPLATE_ARBITRARY_FILE }?.valueText ?: return null
            val arbDir = keys.firstOrNull { it.keyText == ARB_DIR }?.valueText ?: return null
            val importPath = keys.firstOrNull { it.keyText == EXTENSION_IMPORT_PATH }?.valueText ?: return null
            val extensionName = keys.firstOrNull { it.keyText == EXTENSION_NAME }?.valueText ?: return null

            return L10nYamlProperties("$arbDir/$templateArbFile", "import \'$importPath\';", extensionName)
        }
    }
}

data class L10nProperties(
    val arbFile: VirtualFile,
    val importPath: String,
    val extensionName: String,
)
