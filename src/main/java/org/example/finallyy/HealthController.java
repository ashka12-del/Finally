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
import java.util.List;

public class HealthController {
    @FXML private ComboBox<String> doctorBox, timeBox;
    @FXML private TextArea symptomsArea;
    @FXML private ToggleGroup modeGroup;
    @FXML private Label countLabel, timeLabel;
    @FXML private RadioButton radioOnline, radioPhysical;

    private String currentStudentId = UserSession.loggedInUserId;
    private final String[] allSlots = {"09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:30 PM", "02:30 PM"};

    @FXML
    public void initialize() {
        doctorBox.getItems().addAll("Dr. Rahim (Medicine)", "Dr. Karim (Orthopedic)");

        timeBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                } else {
                    setText(item);
                    if (item.contains("(Booked)")) {
                        setDisable(true);
                        setStyle("-fx-text-fill: #9c27b0; -fx-opacity: 0.6;");
                    } else {
                        setDisable(false);
                        setStyle("-fx-text-fill: black;");
                    }
                }
            }
        });
    }

    @FXML
    private void handleDoctorSelection() {
        String doctor = doctorBox.getValue();
        if (doctor != null) {
            // এখন মোট রিকোয়েস্ট সংখ্যা দেখাবে (Pending + Accepted)
            int count = DatabaseHandler.getDoctorTotalRequestCount(doctor);
            countLabel.setText("Waitlist: " + count + " patients so far");
            updateAvailableTimes(doctor);
        }
    }

    private void updateAvailableTimes(String doctor) {
        List<String> booked = DatabaseHandler.getBookedTimes(doctor);
        timeBox.getItems().clear();
        for (String slot : allSlots) {
            if (booked.contains(slot)) {
                timeBox.getItems().add(slot + " (Booked)");
            } else {
                timeBox.getItems().add(slot);
            }
        }
    }

    @FXML
    private void toggleTimeVisibility() {
        boolean isOnline = radioOnline.isSelected();
        timeBox.setVisible(!isOnline);
        timeLabel.setVisible(!isOnline);
    }

    @FXML
    private void requestAppointment() {
        String doctor = doctorBox.getValue();
        String symptoms = symptomsArea.getText();
        RadioButton selected = (RadioButton) modeGroup.getSelectedToggle();

        String time = (radioOnline.isSelected()) ? "N/A" : timeBox.getValue();

        if (selected == null || doctor == null || symptoms.trim().isEmpty() || (radioPhysical.isSelected() && (time == null || time.isEmpty()))) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill all required fields!");
            return;
        }

        if (time != null && time.contains("(Booked)")) {
            showAlert(Alert.AlertType.ERROR, "Slot Unavailable", "Selected time is already taken!");
            return;
        }

        boolean success = DatabaseHandler.saveAppointment(currentStudentId, doctor, symptoms, selected.getText(), time);
        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment Requested Successfully!");
            symptomsArea.clear();
            handleDoctorSelection();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/StudentDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}