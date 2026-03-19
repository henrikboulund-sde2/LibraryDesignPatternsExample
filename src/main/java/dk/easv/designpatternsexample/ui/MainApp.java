package dk.easv.designpatternsexample.ui;

import dk.easv.designpatternsexample.bll.BookService;
import dk.easv.designpatternsexample.bll.iterator.IBookIterator;
import dk.easv.designpatternsexample.dal.Book;
import dk.easv.designpatternsexample.dal.BookRepository;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Design Patterns demo application.
 * Belongs to the UI layer.
 *
 * The UI communicates exclusively with BookService (BLL).
 * It never touches the DAL directly — that is what the three-layer architecture enforces.
 *
 * The window has three tabs, one for each design pattern:
 *   Tab 1 — Repository Pattern : direct CRUD through IBookRepository
 *   Tab 2 — Command Pattern    : operations wrapped in ICommand objects (with undo)
 *   Tab 3 — Iterator Pattern   : sequential traversal of books filtered by genre
 */
public class MainApp extends Application {

    // The single service instance shared across all tabs.
    private final BookService service = new BookService(new BookRepository());

    // Shared observable list — all TableViews across tabs are backed by this list.
    // Call refreshBooks() after any repository change to keep the UI in sync.
    private final ObservableList<Book> sharedBookList = FXCollections.observableArrayList();

    // -------------------------------------------------------------------------
    // Application start
    // -------------------------------------------------------------------------

    @Override
    public void start(Stage stage) {
        refreshBooks();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                buildRepositoryTab(),
                buildCommandTab(),
                buildIteratorTab()
        );

        VBox root = new VBox();
        root.getChildren().addAll(buildHeader(), tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Scene scene = new Scene(root, 960, 700);
        stage.setTitle("Design Patterns — Book Library");
        stage.setScene(scene);
        stage.show();
    }

    // -------------------------------------------------------------------------
    // Header
    // -------------------------------------------------------------------------

    private HBox buildHeader() {
        Label title = new Label("Design Patterns Example — Book Library");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label subtitle = new Label("Repository  ·  Command  ·  Iterator");
        subtitle.setFont(Font.font("System", 13));
        subtitle.setTextFill(Color.GRAY);

        VBox text = new VBox(2, title, subtitle);
        HBox header = new HBox(text);
        header.setPadding(new Insets(14, 16, 10, 16));
        header.setStyle("-fx-background-color: #f0f4f8; -fx-border-color: #d0d8e0; -fx-border-width: 0 0 1 0;");
        return header;
    }

    // =========================================================================
    // TAB 1 — Repository Pattern
    // =========================================================================

    private Tab buildRepositoryTab() {
        Tab tab = new Tab("Repository Pattern");

        // --- Explanation ---
        Label explanation = infoLabel(
                "IBookRepository defines the contract for data access. " +
                "BookRepository is the concrete in-memory implementation. " +
                "The UI only ever talks to the interface — the storage mechanism is hidden."
        );

        // --- Form: add a book ---
        TextField titleField  = textField("Title");
        TextField authorField = textField("Author");
        ComboBox<String> genreBox = genreComboBox();
        TextField yearField   = textField("Year");

        Button addBtn = new Button("Add to Repository");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addBtn.setOnAction(e -> {
            if (validateForm(titleField, authorField, genreBox, yearField)) {
                service.addBookDirect(
                        titleField.getText().trim(),
                        authorField.getText().trim(),
                        genreBox.getValue(),
                        Integer.parseInt(yearField.getText().trim())
                );
                clearForm(titleField, authorField, genreBox, yearField);
                refreshBooks();
            }
        });

        HBox form = new HBox(8, titleField, authorField, genreBox, yearField, addBtn);
        form.setAlignment(Pos.CENTER_LEFT);

        // --- Table: all books ---
        TableView<Book> table = buildBookTable();
        table.setItems(sharedBookList);

        // --- Remove selected ---
        Button removeBtn = new Button("Remove Selected");
        removeBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.removeBookDirect(selected.getId());
                refreshBooks();
            }
        });

        VBox layout = new VBox(12,
                sectionTitle("Repository Pattern"),
                explanation,
                new Separator(),
                new Label("Add a new book:"),
                form,
                removeBtn,
                new Separator(),
                new Label("All books in the repository:"),
                table
        );
        layout.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);

        tab.setContent(layout);
        return tab;
    }

    // =========================================================================
    // TAB 2 — Command Pattern
    // =========================================================================

    // These fields are instance-level so the history list can be refreshed
    // from inside the undo button handler.
    private ListView<String> commandHistoryView;
    private Label undoStatusLabel;

    private Tab buildCommandTab() {
        Tab tab = new Tab("Command Pattern");

        Label explanation = infoLabel(
                "Each operation (add / remove) is wrapped in an ICommand object with execute() and undo(). " +
                "The CommandManager stores executed commands on a stack. " +
                "Pressing 'Undo' pops the last command and calls its undo() method."
        );

        // --- Form: add via command ---
        TextField titleField  = textField("Title");
        TextField authorField = textField("Author");
        ComboBox<String> genreBox = genreComboBox();
        TextField yearField   = textField("Year");

        Button addCmdBtn = new Button("Execute: AddBookCommand");
        addCmdBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");
        addCmdBtn.setOnAction(e -> {
            if (validateForm(titleField, authorField, genreBox, yearField)) {
                service.addBookWithCommand(
                        titleField.getText().trim(),
                        authorField.getText().trim(),
                        genreBox.getValue(),
                        Integer.parseInt(yearField.getText().trim())
                );
                clearForm(titleField, authorField, genreBox, yearField);
                refreshBooks();
                refreshCommandHistory();
            }
        });

        HBox addForm = new HBox(8, titleField, authorField, genreBox, yearField, addCmdBtn);
        addForm.setAlignment(Pos.CENTER_LEFT);

        // --- Table: books (shared list, same data as tab 1) ---
        TableView<Book> table = buildBookTable();
        table.setItems(sharedBookList);

        // --- Remove via command ---
        Button removeCmdBtn = new Button("Execute: RemoveBookCommand");
        removeCmdBtn.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white;");
        removeCmdBtn.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.removeBookWithCommand(selected.getId());
                refreshBooks();
                refreshCommandHistory();
            }
        });

        // --- Undo ---
        Button undoBtn = new Button("⟵  Undo Last Command");
        undoStatusLabel = new Label("No commands in history.");
        undoStatusLabel.setTextFill(Color.GRAY);

        undoBtn.setOnAction(e -> {
            service.undo();
            refreshBooks();
            refreshCommandHistory();
        });

        HBox undoRow = new HBox(12, undoBtn, undoStatusLabel);
        undoRow.setAlignment(Pos.CENTER_LEFT);

        // --- Command history list ---
        commandHistoryView = new ListView<>();
        commandHistoryView.setPlaceholder(new Label("No commands executed yet."));
        commandHistoryView.setPrefHeight(180);

        VBox leftPane = new VBox(12,
                sectionTitle("Command Pattern"),
                explanation,
                new Separator(),
                new Label("Add a book via command:"),
                addForm,
                removeCmdBtn,
                undoRow,
                new Separator(),
                new Label("Books in repository:"),
                table
        );
        leftPane.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox rightPane = new VBox(10,
                sectionTitle("Command History"),
                new Label("Most recent command is at the top."),
                commandHistoryView
        );
        rightPane.setPadding(new Insets(16));
        VBox.setVgrow(commandHistoryView, Priority.ALWAYS);

        SplitPane split = new SplitPane(leftPane, rightPane);
        split.setDividerPositions(0.65);

        tab.setContent(split);
        return tab;
    }

    // =========================================================================
    // TAB 3 — Iterator Pattern
    // =========================================================================

    private Tab buildIteratorTab() {
        Tab tab = new Tab("Iterator Pattern");

        Label explanation = infoLabel(
                "IBookIterator provides hasNext(), next(), and reset() — a standard way to walk through " +
                "a collection without knowing the underlying data structure. " +
                "GenreBookIterator filters books by genre and holds an immutable snapshot."
        );

        // --- Genre selector ---
        ComboBox<String> genreBox = new ComboBox<>();
        genreBox.setPromptText("Select genre");
        genreBox.setPrefWidth(160);

        Button createIterBtn = new Button("Create Iterator");
        createIterBtn.setStyle("-fx-background-color: #6A1B9A; -fx-text-fill: white;");

        HBox genreRow = new HBox(10, new Label("Genre:"), genreBox, createIterBtn);
        genreRow.setAlignment(Pos.CENTER_LEFT);

        // --- Status label ---
        Label statusLabel = new Label("Select a genre and press 'Create Iterator'.");
        statusLabel.setTextFill(Color.SLATEGRAY);

        // --- Navigation ---
        Button nextBtn    = new Button("Next Book  →");
        Button resetBtn   = new Button("⟵  Reset");
        nextBtn.setDisable(true);
        resetBtn.setDisable(true);
        nextBtn.setStyle("-fx-background-color: #6A1B9A; -fx-text-fill: white;");

        HBox navRow = new HBox(10, resetBtn, nextBtn);
        navRow.setAlignment(Pos.CENTER_LEFT);

        // --- Book card ---
        Label cardTitle  = new Label();
        Label cardAuthor = new Label();
        Label cardGenre  = new Label();
        Label cardYear   = new Label();
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 15));

        VBox bookCard = new VBox(6,
                new Label("Currently viewing:"),
                cardTitle, cardAuthor, cardGenre, cardYear
        );
        bookCard.setPadding(new Insets(12));
        bookCard.setStyle(
                "-fx-background-color: #f3e5f5; -fx-border-color: #ce93d8; " +
                "-fx-border-radius: 6; -fx-background-radius: 6;"
        );
        bookCard.setVisible(false);

        // --- Progress ---
        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(300);
        Label progressLabel = new Label("0 / 0");

        HBox progressRow = new HBox(10, progress, progressLabel);
        progressRow.setAlignment(Pos.CENTER_LEFT);

        // --- Wire up the iterator state ---
        // The iterator is kept in a one-element array so lambdas can capture and replace it.
        IBookIterator[] iteratorHolder = { null };

        // Populate the genre combo whenever this tab is shown.
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                String current = genreBox.getValue();
                genreBox.setItems(FXCollections.observableArrayList(service.getAvailableGenres()));
                if (current != null) genreBox.setValue(current);
            }
        });

        createIterBtn.setOnAction(e -> {
            String genre = genreBox.getValue();
            if (genre == null || genre.isBlank()) {
                statusLabel.setText("Please select a genre first.");
                return;
            }
            IBookIterator iter = service.createGenreIterator(genre);
            iteratorHolder[0] = iter;

            int count = iter.getCount();
            statusLabel.setText("Iterator created for genre \"" + genre + "\": " + count + " book(s) found.");
            nextBtn.setDisable(count == 0);
            resetBtn.setDisable(count == 0);
            bookCard.setVisible(false);
            progress.setProgress(0);
            progressLabel.setText("0 / " + count);
        });

        nextBtn.setOnAction(e -> {
            IBookIterator iter = iteratorHolder[0];
            if (iter == null || !iter.hasNext()) return;

            Book book = iter.next();
            cardTitle.setText(book.getTitle());
            cardAuthor.setText("Author: " + book.getAuthor());
            cardGenre.setText("Genre:  " + book.getGenre());
            cardYear.setText("Year:   " + book.getYear());
            bookCard.setVisible(true);

            int total = iter.getCount();
            int visited = iter.getCurrentIndex();   // index already advanced by next()
            progress.setProgress((double) visited / total);
            progressLabel.setText(visited + " / " + total);

            nextBtn.setDisable(!iter.hasNext());
            if (!iter.hasNext()) {
                statusLabel.setText("End of collection — press Reset to start over.");
            }
        });

        resetBtn.setOnAction(e -> {
            IBookIterator iter = iteratorHolder[0];
            if (iter == null) return;
            iter.reset();
            bookCard.setVisible(false);
            progress.setProgress(0);
            progressLabel.setText("0 / " + iter.getCount());
            nextBtn.setDisable(false);
            statusLabel.setText("Iterator reset — ready to iterate again.");
        });

        VBox layout = new VBox(12,
                sectionTitle("Iterator Pattern"),
                explanation,
                new Separator(),
                genreRow,
                statusLabel,
                new Separator(),
                navRow,
                bookCard,
                progressRow
        );
        layout.setPadding(new Insets(16));

        tab.setContent(layout);
        return tab;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Reload the observable list from the repository. */
    private void refreshBooks() {
        sharedBookList.setAll(service.getAllBooks());
    }

    /** Reload the command history list and update the undo status label. */
    private void refreshCommandHistory() {
        commandHistoryView.setItems(FXCollections.observableArrayList(service.getCommandHistory()));
        int size = service.commandHistorySize();
        if (size == 0) {
            undoStatusLabel.setText("No commands in history.");
        } else {
            undoStatusLabel.setText(size + " command(s) in history — undo is available.");
        }
    }

    /** Build a TableView with columns for all Book fields. */
    @SuppressWarnings("unchecked")
    private TableView<Book> buildBookTable() {
        TableColumn<Book, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(45);

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(220);

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(160);

        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
        genreCol.setPrefWidth(100);

        TableColumn<Book, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearCol.setPrefWidth(60);

        TableView<Book> table = new TableView<>();
        table.getColumns().addAll(idCol, titleCol, authorCol, genreCol, yearCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        return table;
    }

    /** Creates a text field with a prompt. */
    private TextField textField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        if (prompt.equals("Year")) {
            tf.setPrefWidth(70);
        } else {
            tf.setPrefWidth(150);
        }
        return tf;
    }

    /** Creates a ComboBox pre-loaded with common genres. */
    private ComboBox<String> genreComboBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().addAll("Fantasy", "Sci-Fi", "Mystery", "Romance", "Thriller", "History", "Biography");
        box.setPromptText("Genre");
        box.setPrefWidth(110);
        return box;
    }

    /**
     * Returns true if all form fields are filled in and the year is a valid integer.
     * Shows an Alert with details if validation fails.
     */
    private boolean validateForm(TextField title, TextField author,
                                 ComboBox<String> genre, TextField year) {
        StringBuilder errors = new StringBuilder();
        if (title.getText().isBlank())   errors.append("• Title is required\n");
        if (author.getText().isBlank())  errors.append("• Author is required\n");
        if (genre.getValue() == null)    errors.append("• Genre must be selected\n");
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

    /** Clears all fields in an add-book form. */
    private void clearForm(TextField title, TextField author,
                           ComboBox<String> genre, TextField year) {
        title.clear();
        author.clear();
        genre.setValue(null);
        year.clear();
    }

    /** Creates a bold section title label. */
    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 15));
        return label;
    }

    /** Creates a muted, italic information/explanation label. */
    private Label infoLabel(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setTextFill(Color.SLATEGRAY);
        label.setStyle("-fx-font-style: italic;");
        return label;
    }
}
