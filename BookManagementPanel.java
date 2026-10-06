import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookManagementPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, categoryField, quantityField, isbnField;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);

    public BookManagementPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        initializeComponents();
        loadBooks();
    }

    private void initializeComponents() {
        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        // Create text fields
        titleField = new JTextField(20);
        authorField = new JTextField(20);
        categoryField = new JTextField(20);
        quantityField = new JTextField(20);
        isbnField = new JTextField(20);

        // Add components to input panel
        addLabelAndField(inputPanel, "Title:", titleField);
        addLabelAndField(inputPanel, "Author:", authorField);
        addLabelAndField(inputPanel, "Category:", categoryField);
        addLabelAndField(inputPanel, "Quantity:", quantityField);
        addLabelAndField(inputPanel, "ISBN:", isbnField);

        // Create table
        tableModel = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "Category", "Quantity", "Available"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(booksTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JButton addButton = createButton("Add Book");
        JButton updateButton = createButton("Update Book");
        JButton deleteButton = createButton("Delete Book");
        JButton clearButton = createButton("Clear Fields");

        addButton.addActionListener(e -> addBook());
        updateButton.addActionListener(e -> updateBook());
        deleteButton.addActionListener(e -> deleteBook());
        clearButton.addActionListener(e -> clearFields());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add table selection listener
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = booksTable.getSelectedRow();
                if (row != -1) {
                    titleField.setText((String) tableModel.getValueAt(row, 1));
                    authorField.setText((String) tableModel.getValueAt(row, 2));
                    categoryField.setText((String) tableModel.getValueAt(row, 3));
                    quantityField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
                }
            }
        });
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        panel.add(label);
        panel.add(field);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM books WHERE is_active = 1";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getInt("available_quantity")
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (SQLException ex) {
            showError("Error loading books: " + ex.getMessage());
        }
    }

    private void addBook() {
        if (!validateInputs()) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO books (title, author, category, quantity, available_quantity, isbn, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                stmt.setString(1, titleField.getText().trim());
                stmt.setString(2, authorField.getText().trim());
                stmt.setString(3, categoryField.getText().trim());
                stmt.setInt(4, quantity);
                stmt.setInt(5, quantity);
                stmt.setString(6, isbnField.getText().trim());
                
                stmt.executeUpdate();
                showSuccess("Book added successfully");
                clearFields();
                loadBooks();
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                showError("A book with this ISBN already exists");
            } else {
                showError("Error adding book: " + ex.getMessage());
            }
        } catch (NumberFormatException ex) {
            showError("Quantity must be a number");
        }
    }

    private void updateBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) {
            showError("Please select a book to update");
            return;
        }

        if (!validateInputs()) return;

        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE books SET title = ?, author = ?, category = ?, quantity = ?, " +
                        "available_quantity = available_quantity + (? - quantity) WHERE book_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int bookId = (Integer) tableModel.getValueAt(row, 0);
                int newQuantity = Integer.parseInt(quantityField.getText().trim());
                
                stmt.setString(1, titleField.getText().trim());
                stmt.setString(2, authorField.getText().trim());
                stmt.setString(3, categoryField.getText().trim());
                stmt.setInt(4, newQuantity);
                stmt.setInt(5, newQuantity);
                stmt.setInt(6, bookId);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    showSuccess("Book updated successfully");
                    clearFields();
                    loadBooks();
                }
            }
        } catch (SQLException ex) {
            showError("Error updating book: " + ex.getMessage());
        }
    }

    private void deleteBook() {
        int row = booksTable.getSelectedRow();
        if (row == -1) {
            showError("Please select a book to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this book?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                String sql = "UPDATE books SET is_active = 0 WHERE book_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    int bookId = (Integer) tableModel.getValueAt(row, 0);
                    stmt.setInt(1, bookId);
                    
                    int result = stmt.executeUpdate();
                    if (result > 0) {
                        showSuccess("Book deleted successfully");
                        clearFields();
                        loadBooks();
                    }
                }
            } catch (SQLException ex) {
                showError("Error deleting book: " + ex.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Title is required");
            return false;
        }
        if (authorField.getText().trim().isEmpty()) {
            showError("Author is required");
            return false;
        }
        if (isbnField.getText().trim().isEmpty()) {
            showError("ISBN is required");
            return false;
        }
        if (quantityField.getText().trim().isEmpty()) {
            showError("Quantity is required");
            return false;
        }
        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                showError("Quantity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException ex) {
            showError("Quantity must be a number");
            return false;
        }
        return true;
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        categoryField.setText("");
        quantityField.setText("");
        isbnField.setText("");
        booksTable.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}