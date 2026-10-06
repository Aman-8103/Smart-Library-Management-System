import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.Calendar;

public class IssueBooksPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JTextField searchField;

    public IssueBooksPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createComponents();
        loadBooks();
    }

    private void createComponents() {
        // Title Panel
        JLabel titleLabel = new JLabel("Issue Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        // Create top panel for student ID and search
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        // Student ID panel
        JPanel studentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JLabel studentLabel = new JLabel("Student ID:");
        studentLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        studentIdField = new JTextField(10);
        studentPanel.add(studentLabel);
        studentPanel.add(studentIdField);

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        JLabel searchLabel = new JLabel("Search Books:");
        searchLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        styleButton(searchButton);
        searchButton.addActionListener(e -> searchBooks());
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        topPanel.add(studentPanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Book ID", "Title", "Author", "Category", "Available Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        booksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        booksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        booksTable.setRowHeight(25);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JButton issueButton = new JButton("Issue Book");
        JButton clearButton = new JButton("Clear");
        JButton refreshButton = new JButton("Refresh");

        styleButton(issueButton);
        styleButton(clearButton);
        styleButton(refreshButton);

        issueButton.addActionListener(e -> issueBook());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> loadBooks());

        buttonPanel.add(issueButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT book_id, title, author, category, available_quantity " +
                "FROM books WHERE is_active = 1 AND available_quantity > 0"
            );
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading books: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBooks() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadBooks();
            return;
        }

        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT book_id, title, author, category, available_quantity " +
                "FROM books WHERE is_active = 1 AND available_quantity > 0 " +
                "AND (title LIKE ? OR author LIKE ? OR category LIKE ?)"
            );
            String pattern = "%" + searchText + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("category"),
                    rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error searching books: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void issueBook() {
        int selectedRow = booksTable.getSelectedRow();
        String studentIdText = studentIdField.getText().trim();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a book to issue",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (studentIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a student ID",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = Integer.parseInt(studentIdText);
            int bookId = (int) tableModel.getValueAt(selectedRow, 0);
            
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Verify student exists and is active
                PreparedStatement checkStudent = conn.prepareStatement(
                    "SELECT is_active FROM users WHERE user_id = ? AND role = 'STUDENT'"
                );
                checkStudent.setInt(1, studentId);
                ResultSet studentRs = checkStudent.executeQuery();

                if (!studentRs.next()) {
                    throw new Exception("Student ID not found");
                }

                if (!studentRs.getBoolean("is_active")) {
                    throw new Exception("Student account is not active");
                }

                // Check if student has any overdue books
                PreparedStatement checkOverdue = conn.prepareStatement(
                    "SELECT COUNT(*) FROM book_borrowings " +
                    "WHERE user_id = ? AND status = 'BORROWED' AND due_date < CURRENT_DATE"
                );
                checkOverdue.setInt(1, studentId);
                ResultSet overdueRs = checkOverdue.executeQuery();
                overdueRs.next();
                if (overdueRs.getInt(1) > 0) {
                    throw new Exception("Student has overdue books");
                }

                // Check if student already has this book
                PreparedStatement checkBorrowed = conn.prepareStatement(
                    "SELECT COUNT(*) FROM book_borrowings " +
                    "WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'"
                );
                checkBorrowed.setInt(1, studentId);
                checkBorrowed.setInt(2, bookId);
                ResultSet borrowedRs = checkBorrowed.executeQuery();
                borrowedRs.next();
                if (borrowedRs.getInt(1) > 0) {
                    throw new Exception("Student already has this book");
                }

                // Update book quantity
                PreparedStatement updateBook = conn.prepareStatement(
                    "UPDATE books SET available_quantity = available_quantity - 1 " +
                    "WHERE book_id = ? AND available_quantity > 0"
                );
                updateBook.setInt(1, bookId);
                int updated = updateBook.executeUpdate();
                if (updated == 0) {
                    throw new Exception("Book not available");
                }

                // Create borrowing record
                Calendar cal = Calendar.getInstance();
                Date borrowDate = new Date();
                cal.setTime(borrowDate);
                cal.add(Calendar.DAY_OF_MONTH, 14); // 14 days borrowing period
                Date dueDate = cal.getTime();

                PreparedStatement insertBorrowing = conn.prepareStatement(
                    "INSERT INTO book_borrowings (book_id, user_id, borrow_date, due_date, status) " +
                    "VALUES (?, ?, ?, ?, 'BORROWED')"
                );
                insertBorrowing.setInt(1, bookId);
                insertBorrowing.setInt(2, studentId);
                insertBorrowing.setDate(3, new java.sql.Date(borrowDate.getTime()));
                insertBorrowing.setDate(4, new java.sql.Date(dueDate.getTime()));
                insertBorrowing.executeUpdate();

                // Create notification
                PreparedStatement insertNotification = conn.prepareStatement(
                    "INSERT INTO notifications (user_id, message, is_read) " +
                    "VALUES (?, ?, false)"
                );
                insertNotification.setInt(1, studentId);
                insertNotification.setString(2, "Book '" + tableModel.getValueAt(selectedRow, 1) + 
                    "' has been issued to you. Due date: " + new java.sql.Date(dueDate.getTime()));
                insertNotification.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this,
                    "Book issued successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

                loadBooks(); // Refresh the table
                clearFields();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid student ID format",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error issuing book: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        studentIdField.setText("");
        searchField.setText("");
        booksTable.clearSelection();
    }
}
