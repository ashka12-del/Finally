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
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShopkeeperController {

    @FXML private TableView<PrintRequest> queueTable, completedTable;
    @FXML private TableColumn<PrintRequest, String> colToken, colStudent, colDetails, colAction;
    @FXML private TableColumn<PrintRequest, String> colCompToken, colCompStudent, colCompStatus;
    @FXML private VBox selectionArea, tableArea, cardPending, cardCompleted;
    @FXML private Label tableHeader;

    private ObservableList<PrintRequest> pendingData = FXCollections.observableArrayList();
    private ObservableList<PrintRequest> completedData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Table columns mapping
        colToken.setCellValueFactory(new PropertyValueFactory<>("token"));
        colStudent.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));

        colCompToken.setCellValueFactory(new PropertyValueFactory<>("token"));
        colCompStudent.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colCompStatus.setCellValueFactory(new PropertyValueFactory<>("completionTime"));

        setupActionColumn();
        setupHoverEffects();

        // Database থেকে ডাটা লোড করা
        refreshTablesFromDatabase();

        queueTable.setItems(pendingData);
        completedTable.setItems(completedData);
    }

    private void refreshTablesFromDatabase() {
        pendingData.clear();
        completedData.clear();

        String sql = "SELECT * FROM print_requests";
        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String token = "T-" + rs.getInt("token_no");
                String studentId = rs.getString("student_id");
                String details = String.format("%s, %s, %d copies, %s",
                        rs.getString("file_name"), rs.getString("color"),
                        rs.getInt("copies"), rs.getString("page_range"));

                // ডাটাবেস থেকে pickup_time নিয়ে আসা
                String pickupTime = rs.getString("pickup_time");
                String status = rs.getString("status");

                // নতুন অবজেক্ট তৈরির সময় pickupTime পাঠিয়ে দিন
                PrintRequest request = new PrintRequest(token, studentId, details, pickupTime);

                if ("Pending".equalsIgnoreCase(status)) {
                    pendingData.add(request);
                } else {
                    // কমপ্লিটেড হলেও আমরা স্টুডেন্টের দেওয়া পিকআপ টাইমটাই দেখাবো
                    completedData.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void completeJob(PrintRequest request) {
        // ডাটাবেসে স্ট্যাটাস আপডেট করা
        String tokenRaw = request.getToken().replace("T-", "");
        String sql = "UPDATE print_requests SET status = 'Completed' WHERE token_no = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(tokenRaw));
            int affectedRows = pstmt.executeUpdate();

            // এই অংশটি পরিবর্তন করুন
            if (affectedRows > 0) {
                pendingData.remove(request);
                // request.setCompletionTime(LocalDateTime.now().format(...)); // এটি কমেন্ট করে দিলে অরিজিনাল টাইমটাই থাকবে
                completedData.add(request);
            }
        } catch (SQLException e) {
            System.err.println("Update Error: " + e.getMessage());
        }
    }

    private void setupActionColumn() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Mark Done");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 10;");
                btn.setOnAction(event -> {
                    PrintRequest request = getTableView().getItems().get(getIndex());
                    completeJob(request);
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

    // UI Navigation Methods
    @FXML
    public void showPendingTable() {
        refreshTablesFromDatabase(); // ওপেন করার সময় লেটেস্ট ডাটা দেখাবে
        selectionArea.setVisible(false);
        selectionArea.setManaged(false);
        tableArea.setVisible(true);
        tableArea.setManaged(true);
        tableHeader.setText("Service Queue: Smart Printing");
        queueTable.setVisible(true);
        completedTable.setVisible(false);
    }

    @FXML
    public void showCompletedTable() {
        refreshTablesFromDatabase();
        selectionArea.setVisible(false);
        selectionArea.setManaged(false);
        tableArea.setVisible(true);
        tableArea.setManaged(true);
        tableHeader.setText("Service Logs: Completed Orders");
        queueTable.setVisible(false);
        completedTable.setVisible(true);
    }

    @FXML
    public void backToSelection() {
        tableArea.setVisible(false);
        tableArea.setManaged(false);
        selectionArea.setVisible(true);
        selectionArea.setManaged(true);
    }

    private void setupHoverEffects() {
        String normalStyle = "-fx-background-color: #161616; -fx-background-radius: 35; -fx-border-color: #3D0E5A; -fx-border-width: 4; -fx-border-radius: 35;";
        String hoverStyle = "-fx-background-color: linear-gradient(to bottom right, #4A148C, #161616); -fx-background-radius: 35; -fx-border-color: #C850C0; -fx-border-width: 4; -fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-border-radius: 35;";

        cardPending.setOnMouseEntered(e -> cardPending.setStyle(hoverStyle));
        cardPending.setOnMouseExited(e -> cardPending.setStyle(normalStyle));
        cardCompleted.setOnMouseEntered(e -> cardCompleted.setStyle(hoverStyle));
        cardCompleted.setOnMouseExited(e -> cardCompleted.setStyle(normalStyle));
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}