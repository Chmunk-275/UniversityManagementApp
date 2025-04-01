package com.example.universitymanagementapp.controller.CourseController;

import com.example.universitymanagementapp.controller.FacultyController.FacultyDashboard;
import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.dao.FacultyDAO;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.UniversityManagementApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;



public class CourseFacultyController {

    @FXML
    private TableView<Course> allCoursesTable;

    @FXML
    private TableColumn<Course, Integer> courseCodeColumn;

    @FXML
    private TableColumn<Course, String> subjectCodeColumn;

    @FXML
    private TableColumn<Course, String> courseNameColumn;

    @FXML
    private TableColumn<Course, String> instructorColumn;

    @FXML
    private TableColumn<Course, Integer> capacityColumn;

    @FXML
    private TableColumn<Course, Integer> enrollmentColumn;

    @FXML
    private TableView<Course> myCoursesTable;

    @FXML
    private TableColumn<Course, Integer> myCourseCodeColumn;

    @FXML
    private TableColumn<Course, String> mySubjectCodeColumn;

    @FXML
    private TableColumn<Course, String> myCourseNameColumn;

    @FXML
    private TableColumn<Course, Integer> myCapacityColumn;

    @FXML
    private TableColumn<Course, Integer> myEnrollmentColumn;

    @FXML
    private TableColumn<Course, String> myMeetingDaysColumn;

    @FXML
    private TextField courseSearch;

    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;
    private FacultyDAO facultyDAO = UniversityManagementApp.facultyDAO;

    private String facultyName;
    private FacultyDashboard parentController;

    private ObservableList<Course> allCoursesList = FXCollections.observableArrayList();
    private ObservableList<Course> filteredCoursesList = FXCollections.observableArrayList();

    public CourseFacultyController() {
    }

    @FXML
    public void initialize() {
        // Configure columns for All Courses table
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        instructorColumn.setCellValueFactory(new PropertyValueFactory<>("instructor"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        enrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("currentEnrollment"));

        // Configure columns for My Courses table
        myCourseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        mySubjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));
        myCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        myCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        myEnrollmentColumn.setCellValueFactory(new PropertyValueFactory<>("currentEnrollment"));
        myMeetingDaysColumn.setCellValueFactory(new PropertyValueFactory<>("meetingDaysTime"));

        // Set table resize policies
        allCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        myCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load courses
        loadAllCourses();
        loadMyCourses();

        // Add listener to courseSearch TextField for real-time filtering
        courseSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCourses(newValue);
        });
    }

    private void loadAllCourses() {
        System.out.println("Loading all courses...");
        allCoursesList.clear();
        allCoursesList.addAll(courseDAO.getAllCourses());
        filteredCoursesList.clear();
        filteredCoursesList.addAll(allCoursesList);
        allCoursesTable.setItems(filteredCoursesList);
        System.out.println("All Courses Table: " + allCoursesList);
    }

    private void loadMyCourses() {
        if (facultyName != null) {
            System.out.println("Loading courses for faculty: '" + facultyName + "' (length: " + facultyName.length() + ")");
            List<Course> allCourses = courseDAO.getAllCourses();
            System.out.println("All courses in CourseDAO:");
            for (Course course : allCourses) {
                System.out.println("Course: " + course.getCourseName() + ", Instructor: '" + course.getInstructor() + "' (length: " + (course.getInstructor() != null ? course.getInstructor().length() : "null") + ")");
            }
            ObservableList<Course> myCourses = FXCollections.observableArrayList(courseDAO.getCoursesTaught(facultyName));
            myCoursesTable.setItems(myCourses);
            System.out.println("My Courses Table: " + myCourses);
        } else {
            System.out.println("facultyName is null, my courses not loaded.");
        }
    }

    // Filter courses in the allCoursesTable based on search input
    private void filterCourses(String searchText) {
        filteredCoursesList.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredCoursesList.addAll(allCoursesList);
        } else {
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Course course : allCoursesList) {
                if (course.getCourseName().toLowerCase().contains(lowerCaseSearch) ||
                        course.getSubjectCode().toLowerCase().contains(lowerCaseSearch) ||
                        (course.getInstructor() != null && course.getInstructor().toLowerCase().contains(lowerCaseSearch))) {
                    filteredCoursesList.add(course);
                }
            }
        }
        allCoursesTable.setItems(filteredCoursesList);
        System.out.println("Filtered courses for '" + searchText + "': " + filteredCoursesList);
    }

    public void setLoggedInFacultyName(String facultyName) {
        this.facultyName = facultyName;
        System.out.println("Set facultyName to: '" + facultyName + "' (length: " + (facultyName != null ? facultyName.length() : "null") + ")");
        loadMyCourses();
    }

    public void setParentController(FacultyDashboard parentController) {
        this.parentController = parentController;
    }
}