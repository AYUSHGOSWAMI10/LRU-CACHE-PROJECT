package com.lru.project;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class AppMain {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("This application needs a graphical desktop to show its interface.");
            return;
        }
        SwingUtilities.invokeLater(() -> new DesktopApp().show());
    }
}
