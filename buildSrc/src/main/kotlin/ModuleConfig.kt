@file:Suppress("ConstPropertyName")

object ModuleConfig {
    const val group = "pl.digsa"
    const val version = "1.0.4"

    object IntellijPlugins {
        private object Versions {
            const val dart = "242.20629"
            const val yaml = "242.20224.237"
            const val flutterIntl = "1.18.8-2024.2"
        }

        private const val _dart = "Dart:${Versions.dart}"
        private const val _flutterIntl = "com.localizely.flutter-intl:${Versions.flutterIntl}"
        private const val _yaml = "org.jetbrains.plugins.yaml:${Versions.yaml}"

        val plugins = listOf(_dart, _flutterIntl, _yaml)
    }
}
