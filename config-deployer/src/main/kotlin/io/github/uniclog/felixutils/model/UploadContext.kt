package io.github.uniclog.felixutils.model

import com.intellij.openapi.vfs.VirtualFile

data class UploadContext(
    val file: VirtualFile,
    val targetType: UploadTargetType
)
