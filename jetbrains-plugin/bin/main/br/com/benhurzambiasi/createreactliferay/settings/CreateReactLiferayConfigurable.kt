package br.com.benhurzambiasi.createreactliferay.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.NlsContexts
import br.com.benhurzambiasi.createreactliferay.exec.NodeExec
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class CreateReactLiferayConfigurable : Configurable {
    private var panel: JPanel? = null
    private var nodePathField: JTextField? = null

    override fun getDisplayName(): @NlsContexts.ConfigurableName String = "Create React Liferay"

    override fun createComponent(): JComponent {
        val root = JPanel(BorderLayout(8, 8))

        val form = JPanel(BorderLayout(8, 8))
        form.add(JLabel("Node path (opcional):"), BorderLayout.WEST)

        val nodeField = JTextField()
        nodePathField = nodeField

        val buttons = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        val testBtn = JButton("Test Node")
        testBtn.addActionListener {
            val nodePath = nodeField.text.trim()
            if (nodePath.isNotEmpty() && !NodeExec.isValidNodePath(nodePath)) {
                Messages.showErrorDialog(root, "Caminho inválido: arquivo não encontrado.\n\n$nodePath", "Create React Liferay")
                return@addActionListener
            }
            val out = NodeExec.runVersion(nodePath)
            if (out.exitCode == 0) {
                val stdout = out.stdout.trim().ifEmpty { "(sem saída)" }
                val mode = if (nodePath.isBlank()) "PATH" else "custom"
                Messages.showInfoMessage(root, "OK ($mode): $stdout", "Create React Liferay")
            } else {
                val stderr = out.stderr.trim().ifEmpty { "(sem stderr)" }
                Messages.showErrorDialog(root, "Falhou ao executar Node.\n\n$stderr", "Create React Liferay")
            }
        }
        buttons.add(testBtn)

        val center = JPanel(BorderLayout(8, 8))
        center.add(nodeField, BorderLayout.CENTER)
        center.add(buttons, BorderLayout.EAST)

        form.add(center, BorderLayout.CENTER)

        root.add(form, BorderLayout.NORTH)
        panel = root
        reset()
        return root
    }

    override fun isModified(): Boolean {
        val settings = CreateReactLiferaySettings.getInstance()
        return nodePathField?.text?.trim() != settings.nodePath
    }

    override fun apply() {
        val settings = CreateReactLiferaySettings.getInstance()
        settings.nodePath = nodePathField?.text?.trim().orEmpty()
    }

    override fun reset() {
        val settings = CreateReactLiferaySettings.getInstance()
        nodePathField?.text = settings.nodePath
    }

    override fun disposeUIResources() {
        panel = null
        nodePathField = null
    }
}

