@file:Suppress("ConstPropertyName")

object ModuleConfig {
    const val group = "pl.digsa"
    const val version = "1.0.1"

    object IntellijPlugins {
        private object Versions {
            const val dart = "222.4582"
            const val flutterIntl = "1.18.4-2022.2"
            const val yaml = "222.3739.67"
        }

        private const val _dart = "Dart:${Versions.dart}"
        private const val _flutterIntl = "com.localizely.flutter-intl:${Versions.flutterIntl}"
        private const val _yaml = "org.jetbrains.plugins.yaml:${Versions.yaml}"

        val plugins = listOf(_dart, _flutterIntl, _yaml)
    }
}
