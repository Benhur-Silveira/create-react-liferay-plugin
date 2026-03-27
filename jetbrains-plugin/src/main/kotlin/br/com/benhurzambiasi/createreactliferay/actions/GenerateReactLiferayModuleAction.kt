package br.com.benhurzambiasi.createreactliferay.actions

import br.com.benhurzambiasi.createreactliferay.exec.GenerateExecutor
import br.com.benhurzambiasi.createreactliferay.ui.GenerateFlow
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware

class GenerateReactLiferayModuleAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val payload = GenerateFlow(project).run() ?: return
        GenerateExecutor.run(project, payload)
    }
}

