package dk.easv.designpatternsexample.bll.iterator;

import dk.easv.designpatternsexample.dal.Book;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterates over a snapshot of books that match a specific genre.
 * Belongs to the Business Logic Layer (BLL).
 *
 * The iterator takes an immutable snapshot of the filtered list at creation time.
 * This means changes to the repository after the iterator is created are NOT reflected —
 * a deliberate choice to keep iteration consistent (no "collection modified during iteration").
 */
public class GenreBookIterator implements IBookIterator {

    private final List<Book> books;   // immutable snapshot
    private int currentIndex;

    public GenreBookIterator(List<Book> books) {
        // Copy the list so the iterator is unaffected by later repository changes.
        this.books = List.copyOf(books);
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < books.size();
    }

    @Override
    public Book next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more books — all " + books.size() + " have been visited.");
        }
        return books.get(currentIndex++);
    }

    @Override
    public void reset() {
        currentIndex = 0;
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public int getCurrentIndex() {
        return currentIndex;
    }
}
