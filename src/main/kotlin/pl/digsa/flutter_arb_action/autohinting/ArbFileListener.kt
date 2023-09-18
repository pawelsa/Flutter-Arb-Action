package pl.digsa.flutter_arb_action.autohinting

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiTreeChangeAdapter
import com.intellij.psi.PsiTreeChangeEvent

class ArbFileListener(private val fileToObserve: JsonFile, private val actionWhenChanged: (JsonObject) -> Unit) :
    PsiTreeChangeAdapter() {

    override fun childrenChanged(event: PsiTreeChangeEvent) {
        val file = event.file
        if (file == fileToObserve && file is JsonFile) {
            val topLevelValue = file.topLevelValue
            if (topLevelValue is JsonObject) {
                actionWhenChanged(topLevelValue)
            }
        }
    }
}