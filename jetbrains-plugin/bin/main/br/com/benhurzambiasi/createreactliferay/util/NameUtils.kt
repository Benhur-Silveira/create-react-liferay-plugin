package br.com.benhurzambiasi.createreactliferay.util

import java.text.Normalizer

object NameUtils {
    fun normalizeDirName(input: String): String {
        val base = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
        val cleaned = base
            .replace("[^a-z0-9\\-\\s_]".toRegex(), "")
            .replace("[\\s_]+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
            .trim('-')
        return if (cleaned.isEmpty()) "novo-modulo" else cleaned
    }

    fun formatDisplayName(input: String): String {
        return input.trim()
            .replace("[\\s_\\-]+".toRegex(), " ")
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { w ->
                w.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            .ifBlank { "Novo Modulo" }
    }
}

