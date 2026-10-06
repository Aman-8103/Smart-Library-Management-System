import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;

public class SignUpScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox;
    private RoundedPanel signupPanel;
    private GradientPanel mainPanel;

    public SignUpScreen() {
        setTitle("Library Management System");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        // Create nav bar
        JPanel navBar = new JPanel(new GridBagLayout());
        navBar.setBackground(Color.WHITE);
        navBar.setPreferredSize(new Dimension(getWidth(), 70));
        try {
            ImageIcon logoIcon = new ImageIcon("icons/upeslogo.jpg");
            Image logoImg = logoIcon.getImage().getScaledInstance(200, 50, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
            navBar.add(logoLabel);
        } catch (Exception e) {
            navBar.add(new JLabel("LOGO"));
        }

        // Create main panel with gradient
        mainPanel = new GradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        // Title and subtitle
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Join our Digital Library Community");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(230, 230, 230));

        // Create signup panel
        createSignupPanel();
        // signupPanel.setPreferredSize(new Dimension(400, 500)); // Removed to allow scrolling

        // Wrap signupPanel in JScrollPane
        JScrollPane signupScrollPane = new JScrollPane(signupPanel);
        signupScrollPane.setPreferredSize(new Dimension(400, 500));
        signupScrollPane.setBorder(null);
        signupScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        signupScrollPane.getVerticalScrollBar().setUnitIncrement(16);

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
        mainPanel.add(signupScrollPane, gbc);

        // Set layout and add nav bar and main panel
        setLayout(new BorderLayout());
        add(navBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void createSignupPanel() {
        signupPanel = new RoundedPanel(20, new Color(255, 255, 255, 220));
        signupPanel.setLayout(new GridBagLayout());
        signupPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Full Name
        gbc.gridy = 0;
        JLabel fullNameLabel = new JLabel("Full Name:");
        signupPanel.add(fullNameLabel, gbc);

        gbc.gridy++;
        fullNameField = new JTextField(20);
        styleTextField(fullNameField);
        signupPanel.add(fullNameField, gbc);

        // Username
        gbc.gridy++;
        JLabel usernameLabel = new JLabel("Username:");
        signupPanel.add(usernameLabel, gbc);

        gbc.gridy++;
        usernameField = new JTextField(20);
        styleTextField(usernameField);
        signupPanel.add(usernameField, gbc);

        // Email
        gbc.gridy++;
        JLabel emailLabel = new JLabel("Email:");
        signupPanel.add(emailLabel, gbc);

        gbc.gridy++;
        emailField = new JTextField(20);
        styleTextField(emailField);
        signupPanel.add(emailField, gbc);

        // Password
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        signupPanel.add(passwordLabel, gbc);

        gbc.gridy++;
        passwordField = new JPasswordField(20);
        styleTextField(passwordField);
        signupPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridy++;
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        signupPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy++;
        confirmPasswordField = new JPasswordField(20);
        styleTextField(confirmPasswordField);
        signupPanel.add(confirmPasswordField, gbc);

        // Role Selection
        gbc.gridy++;
        JLabel roleLabel = new JLabel("Role:");
        signupPanel.add(roleLabel, gbc);

        gbc.gridy++;
        String[] roles = {"Student", "Librarian"};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);
        signupPanel.add(roleComboBox, gbc);

        // Sign Up Button
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 10, 0);
        gbc.weightx = 0;
        CustomButton signUpButton = new CustomButton("SIGN UP", new Color(46, 204, 113), false);
        signUpButton.addActionListener(e -> handleSignUp());
        signupPanel.add(signUpButton, gbc);

        // Back to Login Button
        gbc.gridy++;
        CustomButton backButton = new CustomButton("Back to Login", new Color(41, 128, 185), false);
        backButton.addActionListener(e -> {
            new LoginScreen().setVisible(true);
            this.dispose();
        });
        signupPanel.add(backButton, gbc);
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

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        // Validation
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!");
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Check if username already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            // Check if email already exists
            checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Email already registered!");
                return;
            }

            // Insert new user
            String insertQuery = "INSERT INTO users (username, password, email, full_name, role, is_active) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, fullName);
            pstmt.setString(5, role.toUpperCase());
            pstmt.setBoolean(6, false);  // Account needs admin approval
            
            pstmt.executeUpdate();

            // Create notification for admin
            String notifyQuery = "INSERT INTO notifications (user_id, message, type) " +
                               "SELECT user_id, ?, 'APPROVAL' FROM users WHERE role = 'ADMIN'";
            PreparedStatement notifyStmt = conn.prepareStatement(notifyQuery);
            notifyStmt.setString(1, "New " + role + " account registration: " + username);
            notifyStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, 
                "Account created successfully!\nPlease wait for admin approval to login.");
            
            // Return to login screen
            new LoginScreen().setVisible(true);
            this.dispose();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating account: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}
