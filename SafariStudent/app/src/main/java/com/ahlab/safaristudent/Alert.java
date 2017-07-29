package com.ahlab.safaristudent;

/**
 * Created by Katherine on 29/7/17.
 */

public class Alert {

    String dateTime;
    String studentName;
    String alertType;
    String durationElapsed;

    public Alert(String dateTime, String studentName, String alertType, String durationElapsed) {
        this.dateTime = dateTime;
        this.studentName = studentName;
        this.alertType = alertType;
        this.durationElapsed = durationElapsed;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getAlertType() {
        return alertType;
    }

    public String getDurationElapsed() {
        return durationElapsed;
    }
}
