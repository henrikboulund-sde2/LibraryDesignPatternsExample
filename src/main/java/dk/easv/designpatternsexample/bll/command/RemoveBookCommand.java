package dk.easv.designpatternsexample.bll.command;

import dk.easv.designpatternsexample.dal.Book;
import dk.easv.designpatternsexample.dal.IBookRepository;

/**
 * Concrete command that removes a book from the repository.
 *
 * The removed Book object is stored inside the command so that
 * undo() can put it back — this is the key insight of undoable commands.
 *
 * execute() → removes the book.
 * undo()    → re-adds the stored book, restoring the previous state.
 */
public class RemoveBookCommand implements ICommand {

    private final IBookRepository repository;
    // We hold a reference to the book so undo() can restore it.
    private final Book book;

    public RemoveBookCommand(IBookRepository repository, Book book) {
        this.repository = repository;
        this.book = book;
    }

    @Override
    public void execute() {
        repository.remove(book.getId());
    }

    @Override
    public void undo() {
        // Undo a "remove" by adding the book back.
        repository.add(book);
    }

    @Override
    public String getDescription() {
        return "Remove: \"" + book.getTitle() + "\" by " + book.getAuthor();
    }
}
