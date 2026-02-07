package org.example.finallyy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:campus_life.db";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite Driver not found!");
        }
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, username TEXT, email TEXT, password TEXT, role TEXT, status TEXT DEFAULT 'Pending');";
        String createPrintTable = "CREATE TABLE IF NOT EXISTS print_requests (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT, printer_no TEXT, file_name TEXT, page_range TEXT, color TEXT, copies INTEGER, pickup_time TEXT, pickup_date TEXT, token_no INTEGER, status TEXT DEFAULT 'Pending');";
        String createHealthTable = "CREATE TABLE IF NOT EXISTS appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id TEXT, doctor_name TEXT, symptoms TEXT, type TEXT, time TEXT, status TEXT DEFAULT 'Pending');";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createPrintTable);
            stmt.execute(createHealthTable);
            System.out.println("Database Initialized Successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static boolean registerUser(String user, String email, String id, String pass, String role) {
        String sql = "INSERT INTO users(id, username, email, password, role, status) VALUES(?,?,?,?,?,'Pending')";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id); pstmt.setString(2, user); pstmt.setString(3, email);
            pstmt.setString(4, pass); pstmt.setString(5, role);
            pstmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }

    public static int getPrinterQueueCount(String printerName) {
        String sql = "SELECT COUNT(*) FROM print_requests WHERE printer_no = ? AND status = 'Pending'";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, printerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ৫. প্রিন্ট রিকোয়েস্ট সেভ করার মেথড (আপনার এরর দূর করার জন্য এটি যোগ করা হয়েছে)
    public static boolean savePrintRequest(String studentId, String printer, String file, String range, String color, int copies, String time, String date, int token) {
        String sql = "INSERT INTO print_requests(student_id, printer_no, file_name, page_range, color, copies, pickup_time, pickup_date, token_no) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, printer);
            pstmt.setString(3, file);
            pstmt.setString(4, range);
            pstmt.setString(5, color);
            pstmt.setInt(6, copies);
            pstmt.setString(7, time);
            pstmt.setString(8, date);
            pstmt.setInt(9, token);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getNextTokenNumber() {
        String sql = "SELECT MAX(token_no) FROM print_requests";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) { e.printStackTrace(); }
        return 1;
    }

    public static int getDoctorTotalRequestCount(String doctorName) {
        String sql = "SELECT COUNT(*) FROM appointments WHERE doctor_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public static String getStudentNameById(String id) {
        String sql = "SELECT username FROM users WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Unknown Student";
    }

    public static List<String> getBookedTimes(String doctorName) {
        List<String> bookedTimes = new ArrayList<>();
        String sql = "SELECT time FROM appointments WHERE doctor_name = ? AND status != 'Cancelled'";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String t = rs.getString("time");
                if (t != null && !t.isEmpty()) bookedTimes.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return bookedTimes;
    }

    public static boolean saveAppointment(String studentId, String doctor, String symptoms, String type, String time) {
        String sql = "INSERT INTO appointments(student_id, doctor_name, symptoms, type, time) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId); pstmt.setString(2, doctor);
            pstmt.setString(3, symptoms); pstmt.setString(4, type); pstmt.setString(5, time);
            pstmt.executeUpdate(); return true;
        } catch (SQLException e) { return false; }
    }
}