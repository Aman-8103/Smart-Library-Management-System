import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IssuedBooksPanel extends JPanel {
    private int userId;
    private boolean isDarkMode;
    private Color darkBackground = new Color(33, 33, 33);
    private Color lightBackground = new Color(242, 242, 242);

    public IssuedBooksPanel(int userId, boolean isDarkMode) {
        this.userId = userId;
        this.isDarkMode = isDarkMode;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(isDarkMode ? darkBackground : lightBackground);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add title
        JLabel titleLabel = new JLabel("Issued Books", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        // Create table
        String[] columns = {"Book ID", "Title", "Student ID", "Student Name", "Issue Date", "Due Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable issuedBooksTable = new JTable(model);
        issuedBooksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        issuedBooksTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        issuedBooksTable.setRowHeight(25);
        issuedBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Add search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        
        JTextField searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));
        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(70, 130, 180));
        searchButton.setForeground(Color.WHITE);
        
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Add filter options
        String[] filterOptions = {"All", "Currently Issued", "Overdue", "Returned"};
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Filter: "));
        searchPanel.add(filterCombo);

        add(searchPanel, BorderLayout.NORTH);

        // Load issued books
        loadIssuedBooks(model, "All");

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(issuedBooksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            String filterStatus = (String) filterCombo.getSelectedItem();
            searchIssuedBooks(model, searchText, filterStatus);
        });

        filterCombo.addActionListener(e -> {
            String filterStatus = (String) filterCombo.getSelectedItem();
            loadIssuedBooks(model, filterStatus);
        });

        // Add statistics panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(isDarkMode ? darkBackground : lightBackground);
        updateStatistics(statsPanel);
        add(statsPanel, BorderLayout.SOUTH);
    }

    private void loadIssuedBooks(DefaultTableModel model, String filter) {
        model.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT b.book_id, b.title, bb.user_id, u.full_name, " +
                        "bb.borrow_date, bb.due_date, bb.status " +
                        "FROM book_borrowings bb " +
                        "JOIN books b ON bb.book_id = b.book_id " +
                        "JOIN users u ON bb.user_id = u.user_id ";

            if (!filter.equals("All")) {
                sql += "WHERE bb.status = ? ";
            }
            sql += "ORDER BY bb.borrow_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            if (!filter.equals("All")) {
                stmt.setString(1, filter.toUpperCase());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading issued books: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchIssuedBooks(DefaultTableModel model, String searchText, String filter) {
        model.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String sql = "SELECT b.book_id, b.title, bb.user_id, u.full_name, " +
                        "bb.borrow_date, bb.due_date, bb.status " +
                        "FROM book_borrowings bb " +
                        "JOIN books b ON bb.book_id = b.book_id " +
                        "JOIN users u ON bb.user_id = u.user_id " +
                        "WHERE (b.title LIKE ? OR u.full_name LIKE ?) ";

            if (!filter.equals("All")) {
                sql += "AND bb.status = ? ";
            }
            sql += "ORDER BY bb.borrow_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, "%" + searchText + "%");
            if (!filter.equals("All")) {
                stmt.setString(3, filter.toUpperCase());
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                };
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error searching issued books: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatistics(JPanel statsPanel) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            
            // Get total issued books
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'BORROWED' THEN 1 ELSE 0 END) as current, " +
                "SUM(CASE WHEN status = 'BORROWED' AND due_date < CURRENT_DATE THEN 1 ELSE 0 END) as overdue " +
                "FROM book_borrowings"
            );
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                JLabel totalLabel = new JLabel("Total Issues: " + rs.getInt("total"));
                JLabel currentLabel = new JLabel("Currently Issued: " + rs.getInt("current"));
                JLabel overdueLabel = new JLabel("Overdue: " + rs.getInt("overdue"));
                
                totalLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                currentLabel.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                overdueLabel.setForeground(Color.RED);
                
                statsPanel.add(totalLabel);
                statsPanel.add(Box.createHorizontalStrut(20));
                statsPanel.add(currentLabel);
                statsPanel.add(Box.createHorizontalStrut(20));
                statsPanel.add(overdueLabel);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
