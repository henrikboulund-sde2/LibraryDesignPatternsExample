package dk.easv.designpatternsexample.bll.iterator;

import dk.easv.designpatternsexample.dal.Book;

/**
 * Iterator interface for traversing a collection of books.
 * Belongs to the Business Logic Layer (BLL).
 *
 * Iterator Pattern:
 *   Provides a standard way to sequentially access elements of a collection
 *   without exposing the underlying data structure (list, tree, database cursor, etc.).
 *   The caller only needs to know: hasNext(), next(), and reset() — nothing else.
 */
public interface IBookIterator {

    /** Returns true if there are more books to visit. */
    boolean hasNext();

    /** Returns the next book and advances the internal cursor. */
    Book next();

    /** Resets the cursor to the beginning of the collection. */
    void reset();

    /** Total number of books in this iterator's collection. */
    int getCount();

    /** The current position (0-based index of the next book to be returned). */
    int getCurrentIndex();
}
