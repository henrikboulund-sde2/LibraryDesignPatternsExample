package dk.easv.designpatternsexample.dal;

/**
 * Entity class representing a book in the library.
 * Belongs to the Data Access Layer (DAL) as it is the core data model.
 */
public class Book {

    private final int id;
    private String title;
    private String author;
    private String genre;
    private int year;

    public Book(int id, String title, String author, String genre, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
    }

    public int getId()       { return id; }
    public String getTitle() { return title; }
    public String getAuthor(){ return author; }
    public String getGenre() { return genre; }
    public int getYear()     { return year; }

    public void setTitle(String title)   { this.title  = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre)   { this.genre  = genre; }
    public void setYear(int year)        { this.year   = year; }

    @Override
    public String toString() {
        return String.format("[%d] \"%s\" by %s (%s, %d)", id, title, author, genre, year);
    }
}
