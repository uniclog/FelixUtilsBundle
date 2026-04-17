package io.github.uniclog.felixutils.service

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import io.github.uniclog.felixutils.model.UploadContext
import io.github.uniclog.felixutils.model.UploadTargetType

class UploadContextResolver {
    fun resolve(event: AnActionEvent): UploadContext? {
        val file = selectedFile(event) ?: return null
        val targetType = UploadTargetType.fromFile(file) ?: return null
        return UploadContext(file, targetType)
    }

    private fun selectedFile(event: AnActionEvent): VirtualFile? {
        event.getData(CommonDataKeys.VIRTUAL_FILE)?.let { return it }
        event.getData(CommonDataKeys.PSI_FILE)?.virtualFile?.let { return it }

        val psiItem = event.getData(CommonDataKeys.PSI_ELEMENT) as? PsiFileSystemItem
        psiItem?.virtualFile?.let { return it }

        val files = event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
            ?.filterNot { it.isDirectory }
            .orEmpty()

        return files.singleOrNull()
    }
}
