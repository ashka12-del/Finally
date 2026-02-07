package org.example.finallyy;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class to represent a Printing Request.
 * Updated to support JavaFX TableView properties and Database integration.
 */
public class PrintRequest {
    private final StringProperty token;
    private final StringProperty studentId;
    private final StringProperty details;
    private final StringProperty completionTime; // এটিই টেবিল-এ Pickup Info হিসেবে শো করবে

    // Constructor আপডেট করা হয়েছে যাতে ৪টি প্যারামিটার গ্রহণ করে
    public PrintRequest(String token, String studentId, String details, String completionTime) {
        this.token = new SimpleStringProperty(token);
        this.studentId = new SimpleStringProperty(studentId);
        this.details = new SimpleStringProperty(details);
        this.completionTime = new SimpleStringProperty(completionTime);
    }

    // JavaFX Property Getters (TableView এর PropertyValueFactory এর জন্য এগুলো অত্যাবশ্যক)
    public StringProperty tokenProperty() { return token; }
    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty detailsProperty() { return details; }
    public StringProperty completionTimeProperty() { return completionTime; }

    // Standard Getters (নরমাল ব্যবহারের জন্য)
    public String getToken() { return token.get(); }
    public String getStudentId() { return studentId.get(); }
    public String getDetails() { return details.get(); }
    public String getCompletionTime() { return completionTime.get(); }

    // Setter (শপকিপার যখন 'Mark Done' ক্লিক করবে বা ডাটা লোড হবে তখন এটি ব্যবহার হবে)
    public void setCompletionTime(String completionTime) {
        this.completionTime.set(completionTime);
    }
}