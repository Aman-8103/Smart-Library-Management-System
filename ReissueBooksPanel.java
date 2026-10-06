import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ReissueBooksPanel extends JPanel {
    private JTable borrowedBooksTable;
    private DefaultTableModel tableModel;
    private int userId;

    public ReissueBooksPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
        loadBorrowedBooks();
    }

    private void initializeComponents() {
        // Create table
        String[] columns = {"Borrowing ID", "Book Title", "Borrow Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        borrowedBooksTable = new JTable(tableModel);
        borrowedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(borrowedBooksTable);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reissueButton = createStyledButton("Reissue Book");
        JButton refreshButton = createStyledButton("Refresh");
        buttonPanel.add(reissueButton);
        buttonPanel.add(refreshButton);

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners
        reissueButton.addActionListener(e -> reissueBook());
        refreshButton.addActionListener(e -> loadBorrowedBooks());
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

    private void loadBorrowedBooks() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT bb.borrowing_id, b.title, bb.borrow_date, bb.due_date, bb.status " +
                "FROM book_borrowings bb " +
                "JOIN books b ON bb.book_id = b.book_id " +
                "WHERE bb.user_id = ? AND bb.status = 'BORROWED' " +
                "ORDER BY bb.due_date"
            );
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("borrowing_id"),
                    rs.getString("title"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading borrowed books: " + ex.getMessage());
        }
    }

    private void reissueBook() {
        int selectedRow = borrowedBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to reissue");
            return;
        }

        int borrowingId = (int) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if book has already been reissued
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings " +
                "WHERE borrowing_id = ? AND due_date > DATE_ADD(borrow_date, INTERVAL 14 DAY)"
            );
            checkStmt.setInt(1, borrowingId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, 
                    "This book has already been reissued once. Cannot reissue again.");
                return;
            }

            // Extend due date by 7 days
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE book_borrowings SET due_date = DATE_ADD(due_date, INTERVAL 7 DAY) " +
                "WHERE borrowing_id = ? AND status = 'BORROWED'"
            );
            updateStmt.setInt(1, borrowingId);
            
            int result = updateStmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Book '" + bookTitle + "' has been reissued successfully.\n" +
                    "Due date extended by 7 days.");
                loadBorrowedBooks();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reissue book. Please try again.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reissuing book: " + ex.getMessage());
        }
    }
}
