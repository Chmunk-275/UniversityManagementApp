package com.example.universitymanagementapp.controller.FacultyController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Faculty;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

public class FacultySettingsController {

    // UI elements for different tabs
    @FXML private TabPane tabPane;

    // Profile fields
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField maskedPasswordField;
    @FXML private TextField profileLocationField;
    @FXML private TextField profileResearchField;
    @FXML private TextField profileEmailField;
    @FXML private TextField profileDegreeField;
    @FXML private Button saveProfileButton;
    @FXML private Button clearProfileButton;
    @FXML private ImageView profilePictureView;

    // Password change fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;

    // Location change fields
    @FXML private TextField currentLocationField;
    @FXML private TextField newLocationField;
    @FXML private TextField confirmLocationField;
    @FXML private Button changeLocationButton;
    @FXML private Button clearLocationButton;

    // Profile picture update fields
    @FXML private ImageView currentProfilePictureView;
    @FXML private TextField newProfilePicturePathField;
    @FXML private Button browsePictureButton;
    @FXML private Button changeProfilePictureButton;
    @FXML private Button clearProfilePictureButton;

    // Faculty and utilities
    private FacultyDashboard parentController;
    private Faculty loggedInFaculty;
    private File selectedImageFile;

    // Exporter to save updated data
    ExExporter exporter = new ExExporter(
            UniversityManagementApp.courseDAO,
            UniversityManagementApp.studentDAO,
            UniversityManagementApp.facultyDAO,
            UniversityManagementApp.subjectDAO,
            UniversityManagementApp.eventDAO
    );

    // Set the parent controller and load faculty data
    public void setParentController(FacultyDashboard controller) {
        this.parentController = controller;
        this.loggedInFaculty = UniversityManagementApp.facultyDAO.getFacultyByUsername(controller.getFacultyUsername());
        loadFacultyData();
    }

    // Load faculty info into the fields
    private void loadFacultyData() {
        if (loggedInFaculty == null) return;

        nameField.setText(loggedInFaculty.getName());
        usernameField.setText(loggedInFaculty.getUsername());
        maskedPasswordField.setText(loggedInFaculty.getPlaintextPassword());
        profileLocationField.setText(loggedInFaculty.getOfficeLocation());
        currentLocationField.setText(loggedInFaculty.getOfficeLocation());
        profileResearchField.setText(loggedInFaculty.getResearchInterest());
        profileEmailField.setText(loggedInFaculty.getEmail());
        profileDegreeField.setText(loggedInFaculty.getDegree());

        // Load profile picture or set default
        if (loggedInFaculty.getProfilePicture() != null) {
            profilePictureView.setImage(loggedInFaculty.getProfilePicture());
            currentProfilePictureView.setImage(loggedInFaculty.getProfilePicture());
        } else {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            profilePictureView.setImage(defaultImage);
            currentProfilePictureView.setImage(defaultImage);
        }
    }

    // Handles clicking "Save" on profile tab
    @FXML
    private void handleSaveProfile() {
        showAlert(Alert.AlertType.INFORMATION, "This section is view-only. Use the Change Location tab to update.");
    }

    // Resets profile tab fields
    @FXML
    private void handleClearProfile() {
        loadFacultyData();
    }

    // Handle password update
    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText().trim();
        String newPass = newPasswordField.getText().trim();
        String confirm = confirmPasswordField.getText().trim();

        // Check if current password matches
        if (!current.equals(loggedInFaculty.getPlaintextPassword())) {
            showAlert(Alert.AlertType.ERROR, "Incorrect current password.");
            return;
        }

        // Check if new passwords match
        if (newPass.isEmpty() || !newPass.equals(confirm)) {
            showAlert(Alert.AlertType.WARNING, "New passwords do not match.");
            return;
        }

        // Save updated password
        loggedInFaculty.setPassword(newPass);
        UniversityManagementApp.facultyDAO.updateFaculty(loggedInFaculty.getUsername(), loggedInFaculty);
        exporter.exportData();

        clearPasswordFields();
        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully!");
        tabPane.getSelectionModel().select(0);
    }

    // Clears all password fields
    @FXML
    private void handleClearPassword() {
        clearPasswordFields();
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    // Handle location update
    @FXML
    private void handleChangeLocation() {
        String newLoc = newLocationField.getText().trim();
        String confirmLoc = confirmLocationField.getText().trim();

        // Check if new location is valid
        if (newLoc.isEmpty() || !newLoc.equals(confirmLoc)) {
            showAlert(Alert.AlertType.WARNING, "New location is empty or does not match confirmation.");
            return;
        }

        // Save new location
        loggedInFaculty.setOfficeLocation(newLoc);
        UniversityManagementApp.facultyDAO.updateFaculty(loggedInFaculty.getUsername(), loggedInFaculty);
        exporter.exportData();

        clearLocationFields();
        loadFacultyData(); // Refresh profile info
        showAlert(Alert.AlertType.INFORMATION, "Office location updated successfully!");
        tabPane.getSelectionModel().select(0);
    }

    // Clears location update fields
    @FXML
    private void handleClearLocation() {
        clearLocationFields();
    }

    private void clearLocationFields() {
        newLocationField.clear();
        confirmLocationField.clear();
    }

    // Browse for a new profile picture
    @FXML
    private void handleBrowsePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            newProfilePicturePathField.setText(selectedImageFile.getAbsolutePath());
        }
    }

    // Save selected profile picture
    @FXML
    private void handleChangeProfilePicture() {
        if (selectedImageFile == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a new profile picture.");
            return;
        }

        try {
            String imagePath = selectedImageFile.toURI().toString();
            Image newImage = new Image(imagePath);
            loggedInFaculty.setProfilePicture(newImage);
            loggedInFaculty.setProfilePicturePath(selectedImageFile.getAbsolutePath());
            UniversityManagementApp.facultyDAO.updateFaculty(loggedInFaculty.getUsername(), loggedInFaculty);
            exporter.exportData();

            clearProfilePictureFields();
            loadFacultyData();
            showAlert(Alert.AlertType.INFORMATION, "Profile picture updated successfully!");
            tabPane.getSelectionModel().select(0);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update picture: " + e.getMessage());
        }
    }

    // Clear profile picture selection
    @FXML
    private void handleClearProfilePicture() {
        clearProfilePictureFields();
    }

    private void clearProfilePictureFields() {
        newProfilePicturePathField.clear();
        selectedImageFile = null;
    }

    // Reusable alert method for info, warning, or error messages
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Faculty Settings");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
