import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class RequestBooksPanel extends JPanel {
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private int userId;
    private JTextField searchField;

    public RequestBooksPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
        loadAvailableBooks();
    }

    private void initializeComponents() {
        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = createStyledButton("Search");
        searchPanel.add(new JLabel("Search Books: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Create table
        String[] columns = {"Book ID", "Title", "Author", "Category", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(booksTable);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton requestButton = createStyledButton("Request Book");
        JButton refreshButton = createStyledButton("Refresh");
        buttonPanel.add(requestButton);
        buttonPanel.add(refreshButton);

        // Add components
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        searchButton.addActionListener(e -> searchBooks());
        requestButton.addActionListener(e -> requestBook());
        refreshButton.addActionListener(e -> loadAvailableBooks());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(120, 30));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void loadAvailableBooks() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT book_id, title, author, category, " +
                "CASE WHEN available_quantity > 0 THEN 'Available' ELSE 'Not Available' END as status " +
                "FROM books " +
                "WHERE is_active = true"
            );
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage());
        }
    }

    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        tableModel.setRowCount(0);
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT book_id, title, author, category, " +
                "CASE WHEN available_quantity > 0 THEN 'Available' ELSE 'Not Available' END as status " +
                "FROM books " +
                "WHERE is_active = true AND " +
                "(title LIKE ? OR author LIKE ? OR category LIKE ?)"
            );
            
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching books: " + ex.getMessage());
        }
    }

    private void requestBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to request");
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if user already has a pending request for this book
            PreparedStatement checkRequestStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'"
            );
            checkRequestStmt.setInt(1, userId);
            checkRequestStmt.setInt(2, bookId);
            ResultSet requestRs = checkRequestStmt.executeQuery();
            requestRs.next();
            if (requestRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "You already have this book borrowed");
                return;
            }

            // Insert borrowing request
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO book_borrowings (book_id, user_id, borrow_date, due_date, status) " +
                "VALUES (?, ?, CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY), 'BORROWED')"
            );
            insertStmt.setInt(1, bookId);
            insertStmt.setInt(2, userId);
            
            // Update book available quantity
            PreparedStatement updateBookStmt = conn.prepareStatement(
                "UPDATE books SET available_quantity = available_quantity - 1 " +
                "WHERE book_id = ? AND available_quantity > 0"
            );
            updateBookStmt.setInt(1, bookId);

            conn.setAutoCommit(false);
            try {
                int updateResult = updateBookStmt.executeUpdate();
                if (updateResult > 0) {
                    insertStmt.executeUpdate();
                    conn.commit();
                    JOptionPane.showMessageDialog(this, 
                        "Book borrowed successfully.\nDue date is in 14 days.");
                    loadAvailableBooks(); // Refresh the table
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Book is not available for borrowing.");
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error requesting book: " + ex.getMessage());
        }
    }
}