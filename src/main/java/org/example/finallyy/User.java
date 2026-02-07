package org.example.finallyy;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String status;

    public User(String id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = "Pending";
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}