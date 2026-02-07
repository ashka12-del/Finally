package org.example.finallyy;

import javafx.application.Platform;
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
    @FXML private TextField loginIdField;
    @FXML private PasswordField loginPasswordField;
    @FXML private ComboBox<String> loginRoleComboBox;
    @FXML private Label loginErrorLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText();
        String userId = loginIdField.getText();
        String password = loginPasswordField.getText();
        String role = loginRoleComboBox.getValue();

        if (username.isEmpty() || userId.isEmpty() || password.isEmpty() || role == null) {
            loginErrorLabel.setText("Please fill in all fields!");
            return;
        }

        // Hardcoded roles for Admin, Doctor, Shopkeeper
        if (role.equals("Admin") && userId.equals("1") && username.equalsIgnoreCase("Admin") && password.equals("123")) {
            UserSession.loggedInUserName = "Admin";
            UserSession.loggedInUserId = "1";
            switchScene(event, "admin.fxml", "Admin Dashboard");
            return;
        } else if (role.equals("Doctor") && (userId.equalsIgnoreCase("d1") || userId.equalsIgnoreCase("d2")) && password.equals("123")) {
            UserSession.loggedInUserName = "Doctor";
            UserSession.loggedInUserId = userId;
            switchScene(event, "doctor.fxml", "Doctor Dashboard");
            return;
        } else if (role.equals("Shopkeeper") && (userId.equalsIgnoreCase("s1") || userId.equalsIgnoreCase("s2")) && password.equals("123")) {
            UserSession.loggedInUserName = "Shopkeeper";
            UserSession.loggedInUserId = userId;
            switchScene(event, "Shopkeeper.fxml", "Shopkeeper Dashboard");
            return;
        }

        if (role.equals("Student")) {
            validateUserFromDB(event, userId, username, password, role);
        }
    }

    private void validateUserFromDB(ActionEvent event, String userId, String username, String password, String role) {
        String query = "SELECT id, status, username FROM users WHERE id = ? AND username = ? AND password = ? AND role = ?";

        // DatabaseHandler ব্যবহার করা হচ্ছে কানেকশনের জন্য
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, username);
            pstmt.setString(3, password);
            pstmt.setString(4, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    String dbId = rs.getString("id");
                    String dbUsername = rs.getString("username");

                    if (status != null && status.equalsIgnoreCase("Accepted")) {
                        UserSession.loggedInUserId = dbId;
                        UserSession.loggedInUserName = dbUsername;
                        switchScene(event, "StudentDashboard.fxml", "Student Dashboard");
                    } else if (status != null && status.equalsIgnoreCase("Pending")) {
                        showAlert(Alert.AlertType.WARNING, "Pending", "Registration is pending Admin approval.");
                    } else if (status != null && status.equalsIgnoreCase("Rejected")) {
                        handleRejectedUser(dbId);
                    }
                } else {
                    loginErrorLabel.setText("Invalid Student Credentials!");
                }
            }
        } catch (SQLException e) {
            loginErrorLabel.setText("Database Error!");
            e.printStackTrace();
        }
    }

    private void handleRejectedUser(String userId) {
        // Platform.runLater ব্যবহার করা হয়েছে যাতে UI থ্রেডে কোনো সমস্যা না হয়
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Request Rejected");
            alert.setHeaderText("Registration Rejected!");
            alert.setContentText("Your application was denied. Click OK to try again.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    boolean success = deleteUserFromDB(userId);
                    if (success) {
                        loginErrorLabel.setText("You can register again.");
                    } else {
                        loginErrorLabel.setText("Failed to clear data. Try again.");
                    }
                }
            });
        });
    }

    private boolean deleteUserFromDB(String userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        // try-with-resources ব্যবহার করা হয়েছে যাতে কানেকশন অটো ক্লোজ হয়
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            int rowsDeleted = pstmt.executeUpdate();

            // যদি rowsDeleted ০ এর বেশি হয়, তার মানে ডিলিট সফল হয়েছে
            return rowsDeleted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML private void handleGoToRegister(ActionEvent event) { switchScene(event, "register.fxml", "Register"); }
    @FXML private void handleAboutUs(ActionEvent event) { System.out.println("Opening About Us..."); }
    @FXML private void handleContactUs(ActionEvent event) { System.out.println("Opening Contact Us..."); }
}