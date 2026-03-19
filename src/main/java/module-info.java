module dk.easv.designpatternsexample {
    requires javafx.controls;
    requires javafx.fxml;

    // Open the UI package to javafx.fxml (FXML loader uses reflection).
    // Open the DAL package to javafx.base so TableView's PropertyValueFactory
    // can access Book's getter methods via reflection at runtime.
    opens dk.easv.designpatternsexample to javafx.fxml;
    opens dk.easv.designpatternsexample.ui to javafx.fxml;
    opens dk.easv.designpatternsexample.dal to javafx.base;

    exports dk.easv.designpatternsexample;
    exports dk.easv.designpatternsexample.ui;
    exports dk.easv.designpatternsexample.bll;
    exports dk.easv.designpatternsexample.dal;
}
