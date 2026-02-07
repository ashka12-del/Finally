package org.example.finallyy;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class DoctorController {

    @FXML private TableView<Appointment> appointmentTable;
    @FXML private TableColumn<Appointment, String> colStudentId, colType, colTime, colStatus, colAction;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private Text pendingCountText, completedCountText;

    @FXML private VBox prescriptionPane;
    @FXML private VBox prescriptionInputBox, noteInputBox;
    @FXML private Label detailsLabel;
    @FXML private TextArea prescriptionArea, noteArea;

    private ObservableList<Appointment> allPendingAppointments = FXCollections.observableArrayList();
    private Appointment selectedAppointment;

    public void initialize() {
        typeFilterCombo.setItems(FXCollections.observableArrayList("All", "Online", "Physical"));
        setupColumns();
        loadAppointments();
    }

    private void setupColumns() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View & Process");
            {
                btn.setStyle("-fx-background-color: #C850C0; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 15;");
                btn.setOnAction(event -> {
                    selectedAppointment = getTableView().getItems().get(getIndex());
                    openPrescriptionPane(selectedAppointment);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });
    }

    private void loadAppointments() {
        allPendingAppointments.clear();
        int pending = 0, accepted = 0;
        // JOIN কুয়েরি ব্যবহার করলে পারফরম্যান্স ভালো হবে এবং একবারে নাম পাওয়া যাবে
        String sql = "SELECT a.*, u.username FROM appointments a JOIN users u ON a.student_id = u.id";

        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // ৮টি প্যারামিটার সহ Appointment অবজেক্ট তৈরি
                Appointment app = new Appointment(
                        rs.getInt("id"),
                        rs.getString("student_id"),
                        rs.getString("username"), // ৮ নম্বর প্যারামিটার: পেশেন্টের নাম
                        rs.getString("doctor_name"),
                        rs.getString("symptoms"),
                        rs.getString("type"),
                        rs.getString("time"),
                        rs.getString("status")
                );

                if (app.getStatus().equalsIgnoreCase("Pending")) {
                    allPendingAppointments.add(app);
                    pending++;
                } else if (app.getStatus().equalsIgnoreCase("Accepted")) {
                    accepted++;
                }
            }
            appointmentTable.setItems(allPendingAppointments);
            pendingCountText.setText(String.valueOf(pending));
            completedCountText.setText(String.valueOf(accepted));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void openPrescriptionPane(Appointment app) {
        // স্টুডেন্টের নাম ডাটাবেস থেকে নিয়ে আসা
        String studentName = DatabaseHandler.getStudentNameById(app.getStudentId());

        String baseInfo = "Name: " + studentName + "\nStudent ID: " + app.getStudentId() + "\nSymptoms: " + app.getSymptoms();

        if (app.getType().equalsIgnoreCase("Online")) {
            detailsLabel.setText(baseInfo);
            prescriptionInputBox.setVisible(true);
            prescriptionInputBox.setManaged(true);
            noteInputBox.setVisible(true);
            noteInputBox.setManaged(true);
        } else {
            detailsLabel.setText(baseInfo + "\nType: Physical\nTime: " + app.getTime());
            prescriptionInputBox.setVisible(false);
            prescriptionInputBox.setManaged(false);
            noteInputBox.setVisible(false);
            noteInputBox.setManaged(false);
        }
        prescriptionPane.setVisible(true);
    }

    @FXML
    private void submitPrescription() {
        if (selectedAppointment != null) {
            updateStatusInDB(selectedAppointment.getId(), "Accepted");
            prescriptionArea.clear();
            noteArea.clear();
            closePrescription();
            loadAppointments();
        }
    }

    private void updateStatusInDB(int id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    @FXML
    private void handleFilter() {
        String filter = typeFilterCombo.getValue();
        if (filter == null || filter.equals("All")) {
            appointmentTable.setItems(allPendingAppointments);
        } else {
            appointmentTable.setItems(allPendingAppointments.filtered(a -> a.getType().equalsIgnoreCase(filter)));
        }
    }

    @FXML private void closePrescription() { prescriptionPane.setVisible(false); }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}