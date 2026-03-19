package dk.easv.designpatternsexample.dal;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for book storage operations.
 * Belongs to the Data Access Layer (DAL).
 *
 * Repository Pattern:
 *   Defines a contract (interface) for all data access operations.
 *   The BLL and UI depend only on this interface — never on a concrete class.
 *   This means the underlying storage (in-memory, database, file, etc.)
 *   can be swapped without changing any code outside the DAL.
 */
public interface IBookRepository {

    void add(Book book);

    void remove(int id);

    Optional<Book> findById(int id);

    List<Book> findAll();

    List<Book> findByGenre(String genre);
}
