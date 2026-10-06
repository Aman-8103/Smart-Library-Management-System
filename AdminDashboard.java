import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class AdminDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private JPanel menuPanel;
    private String adminName;
    private Color accentColor = new Color(70, 130, 180);

    public AdminDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Admin Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Get admin name from database (optional, fallback to 'Admin')
        adminName = "Admin";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT name, username FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String dbName = rs.getString("name");
                String dbUsername = rs.getString("username");
                System.out.println("Fetched from DB: name = '" + dbName + "', username = '" + dbUsername + "'");
                adminName = dbName;
                if (adminName == null || adminName.trim().isEmpty()) {
                    adminName = dbUsername;
                }
                if (adminName == null || adminName.trim().isEmpty()) {
                    adminName = "Admin";
                }
            } else {
                System.out.println("No user found for id: " + userId);
                adminName = "Admin";
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            adminName = "Admin";
        }

        // Create main split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(300);
        
        // Create menu panel
        menuPanel = createMenuPanel();
        splitPane.setLeftComponent(menuPanel);
        
        // Create content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        splitPane.setRightComponent(contentPanel);
        
        // Create status bar
        statusLabel = new JLabel("Welcome, " + adminName + "!");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.SOUTH);
        
        add(splitPane);
        
        // Show welcome message
        showWelcomeMessage();
        
        // Load pending approvals count
        loadPendingApprovalsCount();
        
        // Apply initial theme
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);

        // Profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setOpaque(false);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Profile icon (image centered)
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setMaximumSize(new Dimension(60, 60));
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setBackground(accentColor);
        try {
            ImageIcon icon = new ImageIcon("java/src/images/icon_admin.jpg");
            Image img = icon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconPanel.add(iconLabel);
        } catch (Exception e) {
            JLabel iconLabel = new JLabel(String.valueOf(adminName.charAt(0)).toUpperCase());
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            iconLabel.setForeground(Color.WHITE);
            iconPanel.add(iconLabel);
        }
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User info (centered)
        JLabel nameLabel = new JLabel(adminName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profilePanel.add(iconPanel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        profilePanel.add(nameLabel);
        profilePanel.add(roleLabel);

        panel.add(profilePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        searchPanel.setBorder(new LineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY, 1, true));
        JTextField searchField = new JTextField();
        searchField.setBorder(new EmptyBorder(5, 10, 5, 10));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(searchField);
        panel.add(searchPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        String[] menuItems = {
            "Dashboard Home",
            "Manage Librarians",
            "View Reports",
            "Fine Management",
            "User Approvals",
            "System Settings",
            "Toggle Theme",
            "Logout"
        };
        String[] icons = {
            "", "", "", "", "", "", "", ""
        };
        for (int i = 0; i < menuItems.length; i++) {
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            buttonPanel.setOpaque(false);
            JButton button = createMenuButton(menuItems[i], icons[i]);
            buttonPanel.add(button);
            panel.add(buttonPanel);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        return panel;
    }

    private JButton createMenuButton(String text, String icon) {
        JButton button = new JButton(icon + "  " + text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(isDarkMode ? new Color(60, 60, 60) : new Color(240, 240, 240));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
            }
        });
        button.addActionListener(e -> {
            switch (text) {
                case "Dashboard Home":
                    showWelcomeMessage();
                    break;
                case "Manage Librarians":
                    showLibrarianManagement();
                    break;
                case "View Reports":
                    showReports();
                    break;
                case "Fine Management":
                    showFineManagement();
                    break;
                case "User Approvals":
                    showUserApprovals();
                    break;
                case "System Settings":
                    showSettings();
                    break;
                case "Toggle Theme":
                    toggleTheme();
                    break;
                case "Logout":
                    logout();
                    break;
            }
        });
        return button;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        // Apply theme to menu panel
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        
        // Apply theme to content panel
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        // Update button colors
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            }
        }

        // Update status bar
        statusLabel.setBackground(isDarkMode ? darkBackground : lightBackground);
        statusLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);

        // Refresh the UI
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        System.out.println("userName used in welcome: " + adminName);
        JLabel welcomeLabel = new JLabel("Welcome, " + adminName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        welcomePanel.add(welcomeLabel, gbc);
        JPanel statsPanel = createStatsPanel();
        gbc.gridy = 1;
        welcomePanel.add(statsPanel, gbc);
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        quickActionsPanel.setOpaque(false);
        quickActionsPanel.setMaximumSize(new Dimension(800, 200));
        quickActionsPanel.add(createQuickActionCard("Manage Librarians", "", "Add, edit, or remove librarians"));
        quickActionsPanel.add(createQuickActionCard("View Reports", "", "View system reports"));
        quickActionsPanel.add(createQuickActionCard("User Approvals", "", "Approve or reject users"));
        quickActionsPanel.add(createQuickActionCard("System Settings", "", "Configure system settings"));
        gbc.gridy = 2;
        welcomePanel.add(quickActionsPanel, gbc);
        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Welcome to Dashboard");
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 20, 20));
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.add(createStatCard("Total Users", getTotalUsers()));
        panel.add(createStatCard("Total Books", getTotalBooks()));
        panel.add(createStatCard("Active Loans", getActiveLoanCount()));
        panel.add(createStatCard("Pending Approvals", getPendingApprovals()));
        return panel;
    }

    private JPanel createStatCard(String title, int value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(isDarkMode ? darkBackground : lightBackground);
        card.setBorder(new LineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY, 1, true));
        card.setPreferredSize(new Dimension(200, 100));

        // Icon selection based on title
        String iconPath = null;
        switch (title) {
            case "Total Users":
                iconPath = "java/src/images/active_users.png";
                break;
            case "Total Books":
                iconPath = "java/src/images/books.jpeg";
                break;
            case "Active Loans":
                iconPath = "java/src/images/over_due.jpg";
                break;
            case "Pending Approvals":
                iconPath = "java/src/images/pending_fines.png";
                break;
        }
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(iconPath);
                Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(img));
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                topPanel.add(iconLabel);
            } catch (Exception e) {
                // Ignore icon if not found
            }
        }
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);

        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createQuickActionCard(String title, String icon, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
        card.setBorder(new LineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY, 1, true));
        card.setPreferredSize(new Dimension(200, 100));
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        topPanel.add(iconLabel);
        topPanel.add(titleLabel);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        centerPanel.add(descLabel);
        card.add(topPanel, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch(title) {
                    case "Manage Librarians":
                        showLibrarianManagement();
                        break;
                    case "View Reports":
                        showReports();
                        break;
                    case "User Approvals":
                        showUserApprovals();
                        break;
                    case "System Settings":
                        showSettings();
                        break;
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(isDarkMode ? new Color(60, 60, 60) : new Color(240, 240, 240));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
            }
        });
        return card;
    }

    private void showLibrarianManagement() {
        contentPanel.removeAll();
        LibrarianManagementPanel panel = new LibrarianManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Librarians");
    }

    private void showReports() {
        contentPanel.removeAll();
        ReportsPanel panel = new ReportsPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Viewing Reports");
    }

    private void showFineManagement() {
        contentPanel.removeAll();
        FineManagementPanel panel = new FineManagementPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing Fines");
    }

    private void showUserApprovals() {
        contentPanel.removeAll();
        UserApprovalPanel panel = new UserApprovalPanel();
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("Managing User Approvals");
    }

    private void showSettings() {
        contentPanel.removeAll();
        SettingsPanel panel = new SettingsPanel(userId);
        panel.setBackground(isDarkMode ? darkBackground : lightBackground);
        contentPanel.add(panel);
        contentPanel.revalidate();
        contentPanel.repaint();
        updateStatus("System Settings");
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginScreen().setVisible(true);
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void loadPendingApprovalsCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) FROM users WHERE is_active = false";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next() && rs.getInt(1) > 0) {
                updateStatus("You have " + rs.getInt(1) + " pending user approvals");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Database utility methods
    private int getTotalUsers() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE is_active = true");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getTotalBooks() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM books WHERE is_active = true");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getActiveLoanCount() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(*) FROM book_borrowings WHERE status = 'BORROWED'"
            );
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getPendingApprovals() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE is_active = false");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
