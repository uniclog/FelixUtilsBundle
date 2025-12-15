package io.github.uniclog.docker.runner;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.*;


public class GenerateTextFromTextInput {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ChatGPT Swing Browser");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);

            //BrowserPanel browserPanel = new BrowserPanel();
            //browserPanel.navigate("https://chat.openai.com/");

            //frame.add(browserPanel, BorderLayout.CENTER);
            //frame.setVisible(true);
        });
    }
}
