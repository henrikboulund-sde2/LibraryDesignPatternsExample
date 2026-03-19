package dk.easv.designpatternsexample.ui;

import dk.easv.designpatternsexample.bll.BookService;
import dk.easv.designpatternsexample.bll.iterator.IBookIterator;
import dk.easv.designpatternsexample.dal.Book;
import dk.easv.designpatternsexample.dal.BookRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

/**
 * FXML controller for hello-view.fxml.
 * Belongs to the UI layer.
 *
 * The FXMLLoader instantiates this class automatically when it loads the FXML file.
 * Fields annotated with @FXML are injected by the loader based on the fx:id attributes
 * in the FXML. Methods annotated with @FXML are called by onAction="#methodName" bindings.
 *
 * The controller communicates exclusively with BookService (BLL).
 * It never touches the DAL directly.
 */
public class MainController {

    // -------------------------------------------------------------------------
    // Service (BLL entry point) and shared data
    // -------------------------------------------------------------------------

    private final BookService service = new BookService(new BookRepository());

    // A single observable list shared by both TableViews (repo tab and command tab).
    // refreshBooks() repopulates it from the repository.
    private final ObservableList<Book> sharedBookList = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // TAB 1 — Repository Pattern : @FXML fields
    // -------------------------------------------------------------------------

    @FXML private TextField          repoTitleField;
    @FXML private TextField          repoAuthorField;
    @FXML private ComboBox<String>   repoGenreBox;
    @FXML private TextField          repoYearField;
    @FXML private TableView<Book>    repoTable;
    @FXML private TableColumn<Book, Integer> repoIdCol;
    @FXML private TableColumn<Book, String>  repoTitleCol;
    @FXML private TableColumn<Book, String>  repoAuthorCol;
    @FXML private TableColumn<Book, String>  repoGenreCol;
    @FXML private TableColumn<Book, Integer> repoYearCol;

    // -------------------------------------------------------------------------
    // TAB 2 — Command Pattern : @FXML fields
    // -------------------------------------------------------------------------

    @FXML private TextField          cmdTitleField;
    @FXML private TextField          cmdAuthorField;
    @FXML private ComboBox<String>   cmdGenreBox;
    @FXML private TextField          cmdYearField;
    @FXML private TableView<Book>    cmdTable;
    @FXML private TableColumn<Book, Integer> cmdIdCol;
    @FXML private TableColumn<Book, String>  cmdTitleCol;
    @FXML private TableColumn<Book, String>  cmdAuthorCol;
    @FXML private TableColumn<Book, String>  cmdGenreCol;
    @FXML private TableColumn<Book, Integer> cmdYearCol;
    @FXML private ListView<String>   commandHistoryView;
    @FXML private Label              undoStatusLabel;

    // -------------------------------------------------------------------------
    // TAB 3 — Iterator Pattern : @FXML fields
    // -------------------------------------------------------------------------

    @FXML private ComboBox<String> iterGenreBox;
    @FXML private Label            iterStatusLabel;
    @FXML private Button           nextBtn;
    @FXML private Button           resetIterBtn;
    @FXML private VBox             bookCard;
    @FXML private Label            cardTitle;
    @FXML private Label            cardAuthor;
    @FXML private Label            cardGenre;
    @FXML private Label            cardYear;
    @FXML private ProgressBar      progressBar;
    @FXML private Label            progressLabel;

    // The active iterator — replaced each time "Create Iterator" is pressed.
    private IBookIterator currentIterator;

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    /**
     * Called by the FXMLLoader after all @FXML fields have been injected.
     * Use this method for any setup that requires the injected controls to exist.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        populateStaticGenreBoxes();
        commandHistoryView.setPlaceholder(new Label("No commands executed yet."));
        refreshBooks();
    }

    /** Wire up CellValueFactory for every TableColumn in both tabs. */
    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
        // Repository tab columns
        repoIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        repoTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        repoAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        repoGenreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        repoYearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        repoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        repoTable.setItems(sharedBookList);

        // Command tab columns
        cmdIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        cmdTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        cmdAuthorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        cmdGenreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        cmdYearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        cmdTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        cmdTable.setItems(sharedBookList);
    }

    /** Pre-fill the static genre ComboBoxes in the add-book forms. */
    private void populateStaticGenreBoxes() {
        var genres = FXCollections.observableArrayList(
                "Fantasy", "Sci-Fi", "Mystery", "Romance", "Thriller", "History", "Biography"
        );
        repoGenreBox.setItems(genres);
        cmdGenreBox.setItems(FXCollections.observableArrayList(genres));
    }

    // =========================================================================
    // TAB 1 — Repository Pattern : event handlers
    // =========================================================================

    @FXML
    private void onRepoAdd() {
        if (!validateForm(repoTitleField, repoAuthorField, repoGenreBox, repoYearField)) return;

        service.addBookDirect(
                repoTitleField.getText().trim(),
                repoAuthorField.getText().trim(),
                repoGenreBox.getValue(),
                Integer.parseInt(repoYearField.getText().trim())
        );
        clearForm(repoTitleField, repoAuthorField, repoGenreBox, repoYearField);
        refreshBooks();
    }

    @FXML
    private void onRepoRemove() {
        Book selected = repoTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.removeBookDirect(selected.getId());
            refreshBooks();
        }
    }

    // =========================================================================
    // TAB 2 — Command Pattern : event handlers
    // =========================================================================

    @FXML
    private void onCmdAdd() {
        if (!validateForm(cmdTitleField, cmdAuthorField, cmdGenreBox, cmdYearField)) return;

        service.addBookWithCommand(
                cmdTitleField.getText().trim(),
                cmdAuthorField.getText().trim(),
                cmdGenreBox.getValue(),
                Integer.parseInt(cmdYearField.getText().trim())
        );
        clearForm(cmdTitleField, cmdAuthorField, cmdGenreBox, cmdYearField);
        refreshBooks();
        refreshCommandHistory();
    }

    @FXML
    private void onCmdRemove() {
        Book selected = cmdTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            service.removeBookWithCommand(selected.getId());
            refreshBooks();
            refreshCommandHistory();
        }
    }

    @FXML
    private void onUndo() {
        service.undo();
        refreshBooks();
        refreshCommandHistory();
    }

    // =========================================================================
    // TAB 3 — Iterator Pattern : event handlers
    // =========================================================================

    /**
     * Fired when the Iterator tab is selected or deselected.
     * Refreshes the genre ComboBox so it always reflects the current repository contents.
     */
    @FXML
    private void onIteratorTabSelected(Event event) {
        Tab tab = (Tab) event.getSource();
        if (!tab.isSelected()) return;

        String current = iterGenreBox.getValue();
        iterGenreBox.setItems(FXCollections.observableArrayList(service.getAvailableGenres()));
        // Preserve the previously selected genre if it still exists.
        if (current != null) iterGenreBox.setValue(current);
    }

    @FXML
    private void onCreateIterator() {
        String genre = iterGenreBox.getValue();
        if (genre == null || genre.isBlank()) {
            iterStatusLabel.setText("Please select a genre first.");
            return;
        }

        currentIterator = service.createGenreIterator(genre);
        int count = currentIterator.getCount();

        iterStatusLabel.setText("Iterator created for genre \"" + genre + "\": " + count + " book(s) found.");
        nextBtn.setDisable(count == 0);
        resetIterBtn.setDisable(count == 0);

        // Hide the book card and reset progress display.
        bookCard.setVisible(false);
        bookCard.setManaged(false);
        progressBar.setProgress(0);
        progressLabel.setText("0 / " + count);
    }

    @FXML
    private void onIterNext() {
        if (currentIterator == null || !currentIterator.hasNext()) return;

        Book book = currentIterator.next();
        cardTitle.setText(book.getTitle());
        cardAuthor.setText("Author: " + book.getAuthor());
        cardGenre.setText("Genre:  " + book.getGenre());
        cardYear.setText("Year:   " + book.getYear());
        bookCard.setVisible(true);
        bookCard.setManaged(true);

        int total   = currentIterator.getCount();
        int visited = currentIterator.getCurrentIndex(); // already advanced by next()
        progressBar.setProgress((double) visited / total);
        progressLabel.setText(visited + " / " + total);

        nextBtn.setDisable(!currentIterator.hasNext());
        if (!currentIterator.hasNext()) {
            iterStatusLabel.setText("End of collection — press Reset to start over.");
        }
    }

    @FXML
    private void onIterReset() {
        if (currentIterator == null) return;
        currentIterator.reset();

        bookCard.setVisible(false);
        bookCard.setManaged(false);
        progressBar.setProgress(0);
        progressLabel.setText("0 / " + currentIterator.getCount());
        nextBtn.setDisable(false);
        iterStatusLabel.setText("Iterator reset — ready to iterate again.");
    }

    // =========================================================================
    // Shared helpers
    // =========================================================================

    /** Reload the shared observable list from the repository. */
    private void refreshBooks() {
        sharedBookList.setAll(service.getAllBooks());
    }

    /** Reload the command history list and update the undo status label. */
    private void refreshCommandHistory() {
        commandHistoryView.setItems(
                FXCollections.observableArrayList(service.getCommandHistory())
        );
        int size = service.commandHistorySize();
        undoStatusLabel.setText(size == 0
                ? "No commands in history."
                : size + " command(s) in history — undo is available."
        );
    }

    /**
     * Validates all fields in an add-book form.
     * Shows an Alert describing any problems and returns false if validation fails.
     */
    private boolean validateForm(TextField title, TextField author,
                                 ComboBox<String> genre, TextField year) {
        StringBuilder errors = new StringBuilder();
        if (title.getText().isBlank())  errors.append("• Title is required\n");
        if (author.getText().isBlank()) errors.append("• Author is required\n");
        if (genre.getValue() == null)   errors.append("• Genre must be selected\n");
        try {
            if (!year.getText().isBlank()) Integer.parseInt(year.getText().trim());
            else errors.append("• Year is required\n");
        } catch (NumberFormatException ex) {
            errors.append("• Year must be a number\n");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /** Clears all fields in an add-book form after a successful add. */
    private void clearForm(TextField title, TextField author,
                           ComboBox<String> genre, TextField year) {
        title.clear();
        author.clear();
        genre.setValue(null);
        year.clear();
    }
}
