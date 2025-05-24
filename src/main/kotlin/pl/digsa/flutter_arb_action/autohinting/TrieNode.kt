package pl.digsa.flutter_arb_action.autohinting

data class TrieNode(
    val children: MutableMap<String, TrieNode> = mutableMapOf(),
    var isEndOfKey: Boolean = false,
)
