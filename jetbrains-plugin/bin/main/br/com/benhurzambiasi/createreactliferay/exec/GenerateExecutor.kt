package br.com.benhurzambiasi.createreactliferay.exec

import br.com.benhurzambiasi.createreactliferay.model.GeneratePayload
import br.com.benhurzambiasi.createreactliferay.settings.CreateReactLiferaySettings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.writeText

object GenerateExecutor {
    fun run(project: Project?, payload: GeneratePayload) {
        val settings = CreateReactLiferaySettings.getInstance()
        val configuredNodePath = settings.nodePath.trim()
        if (configuredNodePath.isNotEmpty() && !NodeExec.isValidNodePath(configuredNodePath)) {
            Messages.showErrorDialog(project, "Node inválido nas Settings do plugin.", "Create React Liferay")
            return
        }
        val nodeCommand = NodeExec.resolveNodeCommand(configuredNodePath)

        object : Task.Backgroundable(project, "Create React Liferay: Gerando módulo", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Preparando payload"
                val payloadFile = Files.createTempFile("create-react-liferay-", ".json")
                payloadFile.writeText(payloadToJson(payload), StandardCharsets.UTF_8)

                val pluginId = PluginId.getId("br.com.benhurzambiasi.create-react-liferay")
                val pluginDescriptor = PluginManagerCore.getPlugin(pluginId)
                val pluginPath = pluginDescriptor?.pluginPath
                    ?: throw IllegalStateException("Plugin path não encontrado.")
                val runnerPath = pluginPath.resolve("modules/runner/run.js").toString()

                val cmd = GeneralCommandLine(nodeCommand, runnerPath, "--payloadFile", payloadFile.toString())
                    .withCharset(StandardCharsets.UTF_8)
                val output = CapturingProcessHandler(cmd).runProcess(15 * 60 * 1000)

                try {
                    Files.deleteIfExists(payloadFile)
                } catch (_: Exception) {
                }

                if (output.exitCode == 0) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Create React Liferay")
                        .createNotification(
                            "Módulo ${payload.safeProjectName} criado com sucesso!",
                            output.stdout.trim(),
                            NotificationType.INFORMATION
                        )
                        .notify(project)
                } else {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Create React Liferay")
                        .createNotification(
                            "Falha ao criar módulo",
                            (output.stderr.ifBlank { output.stdout }).trim(),
                            NotificationType.ERROR
                        )
                        .notify(project)
                }
            }
        }.queue()
    }

    private fun payloadToJson(payload: GeneratePayload): String {
        fun esc(v: String): String = v
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        fun str(v: String?): String = if (v == null) "null" else "\"${esc(v)}\""

        return """
            {
              "type": ${str(payload.type)},
              "targetDir": ${str(payload.targetDir)},
              "safeProjectName": ${str(payload.safeProjectName)},
              "displayName": ${str(payload.displayName)},
              "liferayDir": ${str(payload.liferayDir)},
              "category": ${str(payload.category)},
              "hasSharedBundle": ${if (payload.hasSharedBundle) "true" else "false"},
              "sharedBundleDirName": ${str(payload.sharedBundleDirName)},
              "selectedTemplate": {
                "name": ${str(payload.selectedTemplate.name)},
                "dir": ${str(payload.selectedTemplate.dir)},
                "hasShared": ${if (payload.selectedTemplate.hasShared) "true" else "false"}
              }
            }
        """.trimIndent()
    }
}

