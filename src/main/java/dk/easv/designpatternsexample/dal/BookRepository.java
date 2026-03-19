package dk.easv.designpatternsexample.dal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of IBookRepository.
 * Belongs to the Data Access Layer (DAL).
 *
 * In a real application this could be replaced with a DatabaseBookRepository
 * or a FileBookRepository without touching a single line in the BLL or UI.
 * That is the core benefit of the Repository Pattern.
 */
public class BookRepository implements IBookRepository {

    // LinkedHashMap preserves insertion order, so books appear in the order they were added.
    private final Map<Integer, Book> store = new LinkedHashMap<>();

    @Override
    public void add(Book book) {
        store.put(book.getId(), book);
    }

    @Override
    public void remove(int id) {
        store.remove(id);
    }

    @Override
    public Optional<Book> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Book> findByGenre(String genre) {
        return store.values().stream()
                .filter(book -> book.getGenre().equalsIgnoreCase(genre))
                .toList();
    }
}
