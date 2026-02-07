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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.*;

public class AdminController {

    @FXML private Label activeUsersText, regRequestsText, printRequestsText, consultationReqText;
    @FXML private VBox paneDashboard, paneRegistration, paneConsultation, panePrint;

    @FXML private TableView<User> regTable;
    @FXML private TableColumn<User, String> colId, colName, colRole, colAction;

    // Consultation Table & Columns (আপডেট করা হয়েছে)
    @FXML private TableView<Appointment> consultationTable;
    @FXML private TableColumn<Appointment, String> colPatientId, colPatientName, colDocReason, colConsultStatus; // colPatientName যোগ করা হয়েছে

    @FXML private TableView<PrintRequest> printTable;
    @FXML private TableColumn<PrintRequest, String> colPrintId, colFileName, colPrintResult;

    private final String DB_URL = "jdbc:sqlite:campus_life.db";

    public void initialize() {
        setupTableColumns();
        setupConsultationColumns();
        setupPrintHistoryColumns();
        refreshDashboard();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        Callback<TableColumn<User, String>, TableCell<User, String>> cellFactory = param -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button acceptBtn = new Button("Accept");
                    Button rejectBtn = new Button("Reject");
                    acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                    rejectBtn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-cursor: hand;");
                    acceptBtn.setOnAction(event -> handleAction(getTableView().getItems().get(getIndex()), "Accepted"));
                    rejectBtn.setOnAction(event -> handleAction(getTableView().getItems().get(getIndex()), "Rejected"));
                    HBox hBox = new HBox(acceptBtn, rejectBtn);
                    hBox.setSpacing(10);
                    setGraphic(hBox);
                }
                setText(null);
            }
        };
        colAction.setCellFactory(cellFactory);
    }

    // --- Consultation Table কলাম সেটআপ ---
    private void setupConsultationColumns() {
        colPatientId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("studentName")); // আইডি'র সাথে নাম দেখাবে
        colDocReason.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        colConsultStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // ডানের অতিরিক্ত কলাম দূর করতে এটি যোগ করুন
        consultationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupPrintHistoryColumns() {
        // Print column setup logic here
    }

    public void refreshDashboard() {
        loadCounts();
        loadPendingTable();
    }

    private void loadCounts() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE status = 'Accepted'");
            if (rs1.next()) activeUsersText.setText(String.valueOf(rs1.getInt(1)));
            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE status = 'Pending'");
            if (rs2.next()) regRequestsText.setText(String.valueOf(rs2.getInt(1)));
            ResultSet rs3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM appointments");
            if (rs3.next()) consultationReqText.setText(String.valueOf(rs3.getInt(1)));
            ResultSet rs4 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM print_requests");
            if (rs4.next()) printRequestsText.setText(String.valueOf(rs4.getInt(1)));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPendingTable() {
        ObservableList<User> list = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users WHERE status = 'Pending'")) {
            while (rs.next()) {
                list.add(new User(rs.getString("id"), rs.getString("username"), rs.getString("email"), rs.getString("password"), rs.getString("role")));
            }
            regTable.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- ডাটাবেস থেকে JOIN কুয়েরি দিয়ে আইডি এবং নাম নিয়ে আসা ---
    private void loadConsultationLogs() {
        ObservableList<Appointment> list = FXCollections.observableArrayList();
        // appointments টেবিলের সাথে users টেবিল JOIN করা হয়েছে
        String sql = "SELECT a.*, u.username FROM appointments a JOIN users u ON a.student_id = u.id";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Appointment(
                        rs.getInt("id"),
                        rs.getString("student_id"),
                        rs.getString("username"), // users টেবিল থেকে নাম নেওয়া হয়েছে
                        rs.getString("doctor_name"),
                        rs.getString("symptoms"),
                        rs.getString("type"),
                        rs.getString("time"),
                        rs.getString("status")
                ));
            }
            consultationTable.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void handleAction(User user, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, user.getId());
            pstmt.executeUpdate();
            refreshDashboard();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void hideAllPanes() {
        paneDashboard.setVisible(false);
        paneRegistration.setVisible(false);
        paneConsultation.setVisible(false);
        panePrint.setVisible(false);
    }

    @FXML public void showDashboard() {
        hideAllPanes();
        paneDashboard.setVisible(true);
        refreshDashboard();
    }

    @FXML public void showRegistrationQueue() {
        hideAllPanes();
        paneRegistration.setVisible(true);
        loadPendingTable();
    }

    @FXML public void showConsultationLogs() {
        hideAllPanes();
        paneConsultation.setVisible(true);
        loadConsultationLogs();
    }

    @FXML public void showPrintHistory() {
        hideAllPanes();
        panePrint.setVisible(true);
    }

    @FXML private void handleLogout(ActionEvent event) {
        switchScene(event, "login.fxml", "Login - Campus Life Hub");
    }

    private void switchScene(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/" + fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}