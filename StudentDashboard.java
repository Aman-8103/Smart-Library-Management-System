import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class StudentDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private Color accentColor = new Color(70, 130, 180);
    private JPanel menuPanel;
    private String userName;

    public StudentDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Student Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Get user name from database
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT name FROM users WHERE id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                userName = rs.getString("name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            userName = "Student";
        }

        // Create split pane
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBorder(null);
        
        // Create menu panel
        menuPanel = createMenuPanel();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        
        showWelcomeMessage();

        splitPane.setLeftComponent(menuPanel);
        splitPane.setRightComponent(contentPanel);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(1);

        add(splitPane);
        applyTheme();
    }

    private JPanel createMenuPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Profile section
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setOpaque(false);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Profile icon (centered)
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setMaximumSize(new Dimension(60, 60));
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setBackground(accentColor);
        JLabel iconLabel = new JLabel(String.valueOf(userName.charAt(0)).toUpperCase());
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User info (centered)
        JLabel nameLabel = new JLabel(userName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Student");
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
            "Borrow Books",
            "Return Books",
            "View Status",
            "Request Books",
            "Notifications",
            "Toggle Theme",
            "Logout"
        };

        String[] icons = {
            "", "", "", "", "", "", ""
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
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
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
            switch(text) {
                case "Borrow Books":
                    showBorrowBooks();
                    break;
                case "Return Books":
                    showReturnBooks();
                    break;
                case "View Status":
                    showStatus();
                    break;
                case "Request Books":
                    showRequestBooks();
                    break;
                case "Notifications":
                    showNotifications();
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
            } else if (c instanceof JPanel) {
                // Update profile section
                JPanel panel = (JPanel) c;
                panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                    }
                }
            }
        }

        // Refresh the UI
        SwingUtilities.updateComponentTreeUI(this);
    }

    private void showWelcomeMessage() {
        contentPanel.removeAll();
        
        JPanel welcomePanel = new JPanel(new GridBagLayout());
        welcomePanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 20);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, " + userName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        welcomePanel.add(welcomeLabel, gbc);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(800, 150));

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Borrowed books card
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings WHERE user_id = ? AND status = 'BORROWED'"
            );
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Borrowed Books", String.valueOf(rs.getInt(1))));
            }

            // Pending requests card
            stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_requests WHERE user_id = ? AND status = 'PENDING'"
            );
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Pending Requests", String.valueOf(rs.getInt(1))));
            }

            // Overdue books card
            stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM book_borrowings WHERE user_id = ? AND status = 'BORROWED' AND return_date < CURRENT_DATE"
            );
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Overdue Books", String.valueOf(rs.getInt(1))));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        gbc.insets = new Insets(40, 20, 20, 20);
        welcomePanel.add(statsPanel, gbc);

        // Quick actions
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        quickActionsPanel.setOpaque(false);
        quickActionsPanel.setMaximumSize(new Dimension(800, 200));

        quickActionsPanel.add(createQuickActionCard("Borrow Books", "", "Borrow new books"));
        quickActionsPanel.add(createQuickActionCard("Return Books", "", "Return borrowed books"));
        quickActionsPanel.add(createQuickActionCard("View Status", "", "Check your status"));
        quickActionsPanel.add(createQuickActionCard("Request Books", "", "Request new books"));

        gbc.insets = new Insets(20, 20, 20, 20);
        welcomePanel.add(quickActionsPanel, gbc);

        contentPanel.add(welcomePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createStatCard(String icon, String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
        card.setBorder(new LineBorder(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY, 1, true));
        card.setPreferredSize(new Dimension(200, 150));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        topPanel.add(iconLabel);
        topPanel.add(titleLabel);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);
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
                    case "Borrow Books":
                        showBorrowBooks();
                        break;
                    case "Return Books":
                        showReturnBooks();
                        break;
                    case "View Status":
                        showStatus();
                        break;
                    case "Request Books":
                        showRequestBooks();
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

    private void showBorrowBooks() {
        contentPanel.removeAll();
        BorrowBooksPanel borrowPanel = new BorrowBooksPanel(userId);
        contentPanel.add(borrowPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReturnBooks() {
        contentPanel.removeAll();
        try {
            ReturnBooksPanel returnPanel = new ReturnBooksPanel(userId, isDarkMode);
            contentPanel.add(returnPanel);
        } catch (Exception ex) {
            JPanel errorPanel = new JPanel(new GridBagLayout());
            errorPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
            
            JLabel errorLabel = new JLabel("Error loading return books panel: " + ex.getMessage());
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            
            errorPanel.add(errorLabel);
            contentPanel.add(errorPanel);
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void showErrorMessage(String message) {
        JPanel errorPanel = new JPanel(new GridBagLayout());
        errorPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        JLabel errorLabel = new JLabel(message);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        errorPanel.add(errorLabel);
        contentPanel.add(errorPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showStatus() {
        contentPanel.removeAll();
        StatusPanel statusPanel = new StatusPanel(userId);
        contentPanel.add(statusPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showRequestBooks() {
        contentPanel.removeAll();
        RequestBooksPanel requestPanel = new RequestBooksPanel(userId);
        contentPanel.add(requestPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNotifications() {
        contentPanel.removeAll();
        NotificationPanel notificationPanel = new NotificationPanel(userId, isDarkMode);    
        contentPanel.add(notificationPanel);
        contentPanel.revalidate();
        contentPanel.repaint();
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
}
