package pl.digsa.flutter_arb_action.autohinting

class AutocompletionTree(keys: List<String>) {
    private val startNodes: MutableSet<AutocompletionNode> = mutableSetOf()

    init {
        keys.forEach {

            val parts = it.cutVariableName()
            val newRoot = startNodes.firstOrNull { it.value == parts.first() } ?: AutocompletionNode(parts.first())
            newRoot.addNodes(parts.drop(1))
            startNodes.add(newRoot)

        }
    }

    fun findMatching(text: String?): String? {
        if (text == null) return null

        val parts = text.cutVariableName()

        for (node in startNodes) {
            val matching = node.findMatching(parts)
            if (matching != null) {
                return matching
            }
        }
        return null
    }
}

data class AutocompletionNode(val value: String, val nextNode: MutableSet<AutocompletionNode> = mutableSetOf()) {

    fun addNodes(parts: List<String>) {
        if (parts.isEmpty()) return
        val newRoot = nextNode.firstOrNull { it.value == parts.first() } ?: AutocompletionNode(parts.first())
        newRoot.addNodes(parts.drop(1))
        nextNode.add(newRoot)
    }

    fun findMatching(parts: List<String>): String? {
        if (parts.isEmpty()) return null
        if (value == parts.first()) {
            return value + (nextNode.firstOrNull { it.findMatching(parts.drop(1)) != null }?.value ?: "")
        } else if (value.contains(parts.first())) {
            return value
        }
        return null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutocompletionNode

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

}