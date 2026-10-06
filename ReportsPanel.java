import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportsPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JTable booksTable, borrowingsTable, finesTable;
    private DefaultTableModel booksModel, borrowingsModel, finesModel;
    private JComboBox<String> reportTypeCombo;
    private JButton generateButton, exportButton;

    public ReportsPanel() {
        setLayout(new BorderLayout());
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        createTables();
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load initial data
        loadReports("ALL");
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.add(new JLabel("Report Type:"));
        String[] reportTypes = {"All", "Books", "Borrowings", "Fines"};
        reportTypeCombo = new JComboBox<>(reportTypes);
        panel.add(reportTypeCombo);
        
        generateButton = new JButton("Generate Report");
        generateButton.addActionListener(e -> loadReports(
            (String) reportTypeCombo.getSelectedItem()));
        panel.add(generateButton);
        
        exportButton = new JButton("Export to Excel");
        exportButton.addActionListener(e -> exportToExcel());
        panel.add(exportButton);
        
        return panel;
    }

    private void createTables() {
        // Books table
        String[] bookColumns = {"Book ID", "ISBN", "Title", "Author", "Total Copies", "Available"};
        booksModel = new DefaultTableModel(bookColumns, 0);
        booksTable = new JTable(booksModel);
        tabbedPane.addTab("Books", new JScrollPane(booksTable));
        
        // Borrowings table
        String[] borrowColumns = {"Borrowing ID", "Book Title", "Student Name", "Borrow Date", "Due Date", "Status"};
        borrowingsModel = new DefaultTableModel(borrowColumns, 0);
        borrowingsTable = new JTable(borrowingsModel);
        tabbedPane.addTab("Borrowings", new JScrollPane(borrowingsTable));
        
        // Fines table
        String[] fineColumns = {"Fine ID", "Student Name", "Book Title", "Amount", "Status", "Date"};
        finesModel = new DefaultTableModel(fineColumns, 0);
        finesTable = new JTable(finesModel);
        tabbedPane.addTab("Fines", new JScrollPane(finesTable));
    }

    private void loadReports(String type) {
        switch (type.toUpperCase()) {
            case "ALL":
                loadBooksReport();
                loadBorrowingsReport();
                loadFinesReport();
                break;
            case "BOOKS":
                loadBooksReport();
                break;
            case "BORROWINGS":
                loadBorrowingsReport();
                break;
            case "FINES":
                loadFinesReport();
                break;
        }
    }

    private void loadBooksReport() {
        booksModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM books WHERE is_active = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("book_id"),
                    rs.getString("isbn"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getInt("quantity"),
                    rs.getInt("available_quantity")
                };
                booksModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books report: " + e.getMessage());
        }
    }

    private void loadBorrowingsReport() {
        borrowingsModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT bb.borrow_id, b.title, u.full_name, " +
                          "bb.borrow_date, bb.due_date, bb.status " +
                          "FROM book_borrowings bb " +
                          "JOIN books b ON bb.book_id = b.book_id " +
                          "JOIN users u ON bb.user_id = u.user_id " +
                          "ORDER BY bb.borrow_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("borrow_id"),
                    rs.getString("title"),
                    rs.getString("full_name"),
                    rs.getDate("borrow_date"),
                    rs.getDate("due_date"),
                    rs.getString("status")
                };
                borrowingsModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading borrowings report: " + e.getMessage());
        }
    }

    private void loadFinesReport() {
        finesModel.setRowCount(0);
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT f.fine_id, u.full_name, b.title, " +
                          "f.amount, f.status, f.created_at " +
                          "FROM fines f " +
                          "JOIN book_borrowings bb ON f.borrow_id = bb.borrow_id " +
                          "JOIN users u ON bb.user_id = u.user_id " +
                          "JOIN books b ON bb.book_id = b.book_id " +
                          "ORDER BY f.created_at DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("fine_id"),
                    rs.getString("full_name"),
                    rs.getString("title"),
                    String.format("$%.2f", rs.getDouble("amount")),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                };
                finesModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading fines report: " + e.getMessage());
        }
    }

    private void exportToExcel() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Excel File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getPath();
                if (!filePath.endsWith(".xlsx")) {
                    filePath += ".xlsx";
                }
                
                // Here you would implement the actual Excel export
                // Using libraries like Apache POI or similar
                JOptionPane.showMessageDialog(this, 
                    "Export functionality to be implemented.\nFile will be saved to: " + filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting to Excel: " + e.getMessage());
        }
    }
}