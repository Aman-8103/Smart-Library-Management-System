import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentRecordsPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);
    private DefaultTableModel tableModel;
    private JTable studentsTable;

    public StudentRecordsPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create title panel
        createTitlePanel();

        // Create search panel
        createSearchPanel();

        // Create table
        createTable();

        // Create buttons panel
        createButtonsPanel();
    }

    private void createTitlePanel() {
        JLabel titleLabel = new JLabel("Student Records", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);
    }

    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);

        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            searchStudents(searchText);
        });

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        add(searchPanel, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {
            "Student ID", 
            "Full Name", 
            "Email", 
            "Books Borrowed", 
            "Books Returned",
            "Status"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentsTable = new JTable(tableModel);
        studentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentsTable.setRowHeight(25);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Set column widths
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        studentsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        studentsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        studentsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        studentsTable.getColumnModel().getColumn(5).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load initial data
        loadStudentData();
    }

    private void createButtonsPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(isDarkMode ? darkBackground : lightBackground);

        JButton viewDetailsButton = new JButton("View Details");
        JButton viewHistoryButton = new JButton("View History");

        // Style buttons
        for (JButton button : new JButton[]{viewDetailsButton, viewHistoryButton}) {
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            buttonPanel.add(button);
        }

        // Add button actions
        viewDetailsButton.addActionListener(e -> viewStudentDetails());
        viewHistoryButton.addActionListener(e -> viewStudentHistory());

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadStudentData() {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = 
                "SELECT u.user_id, u.full_name, u.email, u.is_active, " +
                "(SELECT COUNT(*) FROM book_borrowings WHERE user_id = u.user_id) as total_borrowed, " +
                "(SELECT COUNT(*) FROM book_borrowings WHERE user_id = u.user_id AND status = 'RETURNED') as total_returned " +
                "FROM users u WHERE u.role = 'STUDENT'";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getInt("total_borrowed"),
                    rs.getInt("total_returned"),
                    rs.getBoolean("is_active") ? "Active" : "Inactive"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading student data: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudents(String searchText) {
        tableModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = 
                "SELECT u.user_id, u.full_name, u.email, u.is_active, " +
                "(SELECT COUNT(*) FROM book_borrowings WHERE user_id = u.user_id) as total_borrowed, " +
                "(SELECT COUNT(*) FROM book_borrowings WHERE user_id = u.user_id AND status = 'RETURNED') as total_returned " +
                "FROM users u WHERE u.role = 'STUDENT' AND " +
                "(u.full_name LIKE ? OR u.email LIKE ? OR CAST(u.user_id AS VARCHAR) LIKE ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchText + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getInt("total_borrowed"),
                    rs.getInt("total_returned"),
                    rs.getBoolean("is_active") ? "Active" : "Inactive"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error searching students: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewStudentDetails() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a student to view details",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        showStudentDetailsDialog(studentId);
    }

    private void viewStudentHistory() {
        int selectedRow = studentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a student to view history",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int studentId = (int) tableModel.getValueAt(selectedRow, 0);
        showStudentHistoryDialog(studentId);
    }

    private void showStudentDetailsDialog(int studentId) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE user_id = ?"
            );
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JDialog dialog = new JDialog();
                dialog.setTitle("Student Details");
                dialog.setLayout(new BorderLayout(10, 10));
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(this);

                JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                addDetailField(detailsPanel, "Student ID:", String.valueOf(rs.getInt("user_id")));
                addDetailField(detailsPanel, "Full Name:", rs.getString("full_name"));
                addDetailField(detailsPanel, "Email:", rs.getString("email"));
                addDetailField(detailsPanel, "Status:", rs.getBoolean("is_active") ? "Active" : "Inactive");

                dialog.add(detailsPanel, BorderLayout.CENTER);
                dialog.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading student details: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStudentHistoryDialog(int studentId) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Borrowing History");
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columns = {"Book Title", "Borrow Date", "Return Date", "Status"};
        DefaultTableModel historyModel = new DefaultTableModel(columns, 0);
        JTable historyTable = new JTable(historyModel);

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT b.title, bb.borrow_date, bb.return_date, bb.status " +
                "FROM book_borrowings bb " +
                "JOIN books b ON bb.book_id = b.book_id " +
                "WHERE bb.user_id = ? " +
                "ORDER BY bb.borrow_date DESC"
            );
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("title"),
                    rs.getDate("borrow_date"),
                    rs.getDate("return_date"),
                    rs.getString("status")
                };
                historyModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading borrowing history: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }

        dialog.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void addDetailField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        JLabel valueComponent = new JLabel(value);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(labelComponent);
        panel.add(valueComponent);
    }
}
