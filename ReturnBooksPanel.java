import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReturnBooksPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);

    public ReturnBooksPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add title
        JLabel titleLabel = new JLabel("Return Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Book ID", "Title", "Borrow Date", "Due Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable borrowedBooksTable = new JTable(model);
        borrowedBooksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        borrowedBooksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        borrowedBooksTable.setRowHeight(25);
        borrowedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load borrowed books
        loadBorrowedBooks(model);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(borrowedBooksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create return button
        JButton returnButton = new JButton("Return Selected Book");
        returnButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        returnButton.setBackground(new Color(70, 130, 180));
        returnButton.setForeground(Color.WHITE);
        returnButton.setFocusPainted(false);

        returnButton.addActionListener(e -> {
            int selectedRow = borrowedBooksTable.getSelectedRow();
            if (selectedRow != -1) {
                int bookId = (int) borrowedBooksTable.getValueAt(selectedRow, 0);
                returnBook(bookId, model, selectedRow);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a book to return",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        // Add button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        buttonPanel.add(returnButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadBorrowedBooks(DefaultTableModel model) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT b.book_id, b.title, bb.borrow_date, bb.due_date " +
                "FROM book_borrowings bb " +
                "JOIN books b ON bb.book_id = b.book_id " +
                "WHERE bb.user_id = ? AND bb.status = 'BORROWED'"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading borrowed books: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook(int bookId, DefaultTableModel model, int row) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try {
                // Update book_borrowings status
                PreparedStatement updateBorrowingStmt = conn.prepareStatement(
                    "UPDATE book_borrowings SET status = 'RETURNED', return_date = CURRENT_DATE " +
                    "WHERE book_id = ? AND user_id = ? AND status = 'BORROWED'"
                );
                updateBorrowingStmt.setInt(1, bookId);
                updateBorrowingStmt.setInt(2, userId);
                updateBorrowingStmt.executeUpdate();

                // Update book available quantity
                PreparedStatement updateBookStmt = conn.prepareStatement(
                    "UPDATE books SET available_quantity = available_quantity + 1 " +
                    "WHERE book_id = ?"
                );
                updateBookStmt.setInt(1, bookId);
                updateBookStmt.executeUpdate();

                conn.commit();
                model.removeRow(row);
                
                JOptionPane.showMessageDialog(this,
                    "Book returned successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error returning book: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}