package com.example.universitymanagementapp.controller.FacultyController;


import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.dao.FacultyDAO;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.model.Faculty;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AdminFacultyController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<Faculty> allFacultyTable;

    @FXML
    private TableColumn<Faculty, String> nameColumn;

    @FXML
    private TableColumn<Faculty, String> facultyIdColumn;

    @FXML
    private TableColumn<Faculty, String> researchInterestColumn;

    @FXML
    private TextField facultySearch;

    @FXML
    private AnchorPane manageFacultyPane;

    @FXML
    private TextField nameField;

    @FXML
    private TextField facultyIdField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField degreeField;

    @FXML
    private TextField researchInterestField;

    @FXML
    private TextField officeLocationField;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button assignCoursesButton; // New button

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    // DAOs
    private FacultyDAO facultyDAO = UniversityManagementApp.facultyDAO;
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;

    // Exporter
    private ExExporter exporter = new ExExporter(courseDAO, UniversityManagementApp.studentDAO,
            facultyDAO, UniversityManagementApp.subjectDAO, UniversityManagementApp.eventDAO);

    // Observable lists
    private ObservableList<Faculty> allFacultyList = FXCollections.observableArrayList();
    private ObservableList<Faculty> filteredFacultyList = FXCollections.observableArrayList();
    private ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private Faculty selectedFaculty = null;

    @FXML
    public void initialize() {
        // Configure columns for All Faculty table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        facultyIdColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        researchInterestColumn.setCellValueFactory(new PropertyValueFactory<>("researchInterest"));

        // Set table resize policies
        allFacultyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all faculty
        loadAllFaculty();

        // Add listener to facultySearch TextField for real-time filtering
        facultySearch.textProperty().addListener((observable, oldValue, newValue) -> filterFaculty(newValue));

        // Enable/disable buttons based on selection
        allFacultyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            assignCoursesButton.setDisable(newSelection == null); // Enable/disable Assign Courses button
            deleteButton.setDisable(newSelection == null);
            selectedFaculty = newSelection; // Update selectedFaculty on single-click
        });

        // Set up mouse click handler for All Faculty table
        allFacultyTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) { // Double-click to show details
                Faculty selected = allFacultyTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showFacultyDetails(selected);
                }
            }
        });

        // Load available courses (for the Assign Courses dialog)
        availableCourses.addAll(courseDAO.getAllCourses());
    }

    private void loadAllFaculty() {
        allFacultyList.clear();
        allFacultyList.addAll(facultyDAO.getAllFaculty());
        filteredFacultyList.clear();
        filteredFacultyList.addAll(allFacultyList);
        allFacultyTable.setItems(filteredFacultyList);
    }

    private void filterFaculty(String searchText) {
        filteredFacultyList.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredFacultyList.addAll(allFacultyList);
        } else {
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Faculty faculty : allFacultyList) {
                if (faculty.getName().toLowerCase().contains(lowerCaseSearch) ||
                        faculty.getUsername().toLowerCase().contains(lowerCaseSearch)) {
                    filteredFacultyList.add(faculty);
                }
            }
        }
        allFacultyTable.setItems(filteredFacultyList);
    }

    private void showFacultyDetails(Faculty faculty) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Faculty Details: " + faculty.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Basic Info
        Label nameLabel = new Label("Name: " + faculty.getName());
        Label idLabel = new Label("Faculty ID: " + faculty.getUsername());
        Label emailLabel = new Label("Email: " + faculty.getEmail());
        Label degreeLabel = new Label("Degree: " + (faculty.getDegree() != null ? faculty.getDegree() : "Not set"));
        Label researchInterestLabel = new Label("Research Interest: " + (faculty.getResearchInterest() != null ? faculty.getResearchInterest() : "Not set"));
        Label officeLocationLabel = new Label("Office Location: " + (faculty.getOfficeLocation() != null ? faculty.getOfficeLocation() : "Not set"));
        Label passwordLabel = new Label("Password: " + (faculty.getPlaintextPassword() != null ? faculty.getPlaintextPassword() : "Not set"));

        // Assigned Courses (displaying course names directly)
        Label coursesLabel = new Label("Assigned Courses:");
        ListView<String> coursesListView = new ListView<>();
        ObservableList<String> courseDetails = FXCollections.observableArrayList();

        List<String> courseNames = faculty.getCoursesOffered();
        if (courseNames.isEmpty()) {
            courseDetails.add("No courses assigned.");
        } else {
            courseDetails.addAll(courseNames);
        }
        coursesListView.setItems(courseDetails);

        vbox.getChildren().addAll(
                nameLabel, idLabel, emailLabel, degreeLabel, researchInterestLabel, officeLocationLabel, passwordLabel,
                coursesLabel, coursesListView
        );

        Scene scene = new Scene(vbox, 400, 450);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    @FXML
    private void handleAddFaculty() {
        handleClearForm();
        selectedFaculty = null;
        tabPane.getSelectionModel().select(1);
        if (nameField != null) {
            nameField.requestFocus();
        }
    }

    @FXML
    private void handleEditFaculty() {
        if (selectedFaculty != null) {
            nameField.setText(selectedFaculty.getName());
            facultyIdField.setText(selectedFaculty.getUsername());
            passwordField.setText(selectedFaculty.getPlaintextPassword());
            emailField.setText(selectedFaculty.getEmail());
            degreeField.setText(selectedFaculty.getDegree());
            researchInterestField.setText(selectedFaculty.getResearchInterest());
            officeLocationField.setText(selectedFaculty.getOfficeLocation());

            // Switch to Manage Faculty tab (no course assignment here anymore)
            tabPane.getSelectionModel().select(1);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a faculty member to edit.");
        }
    }

    @FXML
    private void handleAssignCourses() {
        if (selectedFaculty == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a faculty member to assign courses.");
            return;
        }

        // Create a dialog for assigning/unassigning courses
        Stage assignStage = new Stage();
        assignStage.setTitle("Assign Courses to " + selectedFaculty.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // ComboBox for selecting courses to assign
        Label assignLabel = new Label("Select Course to Assign:");
        ComboBox<Course> courseComboBox = new ComboBox<>();
        courseComboBox.setItems(availableCourses);
        courseComboBox.setPromptText("Select Course");
        courseComboBox.setVisibleRowCount(5);

        // TableView for assigned courses
        Label assignedLabel = new Label("Assigned Courses:");
        TableView<Course> assignedCoursesTable = new TableView<>();
        TableColumn<Course, String> courseCodeColumn = new TableColumn<>("Course Code");
        TableColumn<Course, String> courseNameColumn = new TableColumn<>("Course Name");
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        assignedCoursesTable.getColumns().addAll(courseCodeColumn, courseNameColumn);
        assignedCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load currently assigned courses
        ObservableList<Course> assignedCoursesList = FXCollections.observableArrayList();
        List<String> courseNames = selectedFaculty.getCoursesOffered();
        for (String courseName : courseNames) {
            Course course = courseDAO.getAllCourses().stream()
                    .filter(c -> c.getCourseName().equals(courseName))
                    .findFirst().orElse(null);
            if (course != null) {
                assignedCoursesList.add(course);
            }
        }
        assignedCoursesTable.setItems(assignedCoursesList);

        // Buttons for assigning and unassigning
        Button assignButton = new Button("Assign");
        Button unassignButton = new Button("Unassign");
        Button saveButton = new Button("Save");
        HBox buttonBox = new HBox(10, assignButton, unassignButton, saveButton);

        // Enable/disable unassign button based on selection
        unassignButton.setDisable(true);
        assignedCoursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            unassignButton.setDisable(newSelection == null);
        });

        // Assign button action
        assignButton.setOnAction(e -> {
            Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
            if (selectedCourse != null && !assignedCoursesList.contains(selectedCourse)) {
                assignedCoursesList.add(selectedCourse);
                assignedCoursesTable.setItems(assignedCoursesList);
            }
        });

        // Unassign button action
        unassignButton.setOnAction(e -> {
            Course selectedCourse = assignedCoursesTable.getSelectionModel().getSelectedItem();
            if (selectedCourse != null) {
                assignedCoursesList.remove(selectedCourse);
                assignedCoursesTable.setItems(assignedCoursesList);
            }
        });

        // Save button action
        saveButton.setOnAction(e -> {
            // Update the faculty's coursesOffered with the new list of course names
            List<String> newCoursesOffered = new ArrayList<>();
            for (Course course : assignedCoursesList) {
                newCoursesOffered.add(course.getCourseName());
            }

            // Update the instructor for each course
            List<String> oldCourses = selectedFaculty.getCoursesOffered();
            for (String oldCourseName : oldCourses) {
                if (!newCoursesOffered.contains(oldCourseName)) {
                    Course course = courseDAO.getAllCourses().stream()
                            .filter(c -> c.getCourseName().equals(oldCourseName))
                            .findFirst().orElse(null);
                    if (course != null) {
                        course.setInstructor("Unassigned");
                        courseDAO.updateCourse(course);
                    }
                }
            }
            for (Course course : assignedCoursesList) {
                course.setInstructor(selectedFaculty.getUsername());
                courseDAO.updateCourse(course);
            }

            // Update the faculty object
            selectedFaculty.setCoursesOffered(newCoursesOffered);
            facultyDAO.updateFaculty(selectedFaculty.getUsername(), selectedFaculty);
            exporter.exportData();
            assignStage.close();
        });

        vbox.getChildren().addAll(assignLabel, courseComboBox, assignedLabel, assignedCoursesTable, buttonBox);
        Scene scene = new Scene(vbox, 400, 400);
        assignStage.setScene(scene);
        assignStage.show();
    }

    @FXML
    private void handleDeleteFaculty() {
        Faculty selected = allFacultyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder message = new StringBuilder("Are you sure you want to delete the faculty member '")
                    .append(selected.getName())
                    .append("' (")
                    .append(selected.getUsername())
                    .append(")?");

            List<String> courseNames = selected.getCoursesOffered();
            if (!courseNames.isEmpty()) {
                message.append("\n\nThis faculty member will be unassigned from the following courses:\n");
                for (String courseName : courseNames) {
                    Course course = courseDAO.getAllCourses().stream()
                            .filter(c -> c.getCourseName().equals(courseName))
                            .findFirst().orElse(null);
                    if (course != null) {
                        message.append("- ").append(courseName).append(" (Code: ").append(course.getCourseCode()).append(")\n");
                    } else {
                        message.append("- ").append(courseName).append(" (Not found)\n");
                    }
                }
            } else {
                message.append("\n\nThis faculty member is not assigned to any courses.");
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    for (String courseName : courseNames) {
                        Course course = courseDAO.getAllCourses().stream()
                                .filter(c -> c.getCourseName().equals(courseName))
                                .findFirst().orElse(null);
                        if (course != null) {
                            course.setInstructor("Unassigned");
                            courseDAO.updateCourse(course);
                        }
                    }
                    facultyDAO.deleteFacultyById(selected.getUsername());
                    loadAllFaculty();
                    filterFaculty(facultySearch.getText());
                    exporter.exportData();
                }
            });
        }
    }

    @FXML
    private void handleSaveFaculty() {
        String name = nameField.getText().trim();
        String facultyId = facultyIdField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String degree = degreeField.getText().trim();
        String researchInterest = researchInterestField.getText().trim();
        String officeLocation = officeLocationField.getText().trim();

        if (name.isEmpty() || facultyId.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, Faculty ID, and Email are required.");
            return;
        }

        for (Faculty faculty : facultyDAO.getAllFaculty()) {
            if (selectedFaculty != null && faculty.getUsername().equals(selectedFaculty.getUsername())) {
                continue;
            }
            if (faculty.getUsername().equals(facultyId) || faculty.getEmail().equals(email)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Faculty ID or Email already exists.");
                clearForm();
                return;
            }
        }

        Faculty faculty = new Faculty(
                facultyId,
                password.isEmpty() ? "defaultPassword" : password,
                name,
                email,
                degree,
                researchInterest,
                selectedFaculty != null ? selectedFaculty.getCoursesOffered() : new ArrayList<>(), // Retain existing courses
                officeLocation
        );

        if (selectedFaculty == null) {
            facultyDAO.addFaculty(faculty);
        } else {
            facultyDAO.updateFaculty(selectedFaculty.getUsername(), faculty);
            selectedFaculty = null;
        }

        loadAllFaculty();
        filterFaculty(facultySearch.getText());
        clearForm();
        exporter.exportData();
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        selectedFaculty = null;
    }

    private void clearForm() {
        nameField.clear();
        facultyIdField.clear();
        passwordField.clear();
        emailField.clear();
        degreeField.clear();
        researchInterestField.clear();
        officeLocationField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}