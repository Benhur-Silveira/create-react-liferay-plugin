package br.com.benhurzambiasi.createreactliferay.model

data class SelectedTemplate(
    val name: String,
    val dir: String,
    val hasShared: Boolean,
)

data class GeneratePayload(
    val type: String, // widget | shared
    val targetDir: String,
    val safeProjectName: String,
    val displayName: String,
    val liferayDir: String,
    val category: String?,
    val hasSharedBundle: Boolean,
    val sharedBundleDirName: String?,
    val selectedTemplate: SelectedTemplate,
)

