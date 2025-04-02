package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.utils.PasswordHasher;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;

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
    @FXML private Label passwordStatusLabel;

    // Change Profile Picture tab
    @FXML private ImageView currentProfilePictureView;
    @FXML private Button selectImageButton;
    @FXML private Button saveImageButton;
    @FXML private Button resetImageButton;
    @FXML private Label imageStatusLabel;

    private Student loggedInStudent;
    private StudentDashboard parentController;
    private String studentId;
    private String selectedImagePath; // Temporary storage for the selected image path

    public void setParentController(StudentDashboard controller) {
        this.parentController = controller;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        this.loggedInStudent = UniversityManagementApp.studentDAO.getStudentById(studentId);
        loadStudentData();
    }

    @FXML
    public void initialize() {
        // Load the current profile picture when the controller is initialized
        if (loggedInStudent != null) {
            loadProfilePicture();
        }
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

        loadProfilePicture();
    }

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

    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!PasswordHasher.hashPassword(current).equals(loggedInStudent.getPassword())) {
            passwordStatusLabel.setText("Incorrect current password.");
            passwordStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            passwordStatusLabel.setText("Passwords do not match.");
            passwordStatusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        loggedInStudent.setPassword(newPass);
        UniversityManagementApp.studentDAO.updateStudent(loggedInStudent);
        passwordStatusLabel.setText("Password updated successfully!");
        passwordStatusLabel.setStyle("-fx-text-fill: green;");
        clearPasswordFields();
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleClearPassword() {
        clearPasswordFields();
        passwordStatusLabel.setText("");
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

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

            // Notify parent controller to refresh the profile view
            if (parentController != null) {
                parentController.refreshProfileTab();
            }

            selectedImagePath = null; // Clear the temporary path
            tabPane.getSelectionModel().select(0); // Return to Profile tab
        } catch (Exception e) {
            imageStatusLabel.setText("Error saving image: " + e.getMessage());
            imageStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

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

            // Notify parent controller to refresh the profile view
            if (parentController != null) {
                parentController.refreshProfileTab();
            }

            selectedImagePath = null; // Clear the temporary path
            tabPane.getSelectionModel().select(0); // Return to Profile tab
        } catch (Exception e) {
            imageStatusLabel.setText("Error resetting image: " + e.getMessage());
            imageStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Password Update");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}