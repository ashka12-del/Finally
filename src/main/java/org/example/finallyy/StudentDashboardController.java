package org.example.finallyy;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent; // এটি প্রয়োজন
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class StudentDashboardController {

    @FXML private Label printer1Queue, printer2Queue, studentNameLabel;
    @FXML private VBox printerPane, mainHomeBox, cardPrint, cardHealth, cardMap; // আইডিগুলো যুক্ত করা হলো
    @FXML private StackPane contentPane;

    private String currentStudentId = "STU_DEFAULT";

    public void initialize() {
        updateQueueCounts();
    }

    private void updateQueueCounts() {
        printer1Queue.setText("Queue: " + DatabaseHandler.getPrinterQueueCount("Printer 1 (Tower 1)") + " Students");
        printer2Queue.setText("Queue: " + DatabaseHandler.getPrinterQueueCount("Printer 2 (Tower 2)") + " Students");
    }

    // প্রিন্টার সিলেকশন দেখানোর জন্য সহজ লজিক
    @FXML public void showPrinterSelection() {
        mainHomeBox.setVisible(false);
        printerPane.setVisible(true);
        updateQueueCounts();
    }

    // হোম ভিউতে ফিরে যাওয়ার জন্য মেথড
    @FXML public void backToHome(ActionEvent event) {
        printerPane.setVisible(false);
        mainHomeBox.setVisible(true);
    }

    // হোভার ইফেক্ট যা আপনার FXML এ কল করা আছে
    @FXML private void handleHoverEnter(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        card.setScaleX(1.1);
        card.setScaleY(1.1);
    }

    @FXML private void handleHoverExit(MouseEvent event) {
        VBox card = (VBox) event.getSource();
        card.setScaleX(1.0);
        card.setScaleY(1.0);
    }

    @FXML
    public void showHealth(MouseEvent event) { // ActionEvent এর বদলে MouseEvent ব্যবহার করুন যদি FXML এ ক্লিক ইভেন্ট থাকে
        try {
            // ফাইলে পাথটি চেক করুন: আপনার resources ফোল্ডারের ভেতর HealthConsultation.fxml আছে কি না নিশ্চিত হোন
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/finallyy/HealthConsultation.fxml"));
            Parent healthPane = loader.load();

            // ড্যাশবোর্ডের মেইন কন্টেন্ট সরিয়ে হেলথ প্যান যোগ করা
            contentPane.getChildren().clear();
            contentPane.getChildren().add(healthPane);

            System.out.println("Health Consultation Page Loaded Successfully!");
        } catch (IOException e) {
            System.err.println("Error: Could not find HealthConsultation.fxml. Check the file name and path.");
            e.printStackTrace();
        }
    }

    @FXML public void showMap() {
        System.out.println("Opening Campus Map...");
    }

    @FXML public void showNotifications() {
        // আপনার বর্তমান নোটিফিকেশন কোড এখানে থাকবে...
    }

    @FXML private void goToPrintConfig1(ActionEvent event) { switchScene(event, "StudentPrint.fxml", "Printer 1 Config"); }
    @FXML private void goToPrintConfig2(ActionEvent event) { switchScene(event, "StudentPrint.fxml", "Printer 2 Config"); }
    @FXML private void handleLogout(ActionEvent event) { switchScene(event, "login.fxml", "Login"); }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/finallyy/" + fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setStudentData(String studentId, String studentName) {
        this.currentStudentId = studentId;
        if (studentNameLabel != null) {
            this.studentNameLabel.setText("Welcome, " + studentName);
        }
    }
}