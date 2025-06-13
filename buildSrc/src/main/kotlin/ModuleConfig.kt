@file:Suppress("ConstPropertyName")

object ModuleConfig {
    const val id = "pl.digsa.flutter_arb_action"
    const val version = "2.0.1+243-251"

    object IntellijPlugins {
        private object Versions {
            const val dart = "243.23177"
            const val yaml = "243.22562.145"
            const val flutterIntl = "1.18.8-2024.2"
        }

        private val _dart = "Dart" to Versions.dart
        private val _flutterIntl = "com.localizely.flutter-intl" to Versions.flutterIntl
        private val _yaml = "org.jetbrains.plugins.yaml" to Versions.yaml
        const val json = "com.intellij.modules.json"

        val plugins = listOf(_dart, _flutterIntl, _yaml)
    }
}
