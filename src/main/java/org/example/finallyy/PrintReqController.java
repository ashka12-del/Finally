package org.example.finallyy;

import javafx.event.ActionEvent; // যুক্ত করা হয়েছে
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // যুক্ত করা হয়েছে
import javafx.scene.Node; // যুক্ত করা হয়েছে
import javafx.scene.Parent; // যুক্ত করা হয়েছে
import javafx.scene.Scene; // যুক্ত করা হয়েছে
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage; // যুক্ত করা হয়েছে
import java.io.File;
import java.io.IOException; // যুক্ত করা হয়েছে
import java.time.LocalDate;

public class PrintReqController {
    @FXML private ComboBox<String> printerSelection, pageRangeBox, colorModeBox, paperSizeBox, pickupTimeBox;
    @FXML private Label queueDisplayLabel;
    @FXML private Spinner<Integer> copiesSpinner;
    @FXML private Text fileNameLabel;
    @FXML private DatePicker pickupDatePicker;

    private String currentStudentId = "STU_DEFAULT";

    @FXML
    public void initialize() {
        printerSelection.getItems().addAll("Printer 1 (Tower 1)", "Printer 2 (Tower 2)");
        pageRangeBox.getItems().addAll("All Pages", "Odd Pages", "Even Pages");
        colorModeBox.getItems().addAll("Black & White", "Colored");
        paperSizeBox.getItems().addAll("A4", "Letter", "Legal");
        pickupTimeBox.getItems().addAll("08:00 AM", "10:00 AM", "02:00 PM");

        copiesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 50, 1));
        pickupDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void updateQueueDisplay() {
        String printer = printerSelection.getValue();
        if (printer != null) {
            int count = DatabaseHandler.getPrinterQueueCount(printer);
            queueDisplayLabel.setText("Queue: " + count);
        }
    }

    @FXML
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void generateToken() {
        String printer = printerSelection.getValue();
        if (printer == null || fileNameLabel.getText().equals("Click to upload your PDF file")) {
            showAlert("Error", "Please select a printer and upload a file!");
            return;
        }

        // ইউনিক টোকেন জেনারেট করা
        int token = DatabaseHandler.getNextTokenNumber();

        boolean success = DatabaseHandler.savePrintRequest(
                currentStudentId,
                printer,
                fileNameLabel.getText(),
                pageRangeBox.getValue() != null ? pageRangeBox.getValue() : "All",
                colorModeBox.getValue() != null ? colorModeBox.getValue() : "B&W",
                copiesSpinner.getValue(),
                pickupTimeBox.getValue() != null ? pickupTimeBox.getValue() : "ASAP",
                pickupDatePicker.getValue().toString(),
                token
        );

        if (success) {
            showAlert("Request Approved", "Your Token Number: T-" + token + "\nPlease collect it from " + printer);
            updateQueueDisplay();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        try {
            // নিশ্চিত করুন আপনার StudentDashboard.fxml ফাইলটি এই পাথে আছে
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/finallyy/StudentDashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 720));
            stage.setTitle("Student Dashboard");
            stage.show();
        } catch (IOException e) {
            System.err.println("Dashboard load failed!");
            e.printStackTrace();
        }
    }
}