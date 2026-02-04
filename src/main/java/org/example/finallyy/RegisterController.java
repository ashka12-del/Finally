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

public class RegisterController {
    @FXML private TextField regUsernameField, regEmailField, regIdField;
    @FXML private PasswordField regPasswordField;
    @FXML private ComboBox<String> regRoleComboBox;

    @FXML
    private void handleRegistration() {
        String user = regUsernameField.getText();
        String email = regEmailField.getText();
        String id = regIdField.getText();
        String pass = regPasswordField.getText();
        String role = regRoleComboBox.getValue();

        // ১. কোনো ঘর খালি আছে কি না চেক করা
        if (user.isEmpty() || email.isEmpty() || id.isEmpty() || pass.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Form Error!", "Please fill in all the fields!");
            return;
        }

        // ২. ডাটাবেসে তথ্য পাঠানো (Status ডিফল্ট 'Pending' থাকবে)
        boolean success = DatabaseHandler.registerUser(user, email, id, pass, role);

        // ৩. রেজাল্ট দেখানো
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Status", "Registration is pending for Admin approval!");
            clearFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Database Error!", "Failed to save data. ID might already exist.");
        }
    }

    // পপ-আপ অ্যালার্ট দেখানোর মেথড
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ইনপুট ফিল্ডগুলো পরিষ্কার করা
    private void clearFields() {
        regUsernameField.clear();
        regEmailField.clear();
        regIdField.clear();
        regPasswordField.clear();
        regRoleComboBox.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            // login.fxml লোড করা
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // বর্তমান স্টেজ (Window) খুঁজে বের করা
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // নতুন সিন (Login) সেট করা
            Scene scene = new Scene(root, 1000, 700);
            stage.setTitle("Campus Life Hub - Login");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error: Could not load login.fxml");
            e.printStackTrace();
        }
    }
}