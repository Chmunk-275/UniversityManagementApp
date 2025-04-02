package com.example.universitymanagementapp.model;

import javafx.scene.image.Image;
import java.util.List;

public class Faculty extends User {
    private String name;
    private String email;
    private String degree;
    private String researchInterest;
    private List<String> coursesOffered;
    private String officeLocation;
    private Image profilePicture; // Add profile picture field
    private String profilePicturePath; // Add profile picture path field

    // Constructor
    public Faculty(String username, String password, String name, String email, String degree, String researchInterest,
                   List<String> coursesOffered, String officeLocation, Image profilePicture, String profilePicturePath) {
        super(username, password, "Faculty");
        this.name = name;
        this.email = email;
        this.degree = degree;
        this.researchInterest = researchInterest;
        this.coursesOffered = coursesOffered;
        this.officeLocation = officeLocation;
        this.profilePicture = profilePicture;
        this.profilePicturePath = profilePicturePath;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getResearchInterest() {
        return researchInterest;
    }

    public void setResearchInterest(String researchInterest) {
        this.researchInterest = researchInterest;
    }

    public List<String> getCoursesOffered() {
        return coursesOffered;
    }

    public void setCoursesOffered(List<String> coursesOffered) {
        this.coursesOffered = coursesOffered;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public Image getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Image profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    @Override
    public String toString() {
        return "Faculty\n" +
                "username='" + getUsername() + "\n" +
                "password='" + getPassword() + "\n" +
                "name='" + name + "\n" +
                "email='" + email + "\n" +
                "degree='" + degree + "\n" +
                "researchInterest='" + researchInterest + "\n" +
                "coursesOffered=" + coursesOffered + "\n" +
                "officeLocation='" + officeLocation + "\n" +
                "profilePicturePath='" + profilePicturePath + "\n";
    }
}