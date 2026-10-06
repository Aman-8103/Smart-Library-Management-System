import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";
    
    // Connection pool size
    private static final int MAX_RETRIES = 3;
    private static Connection connection = null;

    public static Connection getConnection() {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                // Check if connection is null or closed
                if (connection == null || connection.isClosed()) {
                    // Load the JDBC driver
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    
                    // Create new connection with timeout settings
                    connection = DriverManager.getConnection(
                        URL + "?autoReconnect=true&useSSL=false&serverTimezone=UTC",
                        USERNAME,
                        PASSWORD
                    );
                    
                    // Test the connection
                    if (connection.isValid(5)) { // 5 seconds timeout
                        return connection;
                    }
                } else if (connection.isValid(5)) {
                    return connection;
                }
            } catch (ClassNotFoundException e) {
                showError("Database driver not found. Please ensure MySQL JDBC driver is in the classpath.\nError: " + e.getMessage());
                return null;
            } catch (SQLException e) {
                retries++;
                if (retries == MAX_RETRIES) {
                    showError("Failed to connect to database after " + MAX_RETRIES + " attempts.\n" +
                             "Please check:\n" +
                             "1. MySQL server is running\n" +
                             "2. Database 'library_management' exists\n" +
                             "3. Username and password are correct\n" +
                             "4. Port 3306 is correct and available\n\n" +
                             "Error: " + e.getMessage());
                    return null;
                }
                // Wait before retrying
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return null;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            showError("Error closing database connection: " + e.getMessage());
        }
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(
            null,
            message,
            "Database Connection Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Method to check if database exists and create if it doesn't
    public static void initializeDatabase() {
        try {
            // First try to connect to MySQL without specifying database
            Connection tempConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?autoReconnect=true&useSSL=false&serverTimezone=UTC",
                USERNAME,
                PASSWORD
            );

            java.sql.Statement stmt = tempConn.createStatement();

            // Create database if it doesn't exist
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS library_db");

            // Use the database
            stmt.executeUpdate("USE library_db");

            // Create necessary tables
            createTables(stmt);

            tempConn.close();
        } catch (SQLException e) {
            showError("Error initializing database: " + e.getMessage());
        }
    }

    private static void createTables(java.sql.Statement stmt) throws SQLException {
        // Users table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "username VARCHAR(50) UNIQUE NOT NULL, " +
            "password VARCHAR(255) NOT NULL, " +
            "full_name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100) UNIQUE NOT NULL, " +
            "phone VARCHAR(20), " +
            "role ENUM('ADMIN', 'LIBRARIAN', 'STUDENT') NOT NULL, " +
            "is_active BOOLEAN DEFAULT false, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "approved_at TIMESTAMP NULL" +
            ")"
        );

        // Books table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS books (" +
            "book_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "title VARCHAR(255) NOT NULL, " +
            "author VARCHAR(255) NOT NULL, " +
            "isbn VARCHAR(13) UNIQUE NOT NULL, " +
            "category VARCHAR(50), " +
            "quantity INT NOT NULL DEFAULT 1, " +
            "available_quantity INT NOT NULL DEFAULT 1, " +
            "shelf_location VARCHAR(50), " +
            "is_active BOOLEAN DEFAULT true" +
            ")"
        );

        // Book borrowings table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS book_borrowings (" +
            "borrowing_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "book_id INT NOT NULL, " +
            "user_id INT NOT NULL, " +
            "borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "due_date TIMESTAMP NOT NULL, " +
            "return_date TIMESTAMP NULL, " +
            "status ENUM('BORROWED', 'RETURNED', 'OVERDUE') NOT NULL DEFAULT 'BORROWED', " +
            "FOREIGN KEY (book_id) REFERENCES books(book_id), " +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );

        // Notifications table
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS notifications (" +
            "notification_id INT PRIMARY KEY AUTO_INCREMENT, " +
            "user_id INT NOT NULL, " +
            "message TEXT NOT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "is_read BOOLEAN DEFAULT false, " +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );

        // Create default admin account if it doesn't exist
        stmt.executeUpdate(
            "INSERT IGNORE INTO users (username, password, full_name, email, role, is_active) " +
            "VALUES ('admin', 'admin123', 'System Administrator', 'admin@library.com', 'ADMIN', true)"
        );
    }
}