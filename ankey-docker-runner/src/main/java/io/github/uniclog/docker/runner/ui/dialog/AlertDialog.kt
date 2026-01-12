package io.github.uniclog.docker.runner.ui.dialog

import com.intellij.openapi.ui.Messages

object AlertDialog {
    fun showErrorDialog(title: String, comment: String) {
        Messages.showErrorDialog(comment, title)
    }
}