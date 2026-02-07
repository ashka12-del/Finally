package org.example.finallyy;

public class Appointment {
    private int id;
    private String studentId, studentName, doctorName, symptoms, type, time, status;

    // কনস্ট্রাক্টর আপডেট করা হয়েছে যাতে নামও গ্রহণ করতে পারে
    public Appointment(int id, String studentId, String studentName, String doctorName, String symptoms, String type, String time, String status) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName; // নতুন ফিল্ড
        this.doctorName = doctorName;
        this.symptoms = symptoms;
        this.type = type;
        this.time = time;
        this.status = status;
    }

    public int getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; } // নতুন গেটার
    public String getDoctorName() { return doctorName; }
    public String getSymptoms() { return symptoms; }
    public String getType() { return type; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}