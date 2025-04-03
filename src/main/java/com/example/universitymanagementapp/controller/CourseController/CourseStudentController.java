package com.example.universitymanagementapp.controller.CourseController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.StudentController.StudentDashboard;
import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.dao.FacultyDAO;
import com.example.universitymanagementapp.dao.StudentDAO;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.model.Faculty;
import com.example.universitymanagementapp.model.Student;
import javafx.beans.property.SimpleStringProperty;
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
    private TextField enrolledCourseSearch;

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

    @FXML
    private TabPane tabPane;

    private StudentDashboard parentController;

    public void setParentController(StudentDashboard parentController) {
        this.parentController = parentController;
    }

    // Data access objects
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;
    private FacultyDAO facultyDAO = UniversityManagementApp.facultyDAO; // Add FacultyDAO

    private ObservableList<Course> enrolledCoursesList = FXCollections.observableArrayList();
    private FilteredList<Course> filteredEnrolledCourses;
    private String studentId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns for "Enrolled Courses" table
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));

        // instructor column to display faculty name instead of ID
        instructorColumn.setCellValueFactory(cellData -> {
            String instructorId = cellData.getValue().getInstructor();
            if (instructorId == null || instructorId.isEmpty() || instructorId.equals("Unassigned")) {
                return new SimpleStringProperty(instructorId);
            }
            // Try to find the faculty by ID (username)
            Faculty faculty = facultyDAO.getFacultyById(instructorId);
            if (faculty != null) {
                return new SimpleStringProperty(faculty.getName());
            }
            // If not found by ID, it might already be a name
            return new SimpleStringProperty(instructorId);
        });

        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        enrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("currentEnrollment"));

        enrolledCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        if (studentId != null) {
            loadEnrolledCourses();
        } else {
            System.out.println("Student ID not set. Cannot load enrolled courses.");
        }

        filteredEnrolledCourses = new FilteredList<>(enrolledCoursesList, p -> true);
        enrolledCoursesTable.setItems(filteredEnrolledCourses);

        enrolledCourseSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEnrolledCourses(newValue);
        });

        enrolledCoursesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && enrolledCoursesTable.getSelectionModel().getSelectedItem() != null) {
                handleViewDetails(enrolledCoursesTable.getSelectionModel().getSelectedItem());
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Enrolled Courses".equals(newTab.getText()) && studentId != null) {
                loadEnrolledCourses();
            }
        });
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
        System.out.println("Student ID set in CourseStudentController: " + studentId);
        loadEnrolledCourses();
    }

    private void loadEnrolledCourses() {
        if (studentId == null) {
            System.out.println("Cannot load enrolled courses: studentId is null.");
            return;
        }
        enrolledCoursesList.clear();
        Student student = studentDAO.getStudentById(studentId);
        if (student != null && student.getRegisteredCourses() != null) {
            enrolledCoursesList.addAll(student.getRegisteredCourses());
        } else {
            System.out.println("Student " + studentId + " not found or has no registered courses.");
        }
        System.out.println("Loaded enrolled courses for student " + studentId + ": " + enrolledCoursesList);
    }

    private void filterEnrolledCourses(String searchText) {
        filteredEnrolledCourses.setPredicate(course -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return true;
            }

            String lowerCaseSearch = searchText.trim().toLowerCase();
            return String.valueOf(course.getCourseCode()).toLowerCase().contains(lowerCaseSearch) ||
                    course.getCourseName().toLowerCase().contains(lowerCaseSearch) ||
                    course.getSubjectCode().toLowerCase().contains(lowerCaseSearch);
        });
        System.out.println("Filtered enrolled courses for '" + searchText + "': " + filteredEnrolledCourses);
    }

    private void handleViewDetails(Course selectedCourse) {
        if (selectedCourse != null) {
            courseCodeLabel.setText(String.valueOf(selectedCourse.getCourseCode()));
            courseNameLabel.setText(selectedCourse.getCourseName());
            subjectNameLabel.setText(selectedCourse.getSubjectCode());

            // Resolve instructor name for the "Course Details" tab
            String instructorDisplay = selectedCourse.getInstructor();
            if (instructorDisplay != null && !instructorDisplay.isEmpty() && !instructorDisplay.equals("Unassigned")) {
                Faculty faculty = facultyDAO.getFacultyById(instructorDisplay);
                if (faculty != null) {
                    instructorDisplay = faculty.getName();
                }
            }
            instructorLabel.setText(instructorDisplay);

            capacityLabel.setText(String.valueOf(selectedCourse.getCapacity()));
            enrollmentLabel.setText(String.valueOf(selectedCourse.getCurrentEnrollment()));
            meetingDaysLabel.setText(selectedCourse.getMeetingDaysTime() != null ? selectedCourse.getMeetingDaysTime() : "Not set");
            finalExamDateLabel.setText(selectedCourse.getFinalExamDateTime() != null ? selectedCourse.getFinalExamDateTime() : "Not set");

            tabPane.getSelectionModel().select(1);
        }
    }
}