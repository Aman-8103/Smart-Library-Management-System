import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StatusPanel extends JPanel {
    private int userId;
    private JTable borrowingsTable;
    private DefaultTableModel tableModel;
    private JLabel fineLabel;

    public StatusPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        // Create header panel with fine information
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Create table
        createBorrowingsTable();
        JScrollPane scrollPane = new JScrollPane(borrowingsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load data
        loadBorrowings();
        updateFineAmount();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fineLabel = new JLabel("Total Outstanding Fines: $0.00");
        fineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fineLabel);
        return panel;
    }

    private void createBorrowingsTable() {
        String[] columns = {"Book Title", "Borrow Date", "Due Date", "Status", "Fine"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowingsTable = new JTable(tableModel);
    }

    private void loadBorrowings() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT b.title, bb.borrow_date, bb.due_date, bb.status, bb.fine_amount " +
                          "FROM book_borrowings bb " +
                          "JOIN books b ON bb.book_id = b.book_id " +
                          "WHERE bb.user_id = ? " +
                          "ORDER BY bb.borrow_date DESC";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getString("title"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date"),
                    rs.getString("status"),
                    String.format("$%.2f", rs.getDouble("fine_amount"))
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading borrowing history: " + e.getMessage());
        }
    }

    private void updateFineAmount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT SUM(fine_amount) FROM book_borrowings WHERE user_id = ? AND fine_paid = false";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double totalFine = rs.getDouble(1);
                fineLabel.setText(String.format("Total Outstanding Fines: $%.2f", totalFine));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating fines: " + e.getMessage());
        }
    }
}
