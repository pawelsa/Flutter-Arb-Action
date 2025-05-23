package pl.digsa.flutter_arb_action.utils

fun getRestOfHintToShow(enteredText: String, hint: String): String {
    if (enteredText.isEmpty()) return hint

    val parts = enteredText.split("(?=[A-Z])".toRegex())
    val lastPart = parts.lastOrNull()

    return when {
        lastPart == null -> hint
        hint.startsWith(lastPart) -> hint.drop(lastPart.length)
        else -> hint
    }
}