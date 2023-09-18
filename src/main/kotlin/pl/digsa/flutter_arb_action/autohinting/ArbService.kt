package pl.digsa.flutter_arb_action.autohinting

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiManager
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import pl.digsa.flutter_arb_action.firstFileByName

@Service(Service.Level.PROJECT)
class ArbService(private val project: Project) : Disposable {
    val autocompletionTree = AutocompletionTree(emptyList())
    private val arbListener = project.getArbFileOrNull()?.let { arbFile ->
        val topLevelValue = arbFile.topLevelValue
        if (topLevelValue is JsonObject) {
            autocompletionTree.updateTree(topLevelValue.propertyList.map { it.name })
        }
        ArbFileListener(arbFile) { jsonObject ->
            autocompletionTree.updateTree(jsonObject.propertyList.map { it.name })
        }
    }

    init {
        if (arbListener != null) {
            val psiManager = PsiManager.getInstance(project)
            psiManager.addPsiTreeChangeListener(arbListener, this)
        }
    }

    private fun Project.getArbFileOrNull(): JsonFile? {
        val intlConfig = firstFileByName<YAMLFile>("l10n.yaml") ?: return null
        val arbFileName = getArbFileNameFromIntlConfig(intlConfig) ?: return null
        return firstFileByName<JsonFile>(arbFileName)
    }

    private fun getArbFileNameFromIntlConfig(intlConfig: YAMLFile): String? {
        val yamlProperties = (intlConfig.documents.firstOrNull()?.topLevelValue as YAMLMapping).keyValues
        val templateFile = yamlProperties.firstOrNull { it.keyText == "template-arb-file" }?.valueText ?: return null
        val templateDir = yamlProperties.firstOrNull { it.keyText == "arb-dir" }?.valueText ?: return null
        return "$templateDir/$templateFile"
    }

    override fun dispose() {
        if (arbListener != null) {
            val psiManager = PsiManager.getInstance(project)
            psiManager.removePsiTreeChangeListener(arbListener)
        }
    }
}