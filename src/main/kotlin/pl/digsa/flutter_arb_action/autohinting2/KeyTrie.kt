package pl.digsa.flutter_arb_action.autohinting2

class KeyTrie {
    private val root = TrieNode()

    fun insert(key: String) {
        var node = root
        val parts = key.split("(?=[A-Z])".toRegex()) // Split camelCase

        for (part in parts) {
            node = node.children.computeIfAbsent(part) { TrieNode() }
        }
        node.isEndOfKey = true
    }

    private fun findClosestMatch(prefix: String): Set<String> {
        var node = root
        if (prefix.isEmpty()) return node.children.keys

        val parts = prefix.split("(?=[A-Z])".toRegex())

        var currentPart = ""
        for (part in parts) {
            currentPart = part
            if (node.children.containsKey(part)) {
                node = node.children[part]!!
                currentPart = ""
            } else {
                break
            }
        }

        return node.children.keys.filter { it.startsWith(currentPart, ignoreCase = true) }.toSet()
    }

    fun getNextSuggestions(input: String): List<String> {
        return findClosestMatch(input).toList()
    }

    fun keyExists(key: String): Boolean {
        var node = root
        val parts = key.split("(?=[A-Z])".toRegex())

        for (part in parts) {
            node = node.children[part] ?: return false
        }
        return node.isEndOfKey
    }
}