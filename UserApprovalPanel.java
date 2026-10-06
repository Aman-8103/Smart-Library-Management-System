import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class UserApprovalPanel extends JPanel {
    private JTable pendingTable;
    private DefaultTableModel tableModel;

    public UserApprovalPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initializeComponents();
        loadPendingApprovals();
    }

    private void initializeComponents() {
        String[] columns = {"ID", "Username", "Full Name", "Email", "Role"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        pendingTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(pendingTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        approveButton.addActionListener(e -> handleApproval(true));
        rejectButton.addActionListener(e -> handleApproval(false));
        refreshButton.addActionListener(e -> loadPendingApprovals());

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleApproval(boolean isApproved) {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to " + 
                (isApproved ? "approve" : "reject"));
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET is_active = ? WHERE user_id = ?"
            );
            
            stmt.setBoolean(1, isApproved);
            stmt.setInt(2, userId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(this, 
                    "User " + username + " has been " + 
                    (isApproved ? "approved" : "rejected") + " successfully");
                
                // Add notification
                PreparedStatement notifyStmt = conn.prepareStatement(
                    "INSERT INTO notifications (user_id, message) VALUES (?, ?)"
                );
                notifyStmt.setInt(1, userId);
                notifyStmt.setString(2, "Your account has been " + 
                    (isApproved ? "approved" : "rejected") + " by the administrator.");
                notifyStmt.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error " + (isApproved ? "approving" : "rejecting") + 
                " user: " + ex.getMessage());
        }
    }

    private void loadPendingApprovals() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id, username, full_name, email, role " +
                "FROM users WHERE is_active = false ORDER BY user_id"
            );
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("role")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading pending approvals: " + ex.getMessage());
        }
    }
}