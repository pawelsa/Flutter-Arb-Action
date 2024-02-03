@file:Suppress("ConstPropertyName")

object ModuleConfig {
    const val group = "pl.digsa"
    const val version = "1.0.2"

    object IntellijPlugins {
        private object Versions {
            const val dart = "222.4582"
            const val yaml = "222.3739.67"
        }

        private const val _dart = "Dart:${Versions.dart}"
        private const val _yaml = "org.jetbrains.plugins.yaml:${Versions.yaml}"

        val plugins = listOf(_dart, _yaml)
    }
}
