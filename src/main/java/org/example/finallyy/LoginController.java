package org.example.finallyy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class LoginController {
    @FXML private TextField loginUsernameField;
    @FXML private TextField loginIdField; // User ID input field
    @FXML private PasswordField loginPasswordField;
    @FXML private ComboBox<String> loginRoleComboBox;
    @FXML private Label loginErrorLabel;

    private final String DB_URL = "jdbc:sqlite:campus_life.db";

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText();
        String userId = loginIdField.getText();
        String password = loginPasswordField.getText();
        String role = loginRoleComboBox.getValue();

        // ১. ইনপুট ফিল্ড খালি আছে কিনা চেক
        if (username.isEmpty() || userId.isEmpty() || password.isEmpty() || role == null) {
            loginErrorLabel.setText("Please fill in all fields!");
            return;
        }

        // --- হার্ডকোডেড লগইন লজিক (Admin, Doctor, Shopkeeper) ---

        // ২. Admin (ID: 1)
        if (role.equals("Admin")) {
            if (userId.equals("1") && username.equalsIgnoreCase("Admin") && password.equals("123")) {
                switchScene(event, "admin.fxml", "Admin Dashboard");
                return;
            } else {
                loginErrorLabel.setText("Invalid Admin Credentials!");
                return;
            }
        }

        // ৩. Doctor (ID: d1, d2)
        else if (role.equals("Doctor")) {
            if ((userId.equalsIgnoreCase("d1") || userId.equalsIgnoreCase("d2"))
                    && username.equalsIgnoreCase("Doctor") && password.equals("123")) {
                switchScene(event, "doctor.fxml", "Doctor Dashboard");
                return;
            } else {
                loginErrorLabel.setText("Invalid Doctor Credentials! Use d1/d2.");
                return;
            }
        }

        // ৪. Shopkeeper (ID: s1, s2)
        else if (role.equals("Shopkeeper")) {
            if ((userId.equalsIgnoreCase("s1") || userId.equalsIgnoreCase("s2"))
                    && username.equalsIgnoreCase("Shopkeeper") && password.equals("123")) {
                switchScene(event, "shopkeeper.fxml", "Shopkeeper Dashboard");
                return;
            } else {
                loginErrorLabel.setText("Invalid Shopkeeper Credentials! Use s1/s2.");
                return;
            }
        }

        // --- স্টুডেন্ট ডাটাবেস ভ্যালিডেশন ---
        if (role.equals("Student")) {
            validateUserFromDB(event, userId, username, password, role);
        }
    }

    private void validateUserFromDB(ActionEvent event, String userId, String username, String password, String role) {
        String query = "SELECT id, status, username FROM users WHERE id = ? AND username = ? AND password = ? AND role = ?";
        String status = "", dbId = "", dbUsername = "";
        boolean userFound = false;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    status = rs.getString("status");
                    dbId = rs.getString("id");
                    dbUsername = rs.getString("username");
                    userFound = true;
                }
            }
        } catch (SQLException e) {
            loginErrorLabel.setText("Database Connection Error!");
            return;
        }

        if (userFound) {
            if (status.equalsIgnoreCase("Accepted")) {
                UserSession.loggedInUserId = dbId;
                UserSession.loggedInUserName = dbUsername;
                switchScene(event, "student.fxml", "Student Dashboard");
            } else if (status.equalsIgnoreCase("Pending")) {
                showAlert(Alert.AlertType.WARNING, "Pending", "Registration is pending Admin approval.");
            } else if (status.equalsIgnoreCase("Rejected")) {
                handleRejectedUser(dbId);
            }
        } else {
            loginErrorLabel.setText("Invalid Student Credentials!");
        }
    }

    private void handleRejectedUser(String userId) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Request Rejected");
        alert.setHeaderText("Registration Rejected!");
        alert.setContentText("Your data will be cleared. Click OK to try again.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteUserFromDB(userId);
                loginErrorLabel.setText("Data cleared. You can register again.");
            }
        });
    }

    private void deleteUserFromDB(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            System.out.println("Row Deleted for ID: " + userId);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) { loginErrorLabel.setText("Scene Error: " + fxmlFile); }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML private void handleAboutUs(ActionEvent event) { System.out.println("About Us"); }
    @FXML private void handleContactUs(ActionEvent event) { System.out.println("Contact Us"); }
    @FXML private void handleGoToRegister(ActionEvent event) { switchScene(event, "register.fxml", "Register"); }
}