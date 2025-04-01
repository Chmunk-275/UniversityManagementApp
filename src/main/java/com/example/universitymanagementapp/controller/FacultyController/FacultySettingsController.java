package com.example.universitymanagementapp.controller.FacultyController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Faculty;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FacultySettingsController {

    // === Profile Tab ===
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField maskedPasswordField;
    @FXML private TextField profileLocationField;
    @FXML private Button saveProfileButton;
    @FXML private Button clearProfileButton;

    // === Change Password Tab ===
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;

    // === Change Location Tab ===
    @FXML private TextField currentLocationField;
    @FXML private TextField newLocationField;
    @FXML private TextField confirmLocationField;
    @FXML private Button changeLocationButton;
    @FXML private Button clearLocationButton;

    private FacultyDashboard parentController;
    private Faculty loggedInFaculty;

    public void setParentController(FacultyDashboard controller) {
        this.parentController = controller;
        this.loggedInFaculty = UniversityManagementApp.facultyDAO.getFacultyByUsername(controller.getFacultyUsername());
        loadFacultyData();
    }

    private void loadFacultyData() {
        if (loggedInFaculty == null) return;

        nameField.setText(loggedInFaculty.getName());
        usernameField.setText(loggedInFaculty.getUsername());
        maskedPasswordField.setText(loggedInFaculty.getPlaintextPassword());
        profileLocationField.setText(loggedInFaculty.getOfficeLocation());
        currentLocationField.setText(loggedInFaculty.getOfficeLocation());

    }

    // === Profile Tab ===
    @FXML
    private void handleSaveProfile() {
        showInfo("This section is view-only for now. Location changes belong in the third tab.");
    }

    @FXML
    private void handleClearProfile() {
        loadFacultyData();
    }

    // === Change Password ===
    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        if (!current.equals(loggedInFaculty.getPlaintextPassword())) {
            showError("Current password is incorrect.");
            return;
        }
        if (newPass.isEmpty() || !newPass.equals(confirm)) {
            showError("New passwords do not match or are empty.");
            return;
        }

        loggedInFaculty.setPassword(newPass);
        UniversityManagementApp.facultyDAO.updateFaculty(loggedInFaculty.getUsername(), loggedInFaculty);
        exportAndAlert("Password updated successfully!");

        clearPasswordFields();
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

    // === Change Location ===
    @FXML
    private void handleChangeLocation() {
        String newLoc = newLocationField.getText().trim();
        String confirmLoc = confirmLocationField.getText().trim();

        if (newLoc.isEmpty() || !newLoc.equals(confirmLoc)) {
            showError("New location is empty or does not match confirmation.");
            return;
        }

        loggedInFaculty.setOfficeLocation(newLoc);
        UniversityManagementApp.facultyDAO.updateFaculty(loggedInFaculty.getUsername(), loggedInFaculty);
        exportAndAlert("Office location updated successfully!");

        clearLocationFields();
        loadFacultyData(); // refresh profile tab
    }

    @FXML
    private void handleClearLocation() {
        clearLocationFields();
    }

    private void clearLocationFields() {
        newLocationField.clear();
        confirmLocationField.clear();
    }

    // === Utility Methods ===
    private void exportAndAlert(String msg) {
        ExExporter exporter = new ExExporter(
                UniversityManagementApp.courseDAO,
                UniversityManagementApp.studentDAO,
                UniversityManagementApp.facultyDAO,
                UniversityManagementApp.subjectDAO,
                UniversityManagementApp.eventDAO
        );
        exporter.exportData();
        showInfo(msg);
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
