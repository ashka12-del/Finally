package org.example.finallyy;

import java.sql.*;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:campus_life.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createUserTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id TEXT PRIMARY KEY,"
                + "username TEXT,"
                + "email TEXT,"
                + "password TEXT,"
                + "role TEXT,"
                + "status TEXT DEFAULT 'Pending');";

        String createHealthTable = "CREATE TABLE IF NOT EXISTS health_requests ("
                + "request_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "student_id TEXT,"
                + "symptoms TEXT,"
                + "type TEXT,"
                + "time_slot TEXT,"
                + "status TEXT DEFAULT 'Pending',"
                + "prescription TEXT,"
                + "doctor_note TEXT);";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createHealthTable);
            System.out.println("Database Initialized Successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(String user, String email, String id, String pass, String role) {
        String sql = "INSERT INTO users(id, username, email, password, role, status) VALUES(?,?,?,?,?,'Pending')";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, user);
            pstmt.setString(3, email);
            pstmt.setString(4, pass);
            pstmt.setString(5, role);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static boolean saveHealthRequest(String stdId, String symptoms, String type, String time) {
        String sql = "INSERT INTO health_requests (student_id, symptoms, type, time_slot) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, stdId);
            pstmt.setString(2, symptoms);
            pstmt.setString(3, type);
            pstmt.setString(4, time);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }
}