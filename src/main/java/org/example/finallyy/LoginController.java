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
    @FXML private PasswordField loginPasswordField;
    @FXML private ComboBox<String> loginRoleComboBox;
    @FXML private Label loginErrorLabel;

    // ডাটাবেস ইউআরএল
    private final String DB_URL = "jdbc:sqlite:campus_life.db";

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText();
        String password = loginPasswordField.getText();
        String role = loginRoleComboBox.getValue();

        // ১. ইনপুট ফিল্ড চেক
        if (username.isEmpty() || password.isEmpty() || role == null) {
            loginErrorLabel.setText("Please fill in all fields!");
            return;
        }

        // ২. স্পেশাল রোলস (Admin, Shopkeeper, Doctor) - হার্ডকোডেড লগইন
        if (role.equals("Admin") && username.equalsIgnoreCase("Admin") && password.equals("123")) {
            switchScene(event, "admin.fxml", "Admin Dashboard");
            return;
        }
        else if (role.equals("Shopkeeper") && username.equalsIgnoreCase("Shopkeeper") && password.equals("123")) {
            switchScene(event, "shopkeeper.fxml", "Shopkeeper Dashboard");
            return;
        }
        else if (role.equals("Doctor") && username.equalsIgnoreCase("Doctor") && password.equals("123")) {
            switchScene(event, "doctor.fxml", "Doctor Dashboard");
            return;
        }

        // ৩. স্টুডেন্টদের জন্য ডাটাবেস চেক
        validateUserFromDB(event, username, password, role);
    }

    private void validateUserFromDB(ActionEvent event, String username, String password, String role) {
        // স্ট্যাটাস এবং ইউজারনেম ডাটাবেস থেকে রিড করা
        String query = "SELECT status, username FROM users WHERE username = ? AND password = ? AND role = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                String dbUsername = rs.getString("username");

                // ৪. স্ট্যাটাস অনুযায়ী মেসেজ এবং সিন পরিবর্তন
                if (status.equalsIgnoreCase("Accepted")) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Welcome, " + dbUsername + "!");
                    if (role.equals("Student")) {
                        switchScene(event, "student.fxml", "Student Dashboard");
                    }
                }
                else if (status.equalsIgnoreCase("Pending")) {
                    showAlert(Alert.AlertType.WARNING, "Registration Pending",
                            "Your registration is pending. Please wait for Admin approval.");
                }
                else if (status.equalsIgnoreCase("Rejected")) {
                    showAlert(Alert.AlertType.ERROR, "Registration Rejected",
                            "Your registration was rejected. Please try again with valid info.");
                }
            } else {
                loginErrorLabel.setText("Invalid Username, Password, or Role!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            loginErrorLabel.setText("Database Connection Error!");
        }
    }

    // পপ-আপ মেসেজ দেখানোর মেথড
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // সিন স্যুইচিং মেথড (পাথ ফিক্সড)
    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            // আপনার প্যাকেজ স্ট্রাকচার অনুযায়ী রিসোর্স পাথ
            String resourcePath = "/org/example/finallyy/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));

            if (loader.getLocation() == null) {
                loginErrorLabel.setText("FXML not found in: " + resourcePath);
                return;
            }

            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 1080, 720)); // আপনার অ্যাডমিন ডিজাইন অনুযায়ী সাইজ
            stage.show();
        } catch (IOException e) {
            loginErrorLabel.setText("FXML loading error!");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        switchScene(event, "register.fxml", "Register - Campus Life Hub");
    }

    @FXML private void handleAboutUs(ActionEvent event) { System.out.println("Opening About Us..."); }
    @FXML private void handleContactUs(ActionEvent event) { System.out.println("Opening Contact Us..."); }
}