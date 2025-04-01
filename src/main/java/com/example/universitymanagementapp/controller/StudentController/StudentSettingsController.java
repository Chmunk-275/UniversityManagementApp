package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.utils.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class StudentSettingsController {

    @FXML private TabPane tabPane;
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField maskedPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField semesterField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;

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
        maskedPasswordField.setText(loggedInStudent.getPlaintextPassword());
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
            showAlert(Alert.AlertType.ERROR, "Incorrect current password.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "Passwords do not match.");
            return;
        }

        loggedInStudent.setPassword(newPass);
        UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully!");
        clearPasswordFields();
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleClearPassword() {
        clearPasswordFields();
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Password Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}