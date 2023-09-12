package pl.digsa.flutter_arb_action

inline fun <reified R> Sequence<*>.firstIsInstanceOrNull(): R? {
    for (element in this) if (element is R) return element
    return null
}