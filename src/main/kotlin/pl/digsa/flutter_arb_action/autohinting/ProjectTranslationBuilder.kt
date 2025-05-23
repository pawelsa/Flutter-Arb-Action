package pl.digsa.flutter_arb_action.autohinting

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

object ProjectTranslationBuilder {

    fun getTrie(project: Project, file: VirtualFile): KeyTrie {
        val arbFile = PsiManager.getInstance(project).findFile(file) as? JsonFile
        val jsonObject = arbFile?.topLevelValue as? JsonObject ?: return KeyTrie()

        val arbKeys = jsonObject.propertyList
            .map { it.name }
            .filter { !it.startsWith("@") }

        val trie = KeyTrie()
        arbKeys.forEach(trie::insert)
        return trie
    }
}
