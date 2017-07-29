package com.ahlab.safaristudent;

/**
 * Created by Katherine on 24/7/17.
 */

public class Log {

    String dateTime;
    String studentName;
    String qrName;
    String qrContent;

    public Log(String dateTime, String studentName, String qrName, String qrContent) {
        this.dateTime = dateTime;
        this.studentName = studentName;
        this.qrName = qrName;
        this.qrContent = qrContent;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getqrName() {
        return qrName;
    }

    public String getqrContent() {
        return qrContent;
    }
}
