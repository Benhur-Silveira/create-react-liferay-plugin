package br.com.benhurzambiasi.createreactliferay.ui

import br.com.benhurzambiasi.createreactliferay.model.GeneratePayload
import br.com.benhurzambiasi.createreactliferay.model.SelectedTemplate
import br.com.benhurzambiasi.createreactliferay.util.NameUtils
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class GenerateFlow(private val project: Project?) {
    fun run(): GeneratePayload? {
        val type = chooseType() ?: return null
        val payload = when (type) {
            "shared" -> runSharedFlow() ?: return null
            else -> runWidgetFlow() ?: return null
        }
        return payload
    }

    private fun chooseType(): String? {
        val options = arrayOf("Widget React", "Shared Bundle")
        val index = Messages.showDialog(
            project,
            "Qual o tipo do módulo que você quer criar?",
            "Create React Liferay",
            options,
            0,
            null
        )
        return when (index) {
            1 -> "shared"
            0 -> "widget"
            else -> null
        }
    }

    private fun runSharedFlow(): GeneratePayload? {
        val projectNameInput = Messages.showInputDialog(
            project,
            "Qual o nome do shared bundle?",
            "Create React Liferay",
            null,
            "novo-shared-bundle",
            null
        ) ?: return null

        val safeProjectName = NameUtils.normalizeDirName(projectNameInput)
        val displayName = NameUtils.formatDisplayName(projectNameInput)
        val selectedTemplate = SelectedTemplate(
            name = "Shared Bundle",
            dir = "templates/shared-bundle",
            hasShared = false
        )

        val baseDir = chooseDestinationDir() ?: return null
        val (finalName, finalDisplayName, targetDir) = resolveDuplicateName(baseDir, safeProjectName, displayName)

        val liferayDir = askLiferayDir(targetDir) ?: return null

        return GeneratePayload(
            type = "shared",
            targetDir = targetDir.toString(),
            safeProjectName = finalName,
            displayName = finalDisplayName,
            liferayDir = liferayDir,
            category = null,
            hasSharedBundle = false,
            sharedBundleDirName = null,
            selectedTemplate = selectedTemplate
        )
    }

    private fun runWidgetFlow(): GeneratePayload? {
        val templates = listOf(
            "Módulo simples" to SelectedTemplate("Simples", "templates/modules/src-simple", false),
            "Módulo simples com template pré-determinado" to SelectedTemplate("Com Template Pré-determinado", "templates/modules/src-simple-with-template", true),
            "Módulo com Shared (básico)" to SelectedTemplate("Com Shared", "templates/modules/src-shared", true),
            "Módulo com Shared CLI" to SelectedTemplate("Com Shared CLI", "templates/modules/src-shared-cli", true),
        )
        val templateLabels = templates.map { it.first }.toTypedArray()
        val selectedIndex = Messages.showDialog(
            project,
            "Qual o template (modo) do módulo?",
            "Create React Liferay",
            templateLabels,
            0,
            null
        )
        if (selectedIndex < 0) return null
        val selectedTemplate = templates[selectedIndex].second

        val projectNameInput = Messages.showInputDialog(
            project,
            "Qual o nome do módulo?",
            "Create React Liferay",
            null,
            "novo-modulo",
            null
        ) ?: return null

        val safeProjectName = NameUtils.normalizeDirName(projectNameInput)
        val displayName = NameUtils.formatDisplayName(projectNameInput)

        val category = Messages.showInputDialog(
            project,
            "Qual a categoria do portlet? (ex: category.simple, category.hidden)",
            "Create React Liferay",
            null,
            "category.simple",
            null
        ) ?: return null

        val sharedBundleDirName =
            if (selectedTemplate.hasShared) {
                Messages.showInputDialog(
                    project,
                    "Qual o nome do diretório do seu shared bundle? (ex: shared-bundle)",
                    "Create React Liferay",
                    null,
                    "",
                    null
                )?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            } else {
                null
            }

        val baseDir = chooseDestinationDir() ?: return null
        val (finalName, finalDisplayName, targetDir) = resolveDuplicateName(baseDir, safeProjectName, displayName)

        val liferayDir = askLiferayDir(targetDir) ?: return null

        return GeneratePayload(
            type = "widget",
            targetDir = targetDir.toString(),
            safeProjectName = finalName,
            displayName = finalDisplayName,
            liferayDir = liferayDir,
            category = category.trim(),
            hasSharedBundle = sharedBundleDirName != null,
            sharedBundleDirName = sharedBundleDirName,
            selectedTemplate = selectedTemplate
        )
    }

    private fun chooseDestinationDir(): Path? {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("Onde você deseja criar o módulo?")
        val guess = project?.basePath
            ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
        val selected = FileChooser.chooseFile(descriptor, project, guess) ?: return null
        return Path.of(selected.path)
    }

    private data class ResolvedName(val finalName: String, val finalDisplayName: String, val targetDir: Path)

    private fun resolveDuplicateName(baseDir: Path, safeProjectName: String, displayName: String): ResolvedName {
        var finalName = safeProjectName
        var finalDisplayName = displayName
        var targetDir = baseDir.resolve(finalName)
        var counter = 0
        while (targetDir.exists()) {
            counter++
            val suffix = if (counter == 1) "-copia" else "-copia-$counter"
            finalName = safeProjectName + suffix
            finalDisplayName = displayName + " Copia" + if (counter > 1) " $counter" else ""
            targetDir = baseDir.resolve(finalName)
        }
        return ResolvedName(finalName, finalDisplayName, targetDir)
    }

    private fun askLiferayDir(targetDir: Path): String? {
        while (true) {
            val value = Messages.showInputDialog(
                project,
                "Qual o caminho (relativo ou absoluto) para o bundle do Liferay (liferayDir)?\nEx: ../../bundles",
                "Create React Liferay",
                null,
                "../../bundles",
                null
            ) ?: return null

            val resolved = try {
                targetDir.resolve(value).normalize().toAbsolutePath()
            } catch (_: Exception) {
                Messages.showErrorDialog(project, "Caminho inválido.", "Create React Liferay")
                continue
            }

            if (!resolved.exists() || !resolved.isDirectory()) {
                Messages.showErrorDialog(
                    project,
                    "A pasta do Liferay não foi encontrada.\n\nDigitado: $value\nResolvido: $resolved",
                    "Create React Liferay"
                )
                continue
            }

            return value.trim()
        }
    }
}

