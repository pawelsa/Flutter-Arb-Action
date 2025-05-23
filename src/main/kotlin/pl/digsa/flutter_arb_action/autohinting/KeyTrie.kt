package pl.digsa.flutter_arb_action.autohinting

import pl.digsa.flutter_arb_action.splitCamelCase

class KeyTrie {
    private val root = TrieNode()

    fun insert(key: String) {
        var node = root
        val parts = key.splitCamelCase()

        for (part in parts) {
            node = node.children.computeIfAbsent(part) { TrieNode() }
        }
        node.isEndOfKey = true
    }

    private fun findClosestMatch(prefix: String): Set<String> {
        var node = root
        if (prefix.isEmpty()) return node.children.keys

        val parts = prefix.splitCamelCase()

        var incompletePrefix = ""
        for ((index, part) in parts.withIndex()) {
            if (node.children.containsKey(part)) {
                node = node.children[part]!!
                incompletePrefix = ""
            } else {
                incompletePrefix = part
                if (index < parts.lastIndex) return emptySet()
                break
            }
        }

        return node.children.keys.filter { it.startsWith(incompletePrefix, ignoreCase = true) }.toSet()
    }

    fun getNextSuggestions(input: String): List<String> {
        return findClosestMatch(input).toList()
    }

    fun keyExists(key: String): Boolean {
        var node = root
        val parts = key.splitCamelCase()

        for (part in parts) {
            node = node.children[part] ?: return false
        }
        return node.isEndOfKey
    }
}