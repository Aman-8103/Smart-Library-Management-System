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

public class LibrarianDashboard extends JFrame {
    private int userId;
    private JPanel contentPanel;
    private boolean isDarkMode = false;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private Color darkMenuBackground = new Color(50, 50, 50);
    private Color lightMenuBackground = new Color(230, 230, 230);
    private Color primaryColor = new Color(70, 130, 180);
    private JPanel menuPanel;
    private String librarianName;

    public LibrarianDashboard(int userId) {
        this.userId = userId;
        setTitle("Library Management System - Librarian Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Get librarian name from database
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT full_name, username FROM users WHERE user_id = ?");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                librarianName = rs.getString("full_name");
                if (librarianName == null || librarianName.trim().isEmpty()) {
                    librarianName = rs.getString("username");
                }
                if (librarianName == null || librarianName.trim().isEmpty()) {
                    librarianName = "Librarian";
                }
            } else {
                librarianName = "Librarian";
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            librarianName = "Librarian";
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
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(1);

        add(splitPane);
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

        // Profile icon (centered)
        JPanel iconPanel = new JPanel(new GridBagLayout());
        iconPanel.setMaximumSize(new Dimension(60, 60));
        iconPanel.setPreferredSize(new Dimension(60, 60));
        iconPanel.setBackground(primaryColor);
        JLabel iconLabel = new JLabel(String.valueOf(librarianName.charAt(0)).toUpperCase());
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        iconLabel.setForeground(Color.WHITE);
        iconPanel.add(iconLabel);
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // User info (centered)
        JLabel nameLabel = new JLabel(librarianName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roleLabel = new JLabel("Librarian");
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
            "Manage Books",
            "Issue Books",
            "View Issued Books",
            "Return Books",
            "Student Records",
            "Notifications",
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
        button.addActionListener(e -> handleMenuClick(text));
        return button;
    }

    private void handleMenuClick(String menuItem) {
        switch(menuItem) {
            case "Manage Books":
                showBookManagement();
                break;
            case "Issue Books":
                showIssueBooks();
                break;
            case "View Issued Books":
                showIssuedBooks();
                break;
            case "Return Books":
                showReturnBooks();
                break;
            case "Student Records":
                showStudentRecords();
                break;
            case "Notifications":
                showNotifications();
                break;
            case "Toggle Theme":
                isDarkMode = !isDarkMode;
                applyTheme();
                break;
            case "Logout":
                handleLogout();
                break;
        }
    }

    private void applyTheme() {
        menuPanel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
        contentPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        for (Component c : menuPanel.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setBackground(isDarkMode ? new Color(70, 70, 70) : primaryColor);
                button.setForeground(Color.WHITE);
            } else if (c instanceof JPanel) {
                JPanel panel = (JPanel) c;
                panel.setBackground(isDarkMode ? darkMenuBackground : lightMenuBackground);
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                    }
                }
            }
        }

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
        JLabel welcomeLabel = new JLabel("Welcome, " + librarianName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        welcomePanel.add(welcomeLabel, gbc);
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(800, 150));
        try {
            Connection conn = DatabaseConnection.getConnection();
            // Total books
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM books");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Total Books", String.valueOf(rs.getInt(1))));
            }
            // Issued books
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM book_borrowings WHERE status = 'BORROWED'");
            rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Books Issued", String.valueOf(rs.getInt(1))));
            }
            // Overdue books
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM book_borrowings WHERE status = 'BORROWED' AND return_date < CURRENT_DATE");
            rs = stmt.executeQuery();
            if (rs.next()) {
                statsPanel.add(createStatCard("", "Overdue Books", String.valueOf(rs.getInt(1))));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        gbc.insets = new Insets(40, 20, 20, 20);
        welcomePanel.add(statsPanel, gbc);
        JPanel quickActionsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        quickActionsPanel.setOpaque(false);
        quickActionsPanel.setMaximumSize(new Dimension(800, 200));
        quickActionsPanel.add(createQuickActionCard("Manage Books", "", "Add, edit, or remove books"));
        quickActionsPanel.add(createQuickActionCard("Issue Books", "", "Issue books to students"));
        quickActionsPanel.add(createQuickActionCard("Student Records", "", "View student records"));
        quickActionsPanel.add(createQuickActionCard("Notifications", "", "View notifications"));
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
        valueLabel.setForeground(primaryColor);
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
                    case "Manage Books":
                        showBookManagement();
                        break;
                    case "Issue Books":
                        showIssueBooks();
                        break;
                    case "Student Records":
                        showStudentRecords();
                        break;
                    case "Notifications":
                        showNotifications();
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

    private void showBookManagement() {
        contentPanel.removeAll();
        try {
            BookManagementPanel bookPanel = new BookManagementPanel(userId , isDarkMode);
            contentPanel.add(bookPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading book management panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showIssueBooks() {
        contentPanel.removeAll();
        try {
            IssueBooksPanel issuePanel = new IssueBooksPanel(userId, isDarkMode);
            contentPanel.add(issuePanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading issue books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showIssuedBooks() {
        contentPanel.removeAll();
        try {
            IssuedBooksPanel issuedBooksPanel = new IssuedBooksPanel(userId, isDarkMode);
            contentPanel.add(issuedBooksPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading issued books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showReturnBooks() {
        contentPanel.removeAll();
        try {
            ReturnBooksPanel returnPanel = new ReturnBooksPanel(userId, isDarkMode);
            contentPanel.add(returnPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading return books panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showStudentRecords() {
        contentPanel.removeAll();
        try {
            StudentRecordsPanel studentPanel = new StudentRecordsPanel(userId, isDarkMode);
            contentPanel.add(studentPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading student records panel: " + ex.getMessage());
            ex.printStackTrace();
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNotifications() {
        contentPanel.removeAll();
        try {
            NotificationPanel notificationPanel = new NotificationPanel(userId, isDarkMode);
            contentPanel.add(notificationPanel);
        } catch (Exception ex) {
            showErrorMessage("Error loading notifications panel: " + ex.getMessage());
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

    private void handleLogout() {
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