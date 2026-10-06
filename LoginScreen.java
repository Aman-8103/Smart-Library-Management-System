import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private RoundedPanel loginPanel;
    private GradientPanel mainPanel;

    public LoginScreen() {
        setTitle("Library Management System");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Create nav bar
        JPanel navBar = new JPanel(new GridBagLayout());
        navBar.setBackground(Color.WHITE);
        navBar.setPreferredSize(new Dimension(getWidth(), 70)); // Adjust height as needed
        try {
            ImageIcon logoIcon = new ImageIcon("icons/upeslogo.jpg");
            Image logoImg = logoIcon.getImage().getScaledInstance(200, 50, Image.SCALE_SMOOTH); // Expanded horizontally
            JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
            navBar.add(logoLabel);
        } catch (Exception e) {
            navBar.add(new JLabel("LOGO"));
        }

        // Create main panel with gradient
        mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        // Title and subtitle (outside login panel)
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Welcome to the Digital Library Portal");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(230, 230, 230));

        // Create login panel
        createLoginPanel();
        loginPanel.setPreferredSize(new Dimension(400, 400)); // Adjust as needed

        // Add components to main panel with vertical spacing
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(titleLabel, gbc);

        gbc.gridy++;
        mainPanel.add(subtitleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(30, 0, 0, 0);
        mainPanel.add(loginPanel, gbc);

        // Set layout and add nav bar and main panel
        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void createLoginPanel() {
        loginPanel = new RoundedPanel(20, new Color(255, 255, 255, 220));
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Username Label
        gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        loginPanel.add(userLabel, gbc);

        // Username Field
        gbc.gridy++;
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        loginPanel.add(usernameField, gbc);

        // Password Label
        gbc.gridy++;
        JLabel passLabel = new JLabel("Password:");
        loginPanel.add(passLabel, gbc);

        // Password Field
        gbc.gridy++;
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        loginPanel.add(passwordField, gbc);

        // Role Label
        gbc.gridy++;
        JLabel roleLabel = new JLabel("Role:");
        loginPanel.add(roleLabel, gbc);

        // Role ComboBox
        gbc.gridy++;
        String[] roles = {"Admin", "Librarian", "Student"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);
        loginPanel.add(roleComboBox, gbc);

        // Login Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        gbc.weightx = 0;
        CustomButton loginButton = new CustomButton("LOGIN", new Color(41, 128, 185), false);
        loginButton.addActionListener(e -> handleLogin());
        loginPanel.add(loginButton, gbc);

        // Sign Up Button
        gbc.gridy++;
        CustomButton signUpButton = new CustomButton("Create New Account", new Color(46, 204, 113), false);
        signUpButton.addActionListener(e -> openSignUpScreen());
        loginPanel.add(signUpButton, gbc);
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Library Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        return titleLabel;
    }

    private JPanel createInputPanel(String labelText, String iconName) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        
        // Add icon
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/" + iconName));
            Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            panel.add(iconLabel);
        } catch (Exception e) {
            JLabel label = new JLabel(labelText + ":");
            label.setForeground(new Color(44, 62, 80));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(label);
        }
        
        return panel;
    }

    private void styleTextField(JTextField textField) {
        textField.setPreferredSize(new Dimension(250, 35));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(189, 195, 199), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        textField.setBackground(new Color(255, 255, 255));
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setPreferredSize(new Dimension(250, 35));
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(new LineBorder(new Color(189, 195, 199), 1, true));
        comboBox.setBackground(Color.WHITE);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ? AND is_active = true";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                showSuccess("Login successful!");
                openDashboard(userId, role);
            } else {
                showError("Invalid credentials or account not activated!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database error occurred: " + ex.getMessage());
        }
    }

    private void openSignUpScreen() {
        this.dispose();
        new SignUpScreen().setVisible(true);
    }

    private void openDashboard(int userId, String role) {
        this.dispose();
        switch (role.toUpperCase()) {
            case "ADMIN":
                new AdminDashboard(userId).setVisible(true);
                break;
            case "LIBRARIAN":
                new LibrarianDashboard(userId).setVisible(true);
                break;
            case "STUDENT":
                new StudentDashboard(userId).setVisible(true);
                break;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginScreen().setVisible(true);
   });}
}
