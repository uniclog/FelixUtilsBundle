package io.github.uniclog.felixutils.model

import com.intellij.openapi.vfs.VirtualFile

enum class UploadTargetType {
    JSON,
    BUNDLE;

    companion object {
        fun fromFile(file: VirtualFile): UploadTargetType? {
            return when (file.extension?.lowercase()) {
                "json" -> JSON
                "jar" -> BUNDLE
                else -> null
            }
        }
    }
}
