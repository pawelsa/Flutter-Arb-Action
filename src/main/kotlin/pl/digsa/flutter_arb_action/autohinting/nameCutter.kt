package pl.digsa.flutter_arb_action.autohinting

private val regex = Regex("(?=[A-Z]+|[A-Z]?[a-z]+)(?=[A-Z]|\\b)")
fun String.cutVariableName(): List<String> {
    return split(regex)
}