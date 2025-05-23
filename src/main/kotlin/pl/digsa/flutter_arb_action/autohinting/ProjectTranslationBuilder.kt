package pl.digsa.flutter_arb_action.autohinting

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

object ProjectTranslationBuilder {

    fun getTrie(project: Project, file: VirtualFile): KeyTrie {
        val arbFile = PsiManager.getInstance(project).findFile(file)?.let { if (it is JsonFile) it else null }
        val autocompletionTree = mutableSetOf<String>()
        val topLevelValue = arbFile?.topLevelValue
        if (topLevelValue is JsonObject) {
            autocompletionTree.addAll(topLevelValue.propertyList.map { it.name })
        }

        val trie = KeyTrie()
        for (key in autocompletionTree) {
            trie.insert(key)
        }

        println("✅ Trie updated for ${file.name} in project ${project.name} - ${trie.keyExists("home")}")
        return trie
    }
}
