import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FineManagementPanel extends JPanel {
    private JTable fineTable;
    private DefaultTableModel tableModel;
    private JTextField studentIdField;
    private JLabel totalFinesLabel;

    public FineManagementPanel() {
        setLayout(new BorderLayout());
        
        // Create search panel
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // Create table
        createFineTable();
        JScrollPane scrollPane = new JScrollPane(fineTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with total and buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Student ID:"));
        studentIdField = new JTextField(10);
        panel.add(studentIdField);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchFines());
        panel.add(searchButton);
        
        return panel;
    }

    private void createFineTable() {
        String[] columns = {"Fine ID", "Student Name", "Book Title", "Due Date", "Days Late", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        fineTable = new JTable(tableModel);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Total fines label
        totalFinesLabel = new JLabel("Total Outstanding Fines: $0.00");
        totalFinesLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalFinesLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panel.add(totalFinesLabel, BorderLayout.WEST);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        JButton payButton = new JButton("Record Payment");
        JButton waiveButton = new JButton("Waive Fine");
        
        payButton.addActionListener(e -> recordPayment());
        waiveButton.addActionListener(e -> waiveFine());
        
        buttonsPanel.add(payButton);
        buttonsPanel.add(waiveButton);
        panel.add(buttonsPanel, BorderLayout.EAST);
        
        return panel;
    }

    private void searchFines() {
        String studentId = studentIdField.getText().trim();
        if (studentId.isEmpty()) {
            loadAllFines();
            return;
        }

        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT f.fine_id, u.full_name, b.title, bb.due_date, " +
                          "DATEDIFF(IFNULL(bb.return_date, CURRENT_DATE), bb.due_date) as days_late, " +
                          "f.amount, f.status " +
                          "FROM fines f " +
                          "JOIN book_borrowings bb ON f.borrow_id = bb.borrow_id " +
                          "JOIN users u ON bb.user_id = u.user_id " +
                          "JOIN books b ON bb.book_id = b.book_id " +
                          "WHERE u.user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, studentId);
            
            ResultSet rs = pstmt.executeQuery();
            double totalFines = 0;
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("fine_id"),
                    rs.getString("full_name"),
                    rs.getString("title"),
                    rs.getDate("due_date"),
                    rs.getInt("days_late"),
                    String.format("$%.2f", rs.getDouble("amount")),
                    rs.getString("status")
                };
                tableModel.addRow(row);
                
                if ("PENDING".equals(rs.getString("status"))) {
                    totalFines += rs.getDouble("amount");
                }
            }
            
            updateTotalFines(totalFines);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching fines: " + e.getMessage());
        }
    }

    private void loadAllFines() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT f.fine_id, u.full_name, b.title, bb.due_date, " +
                          "DATEDIFF(IFNULL(bb.return_date, CURRENT_DATE), bb.due_date) as days_late, " +
                          "f.amount, f.status " +
                          "FROM fines f " +
                          "JOIN book_borrowings bb ON f.borrow_id = bb.borrow_id " +
                          "JOIN users u ON bb.user_id = u.user_id " +
                          "JOIN books b ON bb.book_id = b.book_id " +
                          "ORDER BY f.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            double totalFines = 0;
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("fine_id"),
                    rs.getString("full_name"),
                    rs.getString("title"),
                    rs.getDate("due_date"),
                    rs.getInt("days_late"),
                    String.format("$%.2f", rs.getDouble("amount")),
                    rs.getString("status")
                };
                tableModel.addRow(row);
                
                if ("PENDING".equals(rs.getString("status"))) {
                    totalFines += rs.getDouble("amount");
                }
            }
            
            updateTotalFines(totalFines);
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage());
        }
    }

    private void recordPayment() {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a fine to record payment");
            return;
        }

        if (!"PENDING".equals(tableModel.getValueAt(selectedRow, 6))) {
            JOptionPane.showMessageDialog(this, "Can only process payment for pending fines");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "UPDATE fines SET status = 'PAID', paid_at = CURRENT_TIMESTAMP " +
                          "WHERE fine_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, (Integer) tableModel.getValueAt(selectedRow, 0));
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Payment recorded successfully!");
            searchFines();
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error recording payment: " + e.getMessage());
        }
    }

    private void waiveFine() {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a fine to waive");
            return;
        }

        if (!"PENDING".equals(tableModel.getValueAt(selectedRow, 6))) {
            JOptionPane.showMessageDialog(this, "Can only waive pending fines");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to waive this fine?",
            "Confirm Waive",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String query = "UPDATE fines SET status = 'WAIVED', paid_at = CURRENT_TIMESTAMP " +
                              "WHERE fine_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, (Integer) tableModel.getValueAt(selectedRow, 0));
                
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Fine waived successfully!");
                searchFines();
                
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error waiving fine: " + e.getMessage());
            }
        }
    }

    private void updateTotalFines(double total) {
        totalFinesLabel.setText(String.format("Total Outstanding Fines: $%.2f", total));
    }
}
