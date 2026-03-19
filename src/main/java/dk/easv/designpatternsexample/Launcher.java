package dk.easv.designpatternsexample;

import dk.easv.designpatternsexample.ui.MainApp;
import javafx.application.Application;

/**
 * Entry point for the Design Patterns demo application.
 *
 * A separate Launcher class (not extending Application) is needed so that
 * the JVM can resolve JavaFX classes on the module path before starting.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
