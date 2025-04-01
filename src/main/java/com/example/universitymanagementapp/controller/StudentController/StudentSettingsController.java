package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.utils.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class StudentSettingsController {
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField semesterField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;
    @FXML private Label passwordStatusLabel;

    private Student loggedInStudent;
    private StudentDashboard parentController;
    private String studentId;

    public void setParentController(StudentDashboard controller) {
        this.parentController = controller;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        this.loggedInStudent = UniversityManagementApp.studentDAO.getStudentById(studentId);
        loadStudentData();
    }

    private void loadStudentData() {
        if (loggedInStudent == null) return;

        nameField.setText(loggedInStudent.getName());
        usernameField.setText(loggedInStudent.getUsername());
        passwordField.setText(loggedInStudent.getPlaintextPassword());
        emailField.setText(loggedInStudent.getEmail());
        phoneField.setText(loggedInStudent.getPhoneNumber());
        addressField.setText(loggedInStudent.getAddress());
        semesterField.setText(loggedInStudent.getCurrentSemester());
    }

    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!PasswordHasher.hashPassword(current).equals(loggedInStudent.getPassword())) {
            passwordStatusLabel.setText("Incorrect current password.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            passwordStatusLabel.setText("Passwords do not match.");
            return;
        }

        loggedInStudent.setPassword(newPass);
        UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
        passwordStatusLabel.setText("Password updated successfully.");
        clearPasswordFields();
    }

    @FXML
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        passwordStatusLabel.setText("");
    }
}
