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

    @FXML private Label activeUsersText, regRequestsText;
    @FXML private VBox paneDashboard, paneRegistration;
    @FXML private TableView<User> regTable;
    @FXML private TableColumn<User, String> colId, colName, colRole, colAction;

    private final String DB_URL = "jdbc:sqlite:campus_life.db";

    public void initialize() {
        setupTableColumns();
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

                    acceptBtn.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleAction(user, "Accepted");
                    });

                    rejectBtn.setOnAction(event -> {
                        User user = getTableView().getItems().get(getIndex());
                        handleAction(user, "Rejected");
                    });

                    HBox hBox = new HBox(acceptBtn, rejectBtn);
                    hBox.setSpacing(10);
                    setGraphic(hBox);
                }
                setText(null);
            }
        };
        colAction.setCellFactory(cellFactory);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPendingTable() {
        ObservableList<User> list = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM users WHERE status = 'Pending'")) {

            while (rs.next()) {
                list.add(new User(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ));
            }
            regTable.setItems(list);
        } catch (SQLException e) {
            System.err.println("Error loading table: " + e.getMessage());
        }
    }

    private void handleAction(User user, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, user.getId());
            pstmt.executeUpdate();

            System.out.println("User: " + user.getUsername() + " status updated to: " + status);
            refreshDashboard();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDashboard() {
        paneDashboard.setVisible(true);
        paneRegistration.setVisible(false);
        refreshDashboard();
    }

    @FXML
    public void showRegistrationQueue() {
        paneDashboard.setVisible(false);
        paneRegistration.setVisible(true);
        loadPendingTable();
    }

    @FXML
    public void showConsultationLogs() {
        System.out.println("Consultation Logs Clicked");
    }

    @FXML
    public void showPrintHistory() {
        System.out.println("Print History Clicked");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Admin logging out...");
        try {
            String path = "/org/example/finallyy/login.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle("Login - Campus Life Hub");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}