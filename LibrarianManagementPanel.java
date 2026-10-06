import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class LibrarianManagementPanel extends JPanel {
    private JTable librarianTable;
    private DefaultTableModel tableModel;
    private Color backgroundColor;
    
    public LibrarianManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        backgroundColor = getBackground();
        initializeComponents();
        loadLibrarians();
    }

    private void initializeComponents() {
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(backgroundColor);
        JLabel titleLabel = new JLabel("Manage Librarians");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titlePanel.add(titleLabel);

        // Create table model with columns
        String[] columns = {"ID", "Username", "Full Name", "Email", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create and configure table
        librarianTable = new JTable(tableModel);
        librarianTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        librarianTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        librarianTable.setRowHeight(25);
        librarianTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create table scroll pane
        JScrollPane scrollPane = new JScrollPane(librarianTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(backgroundColor);
        
        JButton addButton = createStyledButton("Add Librarian");
        JButton updateButton = createStyledButton("Update Librarian");
        JButton deactivateButton = createStyledButton("Deactivate Librarian");
        JButton reactivateButton = createStyledButton("Reactivate Librarian");
        JButton refreshButton = createStyledButton("Refresh");
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(updateButton);
        buttonsPanel.add(deactivateButton);
        buttonsPanel.add(reactivateButton);
        buttonsPanel.add(refreshButton);
        
        // Add components to panel
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add button listeners
        deactivateButton.addActionListener(e -> deactivateLibrarian());
        reactivateButton.addActionListener(e -> reactivateLibrarian());
        updateButton.addActionListener(e -> updateLibrarian());
        addButton.addActionListener(e -> addLibrarian());
        refreshButton.addActionListener(e -> loadLibrarians());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(150, 30));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }

    private void deactivateLibrarian() {
        int selectedRow = librarianTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a librarian to deactivate",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int librarianId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);

        if (currentStatus.equals("Inactive")) {
            JOptionPane.showMessageDialog(this, 
                "This librarian is already inactive.",
                "Already Inactive",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to deactivate librarian: " + username + "?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET is_active = false WHERE user_id = ? AND role = 'LIBRARIAN'"
                );
                stmt.setInt(1, librarianId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Librarian " + username + " has been deactivated successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadLibrarians();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Database error while deactivating librarian: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reactivateLibrarian() {
        int selectedRow = librarianTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a librarian to reactivate",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int librarianId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 4);

        if (currentStatus.equals("Active")) {
            JOptionPane.showMessageDialog(this, 
                "This librarian is already active.",
                "Already Active",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reactivate librarian: " + username + "?",
                "Confirm Reactivation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET is_active = true WHERE user_id = ? AND role = 'LIBRARIAN'"
                );
                stmt.setInt(1, librarianId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Librarian " + username + " has been reactivated successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadLibrarians();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "Database error while reactivating librarian: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateLibrarian() {
        int selectedRow = librarianTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a librarian to update");
            return;
        }

        int librarianId = (int) tableModel.getValueAt(selectedRow, 0);
        showUpdateDialog(librarianId);
    }

    private void showUpdateDialog(int librarianId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Update Librarian", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField fullNameField = new JTextField(20);
        JTextField emailField = new JTextField(20);

        // Load current values
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT full_name, email FROM users WHERE user_id = ? AND role = 'LIBRARIAN'"
            );
            stmt.setInt(1, librarianId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                fullNameField.setText(rs.getString("full_name"));
                emailField.setText(rs.getString("email"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Error loading librarian data: " + ex.getMessage());
            dialog.dispose();
            return;
        }

        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        JButton updateButton = createStyledButton("Update");
        JButton cancelButton = createStyledButton("Cancel");

        updateButton.addActionListener(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET full_name = ?, email = ? WHERE user_id = ? AND role = 'LIBRARIAN'"
                );
                
                stmt.setString(1, fullNameField.getText().trim());
                stmt.setString(2, emailField.getText().trim());
                stmt.setInt(3, librarianId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(dialog, "Librarian updated successfully");
                    loadLibrarians();
                    dialog.dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error updating librarian: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addLibrarian() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Librarian", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField fullNameField = new JTextField(20);
        JTextField emailField = new JTextField(20);

        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);
        JButton addButton = createStyledButton("Add");
        JButton cancelButton = createStyledButton("Cancel");

        addButton.addActionListener(e -> {
            try {
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, full_name, email, role, is_active) " +
                    "VALUES (?, ?, ?, ?, 'LIBRARIAN', true)"
                );
                
                stmt.setString(1, usernameField.getText().trim());
                stmt.setString(2, new String(passwordField.getPassword()));
                stmt.setString(3, fullNameField.getText().trim());
                stmt.setString(4, emailField.getText().trim());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(dialog, "Librarian added successfully");
                    loadLibrarians();
                    dialog.dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error adding librarian: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadLibrarians() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT user_id, username, full_name, email, is_active " +
                "FROM users WHERE role = 'LIBRARIAN' ORDER BY user_id"
            );
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getBoolean("is_active") ? "Active" : "Inactive"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading librarians: " + ex.getMessage());
        }
    }
}