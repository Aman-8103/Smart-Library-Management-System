# Smart Library Management System 📚

A desktop-based Library Management System developed in **Java (Swing)** and **MySQL**, designed with Object-Oriented Programming (OOP) principles and multi-tiered Role-Based Access Control (RBAC).

---

## 🌟 Key Features

### 1. Multi-Tier Role-Based Access Control (RBAC)
- **Admin Dashboard**:
  - Review and approve/reject new user registrations.
  - Manage librarians (add, edit, deactivate, reactivate).
  - Fine oversight (view, waive, mark as paid).
  - Generate comprehensive reports (Books, Borrowings, Fines) with Excel export support.
- **Librarian Dashboard**:
  - Book catalog management (Add, update, delete books).
  - Book issue and return desk with automated availability tracking.
  - View real-time borrowing statuses and student records.
  - Send and manage notifications.
- **Student Dashboard**:
  - Browse book catalog and request borrowings.
  - Return borrowed titles.
  - Monitor active loans, due dates, and outstanding fines.
  - Receive account and due-date notifications.

### 2. Transaction Integrity & Business Logic
- **ACID Transactions**: Atomic execution using JDBC (`setAutoCommit(false)`, `commit()`, `rollback()`) to ensure consistent updates across book inventory, circulation records, and notification logs.
- **Automated Fine Calculation**: Dynamic calculation of late return fees based on scheduled due dates.
- **Approval Gate**: New accounts require administrator approval before login access is granted.

### 3. Modern GUI Design
- Modern Java Swing UI with light/dark theme toggle support.
- Custom styled components (Rounded panels, gradient backgrounds, modern action buttons).

---

## 🗄️ Database Architecture

The system connects to MySQL (`library_db`) and automatically initializes required tables on first startup:
- `users`: User profiles, roles (`ADMIN`, `LIBRARIAN`, `STUDENT`), activation status, and credentials.
- `books`: Catalog details (ISBN, title, author, category, total copies, available copies, shelf location).
- `book_borrowings`: Circulation records (borrow date, 14-day due date, return date, status).
- `notifications`: User-specific system alerts and administrative approval messages.
- `fines`: Fine records, overdue day tracking, and payment statuses (`PENDING`, `PAID`, `WAIVED`).
- `settings`: User preferences (theme selection, notifications).

---

## 🛠️ Technology Stack

- **Language**: Java (JDK 8+)
- **GUI Framework**: Java Swing & AWT
- **Database**: MySQL Server
- **Connectivity**: JDBC (MySQL Connector/J)
- **Architecture**: Object-Oriented Architecture (OOP) & MVC design patterns

---

## 🚀 Getting Started

### Prerequisites
1. **Java Development Kit (JDK 8 or higher)** installed and configured in your PATH.
2. **MySQL Server** (running on `localhost:3306`).
3. MySQL JDBC Driver (`mysql-connector-j-9.3.0.jar` included in root).

### Database Configuration
1. Open [`DatabaseConnection.java`](DatabaseConnection.java).
2. Configure your MySQL credentials:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/library_db";
   private static final String USERNAME = "root";
   private static final String PASSWORD = "your_mysql_password";
   ```
3. The application will automatically create the database `library_db` and all necessary tables upon first launch.

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `ADMIN`

### Running the Application
Compile and run [`LoginScreen.java`](LoginScreen.java):

```bash
# Compile
javac -cp ".;mysql-connector-j-9.3.0.jar" *.java

# Run
java -cp ".;mysql-connector-j-9.3.0.jar" LoginScreen
```
*(On Linux/macOS, use `:` as classpath separator instead of `;`)*

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
