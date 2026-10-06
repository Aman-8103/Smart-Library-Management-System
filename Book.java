public class Book {
    private int id;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private int quantity;
    private int availableQuantity;
    private boolean isActive;

    public Book(int id, String isbn, String title, String author, String publisher, 
                int publicationYear, int quantity, int availableQuantity, boolean isActive) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getAvailableQuantity() { return availableQuantity; }
    public boolean isActive() { return isActive; }
}
