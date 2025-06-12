package pl.digsa.flutter_arb_action.plugin_notifications

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

@Suppress("ClassName")
class WhatsNewIn2_0 : ProjectActivity {
    override suspend fun execute(project: Project) {
        val key = "flutter_arb_action.shown_2_0_0"
        val properties = PropertiesComponent.getInstance()
        if (!properties.getBoolean(key, false)) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Flutter Arb Action")
                .createNotification(
                    "What's New in 2.0 in Flutter Arb Action",
                    "The plugin has been updated. " +
                            "It has improved autocompletion for translation keys and added support for multimodule projects. " +
                            "The configuration changed. Is moved from settings to the l10n.yaml file for each module. " +
                            "How to set it up? Information can be found in the plugin page or on GitHub.",
                    NotificationType.INFORMATION
                )
                .notify(project)
            properties.setValue(key, true)
        }
    }
}