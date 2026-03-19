package dk.easv.designpatternsexample.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX Application entry point.
 * Belongs to the UI layer.
 *
 * Its only responsibility is to load the FXML layout and open the window.
 * All UI logic and @FXML bindings live in MainController.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/dk/easv/designpatternsexample/hello-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 1260, 700);
        stage.setTitle("Design Patterns — Book Library");
        stage.setScene(scene);
        stage.show();
    }
}
