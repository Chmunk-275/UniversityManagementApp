package com.example.universitymanagementapp.controller.FacultyController;

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
import java.util.*;
import java.util.stream.Collectors;

public class StudentFacultyController implements Initializable {

    // FXML components for "Faculty List" tab
    @FXML
    private TableView<Faculty> facultyTable;
    @FXML
    private TableColumn<Faculty, String> nameColumn;
    @FXML
    private TableColumn<Faculty, String> emailColumn;
    @FXML
    private TableColumn<Faculty, String> degreeColumn;
    @FXML
    private TextField facultySearch;
    @FXML
    private TableColumn<Faculty, String> courseColumn;

    // FXML components for "Faculty Details" tab
    @FXML
    private Label nameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label degreeLabel;
    @FXML
    private Label researchInterestLabel;
    @FXML
    private Label coursesOfferedLabel;
    @FXML
    private Label officeLocationLabel;

    // FXML component for the TabPane
    @FXML
    private TabPane tabPane;

    // Data access objects
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;
    private FacultyDAO facultyDAO = UniversityManagementApp.facultyDAO;

    // Observable list to hold faculty data
    private ObservableList<Faculty> facultyList = FXCollections.observableArrayList();

    // Filtered list for faculty search
    private FilteredList<Faculty> filteredFacultyList;

    // Current student's ID (to be set by the parent controller)
    private String studentId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns for "Faculty List" table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        degreeColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));

        courseColumn.setCellValueFactory(cellData -> {
            Faculty faculty = cellData.getValue();
            // Get courses this faculty teaches that the student is enrolled in
            List<String> studentCourses = getStudentCoursesTaughtByFaculty(faculty);
            return new SimpleStringProperty(String.join(", ", studentCourses));
        });

        // Set table resize policy
        facultyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load faculty members (requires studentId to be set)
        if (studentId != null) {
            loadFacultyMembers();
        } else {
            System.out.println("Student ID not set. Cannot load faculty members.");
        }

        // Initialize the filtered list for faculty
        filteredFacultyList = new FilteredList<>(facultyList, p -> true);
        facultyTable.setItems(filteredFacultyList);

        // Add listener to the faculty search TextField for real-time filtering
        facultySearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFaculty(newValue);
        });

        // Add double-click event handler to open faculty details tab
        facultyTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && facultyTable.getSelectionModel().getSelectedItem() != null) {
                handleViewDetails(facultyTable.getSelectionModel().getSelectedItem());
            }
        });

        // Refresh faculty list when returning to the "Faculty List" tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && "Faculty List".equals(newTab.getText()) && studentId != null) {
                loadFacultyMembers();
            }
        });
    }

    private List<String> getStudentCoursesTaughtByFaculty(Faculty faculty) {
        if (studentId == null || faculty == null) {
            return Collections.emptyList();
        }

        Student student = studentDAO.getStudentById(studentId);
        if (student == null || student.getRegisteredCourses() == null) {
            return Collections.emptyList();
        }

        return student.getRegisteredCourses().stream()
                .filter(course -> faculty.getName().equals(course.getInstructor()))
                .map(Course::getCourseName)  // Assuming Course has a getName() method
                .collect(Collectors.toList());
    }

    // Method to set the current student's ID
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        System.out.println("Student ID set in StudentFacultyController: " + studentId);
        // Load faculty members once the student ID is set
        loadFacultyMembers();
    }

    // Load faculty members teaching the student's enrolled courses
    private void loadFacultyMembers() {
        if (studentId == null) {
            System.out.println("Cannot load faculty members: studentId is null.");
            return;
        }

        facultyList.clear();
        Student student = studentDAO.getStudentById(studentId);
        if (student == null || student.getRegisteredCourses() == null) {
            System.out.println("Student " + studentId + " not found or has no registered courses.");
            return;
        }

        Set<String> instructorNames = new HashSet<>();
        for (Course course : student.getRegisteredCourses()) {
            if (course.getInstructor() != null && !course.getInstructor().isEmpty()) {
                instructorNames.add(course.getInstructor());
            }
        }

        for (String instructorName : instructorNames) {
            Faculty faculty = facultyDAO.getFacultyByName(instructorName);
            if (faculty != null) {
                facultyList.add(faculty);
            }
        }

        System.out.println("Loaded faculty members for student " + studentId + ": " + facultyList);
        facultyTable.refresh(); // Ensure table updates with new data
    }

    // Filter faculty based on search input and update the table
    private void filterFaculty(String searchText) {
        filteredFacultyList.setPredicate(faculty -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return true; // Show all faculty if search text is empty
            }

            String lowerCaseSearch = searchText.trim().toLowerCase();
            return faculty.getName().toLowerCase().contains(lowerCaseSearch) ||
                    faculty.getEmail().toLowerCase().contains(lowerCaseSearch);
        });
        System.out.println("Filtered faculty for '" + searchText + "': " + filteredFacultyList);
    }

    // Helper method to populate the "Faculty Details" tab with the selected faculty's information
    private void handleViewDetails(Faculty selectedFaculty) {
        if (selectedFaculty != null) {
            // Populate the "Faculty Details" tab with the selected faculty's information
            nameLabel.setText(selectedFaculty.getName());
            emailLabel.setText(selectedFaculty.getEmail());
            degreeLabel.setText(selectedFaculty.getDegree());
            researchInterestLabel.setText(selectedFaculty.getResearchInterest() != null ? selectedFaculty.getResearchInterest() : "Not set");
            coursesOfferedLabel.setText(selectedFaculty.getCoursesOffered() != null ? String.join(", ", selectedFaculty.getCoursesOffered()) : "Not set");
            officeLocationLabel.setText(selectedFaculty.getOfficeLocation() != null ? selectedFaculty.getOfficeLocation() : "Not set");

            // Switch to the "Faculty Details" tab
            tabPane.getSelectionModel().select(1); // Index 1 corresponds to the "Faculty Details" tab
        }
    }

    private StudentDashboard parentController;
    public void setParentController(StudentDashboard controller) {
        this.parentController = controller;
    }
}