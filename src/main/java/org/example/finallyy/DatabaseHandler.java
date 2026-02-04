package org.example.finallyy;

import java.sql.*;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:campus_life.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        // টেবিল তৈরির সময় 'status' কলাম নিশ্চিত করা হয়েছে
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id TEXT PRIMARY KEY,"
                + "username TEXT,"
                + "email TEXT,"
                + "password TEXT,"
                + "role TEXT,"
                + "status TEXT DEFAULT 'Pending');";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);

            // যদি কলাম না থাকে তবে তা যোগ করবে (আপনার ডাটাবেস স্ক্রিনশট ৭৭৮ এর সমস্যা সমাধান করবে)
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN status TEXT DEFAULT 'Pending'");
            } catch (SQLException ignored) {}

            System.out.println("Database Initialized!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // RegisterController-এর জন্য এই মেথডটি যোগ করুন
    public static boolean registerUser(String user, String email, String id, String pass, String role) {
        String sql = "INSERT INTO users(id, username, email, password, role, status) VALUES(?,?,?,?,?,'Pending')";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, user);
            pstmt.setString(3, email);
            pstmt.setString(4, pass);
            pstmt.setString(5, role);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}