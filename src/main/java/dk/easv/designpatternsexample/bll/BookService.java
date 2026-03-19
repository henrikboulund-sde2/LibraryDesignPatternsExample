package dk.easv.designpatternsexample.bll;

import dk.easv.designpatternsexample.bll.command.AddBookCommand;
import dk.easv.designpatternsexample.bll.command.CommandManager;
import dk.easv.designpatternsexample.bll.command.RemoveBookCommand;
import dk.easv.designpatternsexample.bll.iterator.GenreBookIterator;
import dk.easv.designpatternsexample.bll.iterator.IBookIterator;
import dk.easv.designpatternsexample.dal.Book;
import dk.easv.designpatternsexample.dal.IBookRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service class in the Business Logic Layer (BLL).
 *
 * Acts as the bridge between the UI and the DAL.
 * Exposes two ways to add/remove books:
 *   1. Direct repository operations  (Repository Pattern demo)
 *   2. Command-wrapped operations     (Command Pattern demo)
 * And one way to iterate:
 *   3. Genre-filtered iterator        (Iterator Pattern demo)
 */
public class BookService {

    private final IBookRepository repository;
    private final CommandManager commandManager;

    // Auto-increments to give each new book a unique ID.
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public BookService(IBookRepository repository) {
        this.repository = repository;
        this.commandManager = new CommandManager();
        seedData();
    }

    // -------------------------------------------------------------------------
    // Repository Pattern — direct CRUD through IBookRepository
    // -------------------------------------------------------------------------

    /**
     * Adds a book directly via the repository interface.
     * No command object is created — this is the plain repository approach.
     */
    public void addBookDirect(String title, String author, String genre, int year) {
        Book book = new Book(idCounter.getAndIncrement(), title, author, genre, year);
        repository.add(book);
    }

    /**
     * Removes a book directly via the repository interface.
     */
    public void removeBookDirect(int id) {
        repository.remove(id);
    }

    /** Returns all books currently in the repository. */
    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    // -------------------------------------------------------------------------
    // Command Pattern — operations wrapped in ICommand objects
    // -------------------------------------------------------------------------

    /**
     * Creates an AddBookCommand and hands it to the CommandManager for execution.
     * The command is stored in history so it can later be undone.
     */
    public void addBookWithCommand(String title, String author, String genre, int year) {
        Book book = new Book(idCounter.getAndIncrement(), title, author, genre, year);
        commandManager.execute(new AddBookCommand(repository, book));
    }

    /**
     * Creates a RemoveBookCommand for the given book ID and executes it via the CommandManager.
     * Does nothing if the ID is not found.
     */
    public void removeBookWithCommand(int id) {
        repository.findById(id).ifPresent(book ->
                commandManager.execute(new RemoveBookCommand(repository, book))
        );
    }

    public boolean canUndo() {
        return commandManager.canUndo();
    }

    public void undo() {
        commandManager.undo();
    }

    /** Returns descriptions of all executed commands, most recent first. */
    public List<String> getCommandHistory() {
        return commandManager.getHistory();
    }

    public int commandHistorySize() {
        return commandManager.historySize();
    }

    // -------------------------------------------------------------------------
    // Iterator Pattern — traversing books by genre
    // -------------------------------------------------------------------------

    /**
     * Creates and returns an iterator that traverses only books of the given genre.
     * The iterator holds an immutable snapshot — repository changes afterwards are invisible to it.
     */
    public IBookIterator createGenreIterator(String genre) {
        List<Book> filtered = repository.findByGenre(genre);
        return new GenreBookIterator(filtered);
    }

    /** Returns all genre values currently present in the repository (sorted, no duplicates). */
    public List<String> getAvailableGenres() {
        return repository.findAll().stream()
                .map(Book::getGenre)
                .distinct()
                .sorted()
                .toList();
    }

    // -------------------------------------------------------------------------
    // Seed data — gives the demo something to work with immediately
    // -------------------------------------------------------------------------

    private void seedData() {
        repository.add(new Book(idCounter.getAndIncrement(), "The Hobbit",              "J.R.R. Tolkien",    "Fantasy",         1937));
        repository.add(new Book(idCounter.getAndIncrement(), "Harry Potter",            "J.K. Rowling",      "Fantasy",         1997));
        repository.add(new Book(idCounter.getAndIncrement(), "The Name of the Wind",    "Patrick Rothfuss",  "Fantasy",         2007));
        repository.add(new Book(idCounter.getAndIncrement(), "Dune",                    "Frank Herbert",     "Sci-Fi",          1965));
        repository.add(new Book(idCounter.getAndIncrement(), "Foundation",              "Isaac Asimov",      "Sci-Fi",          1951));
        repository.add(new Book(idCounter.getAndIncrement(), "Neuromancer",             "William Gibson",    "Sci-Fi",          1984));
        repository.add(new Book(idCounter.getAndIncrement(), "Sherlock Holmes",         "Arthur Conan Doyle","Mystery",         1892));
        repository.add(new Book(idCounter.getAndIncrement(), "And Then There Were None","Agatha Christie",   "Mystery",         1939));
        repository.add(new Book(idCounter.getAndIncrement(), "The Girl with the Dragon Tattoo", "Stieg Larsson", "Mystery",    2005));
    }
}
