package org.example.finallyy;

/**
 * এই ক্লাসটি ইউজার ডাটা মডেল হিসেবে কাজ করে।
 * টেবিল ভিউতে ডাটা দেখানোর জন্য এটি অত্যন্ত প্রয়োজনীয়।
 */
public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String status;

    // কনস্ট্রাক্টর
    public User(String id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = "Pending"; // ডিফল্ট স্ট্যাটাস পেন্ডিং থাকবে
    }

    // টেবিল ভিউতে ডাটা দেখানোর জন্য Getters (এগুলো অবশ্যই থাকতে হবে)
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getStatus() { return status; }

    // স্ট্যাটাস পরিবর্তন করার জন্য Setter
    public void setStatus(String status) {
        this.status = status;
    }
}