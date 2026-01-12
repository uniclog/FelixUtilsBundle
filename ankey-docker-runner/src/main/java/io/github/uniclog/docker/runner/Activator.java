package io.github.uniclog.docker.runner;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;

public class Activator implements AppLifecycleListener {
    @Override
    public void appStarted() {
        System.out.println(">>> Docker Runner started");
        var manager = ApplicationManager.getApplication();
        manager.invokeLater(() -> Messages.showMessageDialog(
                "Docker Runner  started!",
                "Docker Runner",
                Messages.getInformationIcon()
        ));
    }
}
