package com.example.universitymanagementapp.controller.CourseController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.StudentController.StudentDashboard;
import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.dao.StudentDAO;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.model.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class CourseStudentController implements Initializable {

    // FXML components for "Enrolled Courses" tab
    @FXML
    private TableView<Course> enrolledCoursesTable;
    @FXML
    private TableColumn<Course, Integer> courseCodeColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, String> subjectNameColumn;
    @FXML
    private TableColumn<Course, String> instructorColumn;
    @FXML
    private TableColumn<Course, Integer> capacityColumn;
    @FXML
    private TableColumn<Course, Integer> enrollmentColumn;
    @FXML
    private TextField enrolledCourseSearch; // TextField for searching enrolled courses

    // FXML components for "Course Details" tab
    @FXML
    private Label courseCodeLabel;
    @FXML
    private Label courseNameLabel;
    @FXML
    private Label subjectNameLabel;
    @FXML
    private Label instructorLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label enrollmentLabel;
    @FXML
    private Label meetingDaysLabel;
    @FXML
    private Label finalExamDateLabel;

    // FXML component for the TabPane
    @FXML
    private TabPane tabPane;

    private StudentDashboard parentController;

    public void setParentController(StudentDashboard parentController) {
        this.parentController = parentController;
    }

    // Data access objects
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;

    // Observable list to hold enrolled course data
    private ObservableList<Course> enrolledCoursesList = FXCollections.observableArrayList();

    // Filtered list for enrolled courses search
    private FilteredList<Course> filteredEnrolledCourses;

    // Current student's ID (to be set by the parent controller)
    private String studentId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns for "Enrolled Courses" table
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        enrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("currentEnrollment"));

        // Set table resize policy
        enrolledCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load enrolled courses (requires studentId to be set)
        if (studentId != null) {
            loadEnrolledCourses();
        } else {
            System.out.println("Student ID not set. Cannot load enrolled courses.");
        }

        // Initialize the filtered list for enrolled courses
        filteredEnrolledCourses = new FilteredList<>(enrolledCoursesList, p -> true);
        enrolledCoursesTable.setItems(filteredEnrolledCourses);

        // Add listener to the enrolled courses search TextField for real-time filtering
        enrolledCourseSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEnrolledCourses(newValue);
        });

        // Add double-click event handler to open course details tab from "Enrolled Courses" tab
        enrolledCoursesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && enrolledCoursesTable.getSelectionModel().getSelectedItem() != null) {
                handleViewDetails(enrolledCoursesTable.getSelectionModel().getSelectedItem());
            }
        });

        // Refresh enrolled courses when returning to the "Enrolled Courses" tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Enrolled Courses".equals(newTab.getText()) && studentId != null) {
                loadEnrolledCourses();
            }
        });
    }

    // Method to set the current student's ID
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        System.out.println("Student ID set in CourseStudentController: " + studentId);
        // Load enrolled courses once the student ID is set
        loadEnrolledCourses();
    }

    // Load the student's enrolled courses into the enrolledCoursesTable
    private void loadEnrolledCourses() {
        if (studentId == null) {
            System.out.println("Cannot load enrolled courses: studentId is null.");
            return;
        }
        enrolledCoursesList.clear();
        // Fetch the student by ID
        Student student = studentDAO.getStudentById(studentId);
        if (student != null && student.getRegisteredCourses() != null) {
            enrolledCoursesList.addAll(student.getRegisteredCourses());
        } else {
            System.out.println("Student " + studentId + " not found or has no registered courses.");
        }
        System.out.println("Loaded enrolled courses for student " + studentId + ": " + enrolledCoursesList);
    }

    // Filter enrolled courses based on search input and update the table
    private void filterEnrolledCourses(String searchText) {
        filteredEnrolledCourses.setPredicate(course -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return true; // Show all courses if search text is empty
            }

            String lowerCaseSearch = searchText.trim().toLowerCase();
            return String.valueOf(course.getCourseCode()).toLowerCase().contains(lowerCaseSearch) ||
                    course.getCourseName().toLowerCase().contains(lowerCaseSearch) ||
                    course.getSubjectCode().toLowerCase().contains(lowerCaseSearch);
        });
        System.out.println("Filtered enrolled courses for '" + searchText + "': " + filteredEnrolledCourses);
    }

    // Helper method to populate the "Course Details" tab with the selected course's information
    private void handleViewDetails(Course selectedCourse) {
        if (selectedCourse != null) {
            // Populate the "Course Details" tab with the selected course's information
            courseCodeLabel.setText(String.valueOf(selectedCourse.getCourseCode()));
            courseNameLabel.setText(selectedCourse.getCourseName());
            subjectNameLabel.setText(selectedCourse.getSubjectCode());
            instructorLabel.setText(selectedCourse.getInstructor());
            capacityLabel.setText(String.valueOf(selectedCourse.getCapacity()));
            enrollmentLabel.setText(String.valueOf(selectedCourse.getCurrentEnrollment()));
            meetingDaysLabel.setText(selectedCourse.getMeetingDaysTime() != null ? selectedCourse.getMeetingDaysTime() : "Not set");
            finalExamDateLabel.setText(selectedCourse.getFinalExamDateTime() != null ? selectedCourse.getFinalExamDateTime() : "Not set");

            // Switch to the "Course Details" tab
            tabPane.getSelectionModel().select(1); // Index 1 corresponds to the "Course Details" tab
        }
    }
}