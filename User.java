public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String fullName;
    private boolean isActive;

    public User(int id, String username, String password, String role, String email, String fullName, boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.fullName = fullName;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isActive() { return isActive; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
}