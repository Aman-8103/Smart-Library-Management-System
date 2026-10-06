import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class NotificationPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private DefaultTableModel tableModel;
    private JTable notificationsTable;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public NotificationPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createComponents();
        loadNotifications();
    }

    private void createComponents() {
        // Title Panel
        JLabel titleLabel = new JLabel("Notifications", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Date", "Message", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        notificationsTable = new JTable(tableModel);
        notificationsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notificationsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        notificationsTable.setRowHeight(25);
        notificationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        notificationsTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        notificationsTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        notificationsTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(notificationsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JButton markReadButton = new JButton("Mark as Read");
        JButton deleteButton = new JButton("Delete");
        JButton refreshButton = new JButton("Refresh");

        // Style buttons
        for (JButton button : new JButton[]{markReadButton, deleteButton, refreshButton}) {
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            buttonPanel.add(button);
        }

        // Add button actions
        markReadButton.addActionListener(e -> markAsRead());
        deleteButton.addActionListener(e -> deleteNotification());
        refreshButton.addActionListener(e -> loadNotifications());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadNotifications() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT created_at, message, is_read " +
                "FROM notifications " +
                "WHERE user_id = ? " +
                "ORDER BY created_at DESC"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    dateFormat.format(rs.getTimestamp("created_at")),
                    rs.getString("message"),
                    rs.getBoolean("is_read") ? "READ" : "UNREAD"
                };
                tableModel.addRow(row);
            }

            // Update unread count
            updateUnreadCount();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading notifications: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markAsRead() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a notification to mark as read",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE notifications SET is_read = true " +
                "WHERE user_id = ? AND created_at = ? AND message = ?"
            );
            stmt.setInt(1, userId);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(tableModel.getValueAt(selectedRow, 0).toString()));
            stmt.setString(3, tableModel.getValueAt(selectedRow, 1).toString());
            
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                tableModel.setValueAt("READ", selectedRow, 2);
                updateUnreadCount();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error marking notification as read: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteNotification() {
        int selectedRow = notificationsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a notification to delete",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this notification?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM notifications " +
                    "WHERE user_id = ? AND created_at = ? AND message = ?"
                );
                stmt.setInt(1, userId);
                stmt.setTimestamp(2, java.sql.Timestamp.valueOf(tableModel.getValueAt(selectedRow, 0).toString()));
                stmt.setString(3, tableModel.getValueAt(selectedRow, 1).toString());
                
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    tableModel.removeRow(selectedRow);
                    updateUnreadCount();
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error deleting notification: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateUnreadCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications " +
                "WHERE user_id = ? AND is_read = false"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int unreadCount = rs.getInt(1);
                if (unreadCount > 0) {
                    ((JLabel)getComponent(0)).setText("Notifications (" + unreadCount + " unread)");
                } else {
                    ((JLabel)getComponent(0)).setText("Notifications");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}