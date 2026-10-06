import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class BorrowBooksPanel extends JPanel {
    private int userId;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public BorrowBooksPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        // Create search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // Create table
        createBookTable();
        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create borrow button panel
        JPanel buttonPanel = new JPanel();
        JButton borrowButton = new JButton("Borrow Selected Book");
        borrowButton.addActionListener(e -> borrowBook());
        buttonPanel.add(borrowButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Load available books
        loadAvailableBooks();
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Search:"));
        searchField = new JTextField(30);
        searchField.addActionListener(e -> searchBooks());
        panel.add(searchField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());
        panel.add(searchButton);
        
        return panel;
    }

    private void createBookTable() {
        String[] columns = {"ID", "ISBN", "Title", "Author", "Available Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
    }

    private void loadAvailableBooks() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM books WHERE available_quantity > 0 AND is_active = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM books WHERE (title LIKE ? OR author LIKE ? OR isbn LIKE ?) " +
                          "AND available_quantity > 0 AND is_active = true";
            PreparedStatement pstmt = conn.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching books: " + e.getMessage());
        }
    }

    private void borrowBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a book to borrow");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if user has any overdue books
            String overdueCheck = "SELECT COUNT(*) FROM book_borrowings WHERE user_id = ? " +
                                "AND status = 'BORROWED' AND due_date < CURRENT_DATE";
            PreparedStatement checkStmt = conn.prepareStatement(overdueCheck);
            checkStmt.setInt(1, userId);
            ResultSet checkRs = checkStmt.executeQuery();
            checkRs.next();
            if (checkRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "You have overdue books. Please return them first.");
                return;
            }

            // Start transaction
            conn.setAutoCommit(false);
            try {
                // Update available quantity
                String updateBook = "UPDATE books SET available_quantity = available_quantity - 1 " +
                                  "WHERE book_id = ? AND available_quantity > 0";
                PreparedStatement updateStmt = conn.prepareStatement(updateBook);
                int bookId = (Integer) bookTable.getValueAt(selectedRow, 0);
                updateStmt.setInt(1, bookId);
                int updated = updateStmt.executeUpdate();

                if (updated > 0) {
                    // Create borrowing record
                    String insertBorrowing = "INSERT INTO book_borrowings (book_id, user_id, borrow_date, due_date, status) " +
                                           "VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'BORROWED')";
                    PreparedStatement borrowStmt = conn.prepareStatement(insertBorrowing);
                    borrowStmt.setInt(1, bookId);
                    borrowStmt.setInt(2, userId);
                    borrowStmt.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Book borrowed successfully!");
                    loadAvailableBooks();
                } else {
                    throw new SQLException("Book not available");
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error borrowing book: " + e.getMessage());
        }
    }
}