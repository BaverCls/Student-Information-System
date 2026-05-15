package models;

public class User {
    private String username;
    private String password;
    private String role; 
    private String fullName;
    private String referenceId;

    public User(String username, String password, String role, String fullName, String referenceId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.referenceId = referenceId;
    }

    public String toFileString() {
        return username + "," + password + "," + role + "," + fullName + "," + referenceId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}
