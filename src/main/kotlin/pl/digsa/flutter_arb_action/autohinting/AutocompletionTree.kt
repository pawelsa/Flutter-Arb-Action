package pl.digsa.flutter_arb_action.autohinting

class AutocompletionTree(keys: List<String>) {
    private val startNodes: MutableSet<AutocompletionNode> = mutableSetOf()

    init {
        updateTree(keys)
    }

    fun updateTree(keys: List<String>) {
        keys.forEach { key ->

            val parts = key.cutVariableName().drop(1)
            val newRoot = startNodes.firstOrNull { it.value == parts.first() } ?: AutocompletionNode(parts.first())
            newRoot.addNodes(parts.drop(1))
            startNodes.add(newRoot)
        }
    }

    fun findMatching(text: String?): String? {
        if (text == null) return null

        val parts = text.cutVariableName().drop(1)

        for (node in startNodes) {
            val matching = node.findMatching(parts)
            if (matching != null) {
                return matching
            }
        }
        return null
    }
}

data class AutocompletionNode(val value: String) {
    private val nextNode: MutableSet<AutocompletionNode> = mutableSetOf()

    fun addNodes(parts: List<String>) {
        if (parts.isEmpty()) return
        val newRoot = nextNode.firstOrNull { it.value == parts.first() } ?: AutocompletionNode(parts.first())
        newRoot.addNodes(parts.drop(1))
        nextNode.add(newRoot)
    }

    fun findMatching(parts: List<String>): String? {
        if (parts.isEmpty()) return value
        if (value == parts.first()) {
            return value + (nextNode.firstNotNullOfOrNull { it.findMatching(parts.drop(1)) } ?: "")
        } else if (value.startsWith(parts.first())) {
            return value
        }
        return null
    }

}