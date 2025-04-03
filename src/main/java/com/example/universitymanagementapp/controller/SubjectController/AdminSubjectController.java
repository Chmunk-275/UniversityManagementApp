package com.example.universitymanagementapp.controller.SubjectController;

import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.dao.StudentDAO;
import com.example.universitymanagementapp.dao.SubjectDAO;
import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.model.Subject;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AdminSubjectController {

    // All changes shown in activity

    @FXML
    public TableView<Subject> allSubjectsTable;

    @FXML
    public TableColumn<Subject, String> subjectNameColumn;

    @FXML
    public TableColumn<Subject, String> subjectCodeColumn;

    @FXML
    public Button addButton;

    @FXML
    public Button editButton;

    @FXML
    public Button deleteButton;

    @FXML
    public TextField subjectSearch;

    @FXML
    public TextField subjectNameField;

    @FXML
    public TextField subjectCodeField;

    @FXML
    public Button saveButton;

    @FXML
    public Button clearButton;

    @FXML
    public TabPane tabPane;

    // Creating DAOs
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;
    private SubjectDAO subjectDAO = UniversityManagementApp.subjectDAO;

    // Creating Exporter constructor
    private ExExporter exporter = new ExExporter(courseDAO, UniversityManagementApp.studentDAO,
            UniversityManagementApp.facultyDAO, subjectDAO, UniversityManagementApp.eventDAO);

    // Creating observable lists
    private ObservableList<Subject> allSubjectsList = FXCollections.observableArrayList();
    private ObservableList<Subject> filteredSubjectsList = FXCollections.observableArrayList();
    private Subject selectedSubject = null; // For editing

    @FXML
    public void initialize() {
        // Configure columns for All Subjects table
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));

        // Set table resize policy
        allSubjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all subjects
        loadAllSubjects();

        // Add listener to subjectSearch TextField for real-time filtering
        subjectSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSubjects(newValue);
        });

        // Enable/disable edit/delete buttons based on selection
        allSubjectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
        });
    }

    private void loadAllSubjects() {
        allSubjectsList.clear();
        allSubjectsList.addAll(subjectDAO.getAllSubjects());
        filteredSubjectsList.clear();
        filteredSubjectsList.addAll(allSubjectsList); // Initially, show all subjects
        allSubjectsTable.setItems(filteredSubjectsList);
        System.out.println("Loaded all subjects: " + allSubjectsList);
    }

    private void filterSubjects(String searchText) {
        filteredSubjectsList.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredSubjectsList.addAll(allSubjectsList); // Show all subjects if search is empty
        } else {
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Subject subject : allSubjectsList) {
                if (subject.getSubjectName().toLowerCase().contains(lowerCaseSearch) ||
                        subject.getSubjectCode().toLowerCase().contains(lowerCaseSearch)) {
                    filteredSubjectsList.add(subject);
                }
            }
        }
        allSubjectsTable.setItems(filteredSubjectsList);
        System.out.println("Filtered subjects for '" + searchText + "': " + filteredSubjectsList);
    }

    @FXML
    private void handleAddSubject() {
        clearForm(); // Clear form for new subject
        selectedSubject = null; // Reset selection
        // Switch to the "Manage Subjects" tab (index 1, since "Search" tab is removed)
        tabPane.getSelectionModel().select(1);
        // Set focus on the subjectNameField
        if (subjectNameField != null) {
            subjectNameField.requestFocus();
        } else {
            System.out.println("subjectNameField is null. Check FXML injection.");
        }
    }

    @FXML
    private void handleEditSubject() {
        selectedSubject = allSubjectsTable.getSelectionModel().getSelectedItem();
        if (selectedSubject != null) {
            subjectNameField.setText(selectedSubject.getSubjectName());
            subjectCodeField.setText(selectedSubject.getSubjectCode());
            // Switch to the "Manage Subjects" tab (index 1)
            tabPane.getSelectionModel().select(1);
        }
    }

    @FXML
    private void handleDeleteSubject() {
        Subject selected = allSubjectsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Get all courses associated with this subject
            List<Course> associatedCourses = courseDAO.getCoursesBySubject(selected.getSubjectCode());

            // Build the confirmation message
            StringBuilder message = new StringBuilder("Are you sure you want to delete the subject '")
                    .append(selected.getSubjectName())
                    .append("' (")
                    .append(selected.getSubjectCode())
                    .append(")?");

            if (!associatedCourses.isEmpty()) {
                message.append("\n\nThe following courses will also be deleted:\n");
                for (Course course : associatedCourses) {
                    message.append("- ")
                            .append(course.getCourseName())
                            .append(" (")
                            .append(course.getCourseCode())
                            .append(")\n");
                }
            } else {
                message.append("\n\nNo courses are associated with this subject.");
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message.toString(), ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Step 1: Unenroll all students from the associated courses
                    for (Course course : associatedCourses) {
                        List<Student> enrolledStudents = studentDAO.getStudentsEnrolledInCourse(course.getCourseCode());
                        for (Student student : enrolledStudents) {
                            studentDAO.removeStudentFromCourse(student, course);
                            System.out.println("Unenrolled student " + student.getStudentId() + " from course " + course.getCourseCode());
                        }
                    }

                    // Step 2: Remove the subject from students' registeredSubjects list
                    String subjectCode = selected.getSubjectCode();
                    for (Student student : studentDAO.getAllStudents()) {
                        // Check if the student is still enrolled in any courses with this subject
                        boolean hasOtherCoursesForSubject = student.getRegisteredCourses().stream()
                                .anyMatch(course -> course.getSubjectCode().equals(subjectCode));
                        if (!hasOtherCoursesForSubject) {
                            // If no other courses for this subject, remove the subject from registeredSubjects
                            student.getRegisteredSubjects().remove(subjectCode);
                            System.out.println("Removed subject " + subjectCode + " from student " + student.getStudentId() + "'s registered subjects");
                            // Update the student in the DAO to persist the change
                            studentDAO.updateStudent(student);
                        }
                    }

                    // Step 3: Delete the subject and associated courses
                    courseDAO.removeCoursesBySubject(selected.getSubjectCode());
                    subjectDAO.removeSubject(selected.getSubjectName());

                    // Refresh the UI
                    loadAllSubjects();
                    filterSubjects(subjectSearch.getText()); // Refresh the filtered list
                    ExExporter.recordActivity("Subject", "Subject" + selectedSubject.getSubjectName() + "(" + selectedSubject.getSubjectCode() + ")" + " deleted");
                    exporter.exportData(); // Export after deleting subject and associated courses
                }
            });
        }
    }

    @FXML
    private void handleSaveSubject() {
        String name = subjectNameField.getText().trim();
        String code = subjectCodeField.getText().trim();

        // Validate required fields
        if (name.isEmpty() || code.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Subject name and code are required.");
            return;
        }

        // Check for duplicate subject name or code
        for (Subject subject : subjectDAO.getAllSubjects()) {
            // Skip the current subject being edited (if editing)
            if (selectedSubject != null && subject.getSubjectCode().equals(selectedSubject.getSubjectCode())) {
                continue;
            }
            // Check if the name or code already exists (for another subject)
            if (subject.getSubjectName().equals(name) || subject.getSubjectCode().equals(code)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Subject name or code already exists.");
                clearForm();
                return;
            }
        }

        if (selectedSubject == null) {
            // Add new subject
            Subject newSubject = new Subject(name, code);
            subjectDAO.addSubject(newSubject);
            ExExporter.recordActivity("Subject", "Subject " + name + "(" + code + ")" + " added");
        } else {
            // Update existing subject
            String originalSubjectCode = selectedSubject.getSubjectCode(); // Store the original subject code
            selectedSubject.setSubjectName(name);
            selectedSubject.setSubjectCode(code);
            subjectDAO.updateSubject(originalSubjectCode, selectedSubject);
            ExExporter.recordActivity("Subject", "Subject " + originalSubjectCode + "(" + name + ")" + " updated to " + code);
            selectedSubject = null;
        }

        loadAllSubjects();
        filterSubjects(subjectSearch.getText()); // Refresh the filtered list
        clearForm();
        exporter.exportData(); // Export after adding or updating a subject
        tabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        selectedSubject = null;
    }

    private void clearForm() {
        subjectNameField.clear();
        subjectCodeField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
