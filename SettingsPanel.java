import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SettingsPanel extends JPanel {
    private int userId;
    private JComboBox<String> themeComboBox;
    private JCheckBox notificationsCheckBox;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public SettingsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        
        // Create settings panel
        JPanel settingsPanel = createSettingsPanel();
        add(settingsPanel, BorderLayout.CENTER);
        
        // Load current settings
        loadSettings();
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Theme:"), gbc);
        
        gbc.gridx = 1;
        String[] themes = {"Light", "Dark"};
        themeComboBox = new JComboBox<>(themes);
        panel.add(themeComboBox, gbc);
        
        // Notifications
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Enable Notifications:"), gbc);
        
        gbc.gridx = 1;
        notificationsCheckBox = new JCheckBox();
        panel.add(notificationsCheckBox, gbc);
        
        // Password change section
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        
        gbc.gridy = 3;
        panel.add(new JLabel("Change Password"), gbc);
        
        // Current password
        gbc.gridwidth = 1;
        gbc.gridy = 4;
        panel.add(new JLabel("Current Password:"), gbc);
        
        gbc.gridx = 1;
        currentPasswordField = new JPasswordField(20);
        panel.add(currentPasswordField, gbc);
        
        // New password
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("New Password:"), gbc);
        
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        panel.add(newPasswordField, gbc);
        
        // Confirm password
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField, gbc);
        
        // Save button
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveSettings());
        panel.add(saveButton, gbc);
        
        return panel;
    }

    private void loadSettings() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT theme_mode, notification_enabled FROM settings WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                themeComboBox.setSelectedItem(rs.getString("theme_mode"));
                notificationsCheckBox.setSelected(rs.getBoolean("notification_enabled"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading settings: " + e.getMessage());
        }
    }

    private void saveSettings() {
        String theme = (String) themeComboBox.getSelectedItem();
        boolean notifications = notificationsCheckBox.isSelected();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Update settings
            String settingsQuery = "INSERT INTO settings (user_id, theme_mode, notification_enabled) " +
                                 "VALUES (?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE theme_mode = ?, notification_enabled = ?";
            PreparedStatement settingsStmt = conn.prepareStatement(settingsQuery);
            settingsStmt.setInt(1, userId);
            settingsStmt.setString(2, theme);
            settingsStmt.setBoolean(3, notifications);
            settingsStmt.setString(4, theme);
            settingsStmt.setBoolean(5, notifications);
            settingsStmt.executeUpdate();

            // Update password if provided
            if (!currentPassword.isEmpty() && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(this, "New passwords do not match!");
                    return;
                }

                String passwordQuery = "UPDATE users SET password = ? WHERE user_id = ? AND password = ?";
                PreparedStatement passwordStmt = conn.prepareStatement(passwordQuery);
                passwordStmt.setString(1, newPassword);
                passwordStmt.setInt(2, userId);
                passwordStmt.setString(3, currentPassword);
                
                int updated = passwordStmt.executeUpdate();
                if (updated > 0) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully!");
                    clearPasswordFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect!");
                }
            }

            JOptionPane.showMessageDialog(this, "Settings saved successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage());
        }
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }
}
