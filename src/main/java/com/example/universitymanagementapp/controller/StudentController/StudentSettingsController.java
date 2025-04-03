package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.utils.ExExporter;
import com.example.universitymanagementapp.utils.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

public class StudentSettingsController {

    // UI Elements
    @FXML private TabPane tabPane;

    // Profile fields
    @FXML private TextField nameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField maskedPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField semesterField;

    // Password change fields
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Button clearPasswordButton;

    // Profile picture section
    @FXML private ImageView currentProfilePictureView;
    @FXML private Button selectImageButton;
    @FXML private Button saveImageButton;
    @FXML private Button resetImageButton;
    @FXML private Label imageStatusLabel;

    // Logged-in student reference and other helpers
    private Student loggedInStudent;
    private StudentDashboard parentController;
    private String studentId;
    private String selectedImagePath;

    // Exporter to save any updates made
    ExExporter exporter = new ExExporter(
            UniversityManagementApp.courseDAO,
            UniversityManagementApp.studentDAO,
            UniversityManagementApp.facultyDAO,
            UniversityManagementApp.subjectDAO,
            UniversityManagementApp.eventDAO
    );

    // Sets the dashboard controller reference
    public void setParentController(StudentDashboard controller) {
        this.parentController = controller;
    }

    // Set student ID and load their data
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        this.loggedInStudent = UniversityManagementApp.studentDAO.getStudentById(studentId);
        loadStudentData();
    }

    // Initial load (profile picture)
    @FXML
    public void initialize() {
        if (loggedInStudent != null) {
            loadProfilePicture();
        }
    }

    // Load student data into fields
    private void loadStudentData() {
        if (loggedInStudent == null) return;

        nameField.setText(loggedInStudent.getName());
        usernameField.setText(loggedInStudent.getUsername());
        maskedPasswordField.setText(loggedInStudent.getPlaintextPassword());
        emailField.setText(loggedInStudent.getEmail());
        phoneField.setText(loggedInStudent.getPhoneNumber());
        addressField.setText(loggedInStudent.getAddress());
        semesterField.setText(loggedInStudent.getCurrentSemester());

        loadProfilePicture();
    }

    // Display student profile picture or default if none
    private void loadProfilePicture() {
        if (loggedInStudent.getProfilePicture() != null) {
            currentProfilePictureView.setImage(loggedInStudent.getProfilePicture());
        } else {
            try {
                currentProfilePictureView.setImage(new Image(getClass().getResourceAsStream("/images/default.jpg")));
            } catch (Exception e) {
                System.out.println("Error loading default profile picture: " + e.getMessage());
                currentProfilePictureView.setImage(null);
            }
        }
    }

    // Handle Password Change
    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        // Check if current password is correct
        if (!PasswordHasher.hashPassword(current).equals(loggedInStudent.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Incorrect current password.");
            return;
        }

        // Check if new password matches confirmation
        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.WARNING, "New passwords do not match.");
            return;
        }

        // Update password and export
        loggedInStudent.setPassword(newPass);
        UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
        showAlert(Alert.AlertType.INFORMATION, "Password updated successfully!");
        clearPasswordFields();
        exporter.exportData();
        tabPane.getSelectionModel().select(0); // Go back to Profile tab
    }

    // Clear all password fields
    @FXML
    private void handleClearPassword() {
        clearPasswordFields();
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    // Profile Picture Handling

    // Select new image
    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                selectedImagePath = selectedFile.toURI().toString();
                Image newImage = new Image(selectedImagePath);
                currentProfilePictureView.setImage(newImage);
                imageStatusLabel.setText("Image selected. Click 'Save' to update.");
                imageStatusLabel.setStyle("-fx-text-fill: blue;");
            } catch (Exception e) {
                imageStatusLabel.setText("Error loading image: " + e.getMessage());
                imageStatusLabel.setStyle("-fx-text-fill: red;");
            }
        }
    }

    // Save selected image as profile picture
    @FXML
    private void handleSaveImage() {
        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            imageStatusLabel.setText("No image selected.");
            imageStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            Image newImage = new Image(selectedImagePath);
            loggedInStudent.setProfilePicture(newImage);
            loggedInStudent.setProfilePicturePath(selectedImagePath);
            UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
            imageStatusLabel.setText("Profile picture updated successfully!");
            imageStatusLabel.setStyle("-fx-text-fill: green;");

            if (parentController != null) {
                parentController.refreshProfileTab();
            }

            exporter.exportData();
            selectedImagePath = null;
            tabPane.getSelectionModel().select(0); // Return to Profile tab
        } catch (Exception e) {
            imageStatusLabel.setText("Error saving image: " + e.getMessage());
            imageStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Reset to default profile picture
    @FXML
    private void handleResetImage() {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/images/default.jpg"));
            loggedInStudent.setProfilePicture(defaultImage);
            loggedInStudent.setProfilePicturePath("default");
            UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
            currentProfilePictureView.setImage(defaultImage);
            imageStatusLabel.setText("Profile picture reset to default.");
            imageStatusLabel.setStyle("-fx-text-fill: green;");

            if (parentController != null) {
                parentController.refreshProfileTab();
            }

            exporter.exportData();
            selectedImagePath = null;
            tabPane.getSelectionModel().select(0);
        } catch (Exception e) {
            imageStatusLabel.setText("Error resetting image: " + e.getMessage());
            imageStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Reusable popup alert
    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Password Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
