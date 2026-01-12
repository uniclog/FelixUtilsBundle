package io.github.uniclog.docker.runner.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import io.github.uniclog.docker.runner.model.AnkeyPath
import java.io.File

object AnkeyPathService {

    fun isValid(path: AnkeyPath): Boolean {
        val file = File(path.absolutePath)
        return file.exists() && file.isDirectory
    }

    /**
     * Find ankey path by dir name "ankey"
     */
    fun findAnkeyPaths(project: Project?): Set<AnkeyPath> {
        val result = mutableSetOf<AnkeyPath>()
        if (project == null)
            return result

        val projectBasePath = project.basePath ?: return emptySet<AnkeyPath>()
        val root = LocalFileSystem.getInstance().refreshAndFindFileByPath(projectBasePath)
            ?: return emptySet<AnkeyPath>()
        VfsUtilCore.iterateChildrenRecursively(root, null) { file ->
            if (file.isDirectory && file.name == "ankey") {
                result.add(AnkeyPath(file.path, projectBasePath))
            }
            true
        }

        return result
    }

    fun deleteFile(file: AnkeyPath) {
        val compose = File(file.getComposePath())
        if (compose.exists()) {
            ApplicationManager.getApplication().runWriteAction {
                compose.delete()
            }
        }
    }
}