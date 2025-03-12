package pl.digsa.flutter_arb_action.autohinting2

data class TrieNode(
    val children: MutableMap<String, TrieNode> = mutableMapOf(),
    var isEndOfKey: Boolean = false,
)
