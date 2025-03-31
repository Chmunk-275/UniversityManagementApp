package com.example.universitymanagementapp.model;

import java.time.LocalDateTime;

public class Registration {
    private String studentId;
    private int courseCode;
    private String courseName;
    private LocalDateTime date;

    public Registration(String studentId, String courseName, int courseCode, LocalDateTime date) {
        this.studentId = studentId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.date = date;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCourseCode() {
        return courseCode;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
