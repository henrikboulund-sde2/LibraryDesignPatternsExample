package dk.easv.designpatternsexample.bll.command;

import dk.easv.designpatternsexample.dal.Book;
import dk.easv.designpatternsexample.dal.IBookRepository;

/**
 * Concrete command that adds a book to the repository.
 *
 * execute() → adds the book.
 * undo()    → removes the same book, restoring the previous state.
 */
public class AddBookCommand implements ICommand {

    private final IBookRepository repository;
    private final Book book;

    public AddBookCommand(IBookRepository repository, Book book) {
        this.repository = repository;
        this.book = book;
    }

    @Override
    public void execute() {
        repository.add(book);
    }

    @Override
    public void undo() {
        // Undo an "add" by removing the book that was added.
        repository.remove(book.getId());
    }

    @Override
    public String getDescription() {
        return "Add: \"" + book.getTitle() + "\" by " + book.getAuthor();
    }
}
