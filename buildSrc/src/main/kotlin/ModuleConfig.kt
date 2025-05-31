@file:Suppress("ConstPropertyName")

object ModuleConfig {
    const val id = "pl.digsa.flutter_arb_action"
    const val version = "2.0.0"

    object IntellijPlugins {
        private object Versions {
            const val dart = "242.20629"
            const val yaml = "242.20224.237"
            const val flutterIntl = "1.18.8-2024.2"
        }

        private val _dart = "Dart" to Versions.dart
        private val _flutterIntl = "com.localizely.flutter-intl" to Versions.flutterIntl
        private val _yaml = "org.jetbrains.plugins.yaml" to Versions.yaml

        val plugins = listOf(_dart, _flutterIntl, _yaml)
    }
}
