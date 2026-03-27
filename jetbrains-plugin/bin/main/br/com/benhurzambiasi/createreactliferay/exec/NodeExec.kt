package br.com.benhurzambiasi.createreactliferay.exec

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.util.SystemInfoRt
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

object NodeExec {
    fun resolveNodeCommand(configuredPath: String): String = configuredPath.trim().ifBlank { "node" }

    fun isValidNodePath(path: String): Boolean {
        val p = path.trim()
        if (p.isEmpty()) return false
        return try {
            Path.of(p).exists()
        } catch (_: Exception) {
            false
        }
    }

    fun runVersion(nodePath: String, timeoutMs: Int = 10_000): ProcessOutput {
        val exe = resolveNodeCommand(nodePath)
        val cmd = GeneralCommandLine(exe, "--version")
            .withCharset(StandardCharsets.UTF_8)
        if (SystemInfoRt.isWindows) {
            cmd.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        }
        return CapturingProcessHandler(cmd).runProcess(timeoutMs)
    }
}

