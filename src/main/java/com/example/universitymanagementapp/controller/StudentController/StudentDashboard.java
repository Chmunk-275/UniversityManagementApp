package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.EventController.EventStudentController;
import com.example.universitymanagementapp.model.*;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.example.universitymanagementapp.controller.CourseController.CourseStudentController;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StudentDashboard {

    @FXML
    public MenuButton toggleMenuButton;
    @FXML
    public MenuItem dashboard;
    @FXML
    public MenuItem courseSelection;
    @FXML
    public MenuItem subjectSelection;
    @FXML
    public MenuItem studentSelection;
    @FXML
    public MenuItem eventSelection;
    @FXML
    public MenuItem logout;

    @FXML
    public AnchorPane contentPane;

    @FXML
    private Text enrolledCoursesText;
    @FXML
    private Text totalCreditsText;
    @FXML
    private Text upcomingEventsCountText;

    @FXML
    private TableView<Course> myCoursesTable;
    @FXML
    private TableColumn<Course, Integer> courseCodeColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, String> instructorColumn;

    @FXML
    private TableView<Registration> recentRegistrationsTable;
    @FXML
    private TableColumn<Registration, Integer> registrationCourseCodeColumn;
    @FXML
    private TableColumn<Registration, String> registrationCourseNameColumn;
    @FXML
    private TableColumn<Registration, String> registrationDateColumn;

    @FXML
    private TableView<Event> upcomingEventsTable;
    @FXML
    private TableColumn<Event, String> eventCodeColumn;
    @FXML
    private TableColumn<Event, String> eventNameColumn;
    @FXML
    private TableColumn<Event, String> eventDateColumn;
    @FXML
    private TableColumn<Event, String> eventLocationColumn;

    private String currentPage = "student-dashboard.fxml";
    private String studentId;
    private String studentName;
    private Node initialDashboardContent;

    private ObservableList<Course> myCourses = FXCollections.observableArrayList();
    private ObservableList<Registration> recentRegistrations = FXCollections.observableArrayList();
    private ObservableList<Event> upcomingEvents = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        contentPane.setPickOnBounds(false);

        // Store the initial content of the contentPane (the dashboard UI)
        if (!contentPane.getChildren().isEmpty()) {
            initialDashboardContent = contentPane.getChildren().get(0);
        }

        // Configure table columns for My Courses
        courseCodeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCourseCode()).asObject());
        courseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        instructorColumn.setCellValueFactory(cellData -> {
            String instructorName = cellData.getValue().getInstructor();
            // Look up the faculty member by name to ensure the name is valid
            Faculty faculty = UniversityManagementApp.facultyDAO.getFacultyByName(instructorName);
            return new SimpleStringProperty(instructorName != null && faculty != null ? instructorName : "N/A");
        });
        myCoursesTable.setItems(myCourses);

        // Configure table columns for Recent Registrations
        registrationCourseCodeColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCourseCode()).asObject());
        registrationCourseNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourseName()));
        registrationDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")))
        );
        recentRegistrationsTable.setItems(recentRegistrations);

        // Configure table columns for Upcoming Events
        eventCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventCode()));
        eventNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventName()));
        eventDateColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getEventDateTime();
            if (date != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                return new SimpleStringProperty(sdf.format(date));
            }
            return new SimpleStringProperty("N/A");
        });
        eventLocationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEventLocation()));
        upcomingEventsTable.setItems(upcomingEvents);

        // Load data if studentId is already set
        if (studentId != null) {
            loadSummaryData();
            loadMyCourses();
            loadRecentRegistrations();
            loadUpcomingEvents();
        }
    }

    // Method to set the student ID from UserLoginController
    public void setStudentId(String studentId) {
        this.studentId = studentId;
        System.out.println("Student ID set in StudentDashboard: " + studentId);
        loadSummaryData();
        loadMyCourses();
        loadRecentRegistrations();
        loadUpcomingEvents();
        loadPage(currentPage); // Reload with student context
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
        System.out.println("Student name set in dashboard: " + studentName);
    }

    private void loadMyCourses() {
        myCourses.clear();
        // Fetch the student using studentDAO
        Student student = UniversityManagementApp.studentDAO.getStudentById(studentId);
        if (student != null) {
            List<Course> enrolledCourses = student.getRegisteredCourses();
            if (enrolledCourses != null && !enrolledCourses.isEmpty()) {
                myCourses.addAll(enrolledCourses);
            }
        }
    }

    private void loadSummaryData() {
        // Fetch the student object to access their enrolled courses and grades
        Student student = UniversityManagementApp.studentDAO.getStudentById(studentId);
        if (student == null) {
            enrolledCoursesText.setText("0");
            totalCreditsText.setText("0");
            upcomingEventsCountText.setText("0");
            return;
        }

        // Load enrolled courses count
        List<Course> enrolledCourses = student.getRegisteredCourses();
        enrolledCoursesText.setText(String.valueOf(enrolledCourses != null ? enrolledCourses.size() : 0));

        // Calculate average grade for enrolled courses
        double totalGrade = 0.0;
        int gradedCoursesCount = 0;
        Map<Integer, Grade> studentGrades = student.getGrades();

        if (enrolledCourses != null) {
            for (Course course : enrolledCourses) {
                Grade grade = studentGrades.get(course.getCourseCode());
                if (grade != null && grade.getFinalGrade() >= 0) {
                    totalGrade += grade.getFinalGrade();
                    gradedCoursesCount++;
                }
            }
        }

        // Calculate average grade
        String averageGradeText = gradedCoursesCount > 0 ? String.format("%.2f", totalGrade / gradedCoursesCount) : "N/A";
        totalCreditsText.setText(averageGradeText);

        // Load upcoming events count
        int upcomingEventsCount = (int) UniversityManagementApp.eventDAO.getAllEvents().stream()
                .filter(event -> {
                    LocalDateTime eventDateTime = event.getEventDateTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    return eventDateTime.isAfter(LocalDateTime.now());
                })
                .count();
        upcomingEventsCountText.setText(String.valueOf(upcomingEventsCount));
    }

    private void loadRecentRegistrations() {
        recentRegistrations.clear();
        List<Registration> allRegistrations = ExExporter.getRecentRegistrations();
        if (allRegistrations != null && !allRegistrations.isEmpty()) {
            List<Registration> studentRegistrations = allRegistrations.stream()
                    .filter(reg -> reg.getStudentId().equals(studentId))
                    .sorted(Comparator.comparing(Registration::getDate).reversed())
                    .collect(Collectors.toList());
            recentRegistrations.addAll(studentRegistrations);
        }
    }

    private void loadUpcomingEvents() {
        List<Event> events = UniversityManagementApp.eventDAO.getAllEvents().stream()
                .filter(event -> {
                    LocalDateTime eventDateTime = event.getEventDateTime()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    return eventDateTime.isAfter(LocalDateTime.now());
                })
                .sorted(Comparator.comparing(event -> event.getEventDateTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()))
                .limit(5)
                .collect(Collectors.toList());
        upcomingEvents.setAll(events);
    }

    private void loadPage(String fxmlFile) {
        if (fxmlFile.equals("student-dashboard.fxml")) {
            if (initialDashboardContent != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(initialDashboardContent);
                currentPage = "student-dashboard.fxml";
                // Always refresh data when loading the dashboard
                if (studentId != null) {
                    loadSummaryData();
                    loadMyCourses();
                    loadRecentRegistrations();
                    loadUpcomingEvents();
                }
            }
            return;
        }

        if (fxmlFile.equals(currentPage)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementapp/" + fxmlFile));
            Parent newPage = loader.load();

            if (fxmlFile.equals("student-course-selection.fxml")) {
                CourseStudentController controller = loader.getController();
                if (studentId != null) {
                    controller.setStudentId(studentId);
                }
            } else if (fxmlFile.equals("student-student-selection.fxml")) {
                StudentStudentController controller = loader.getController();
                System.out.println("Setting studentId in StudentStudentController: " + studentId);
                if (studentId != null) {
                    controller.setStudentId(studentId);
                } else {
                    System.out.println("studentId is null in StudentDashboard when loading student-student.fxml");
                }
            } else if (fxmlFile.equals("student-event-selection.fxml")) {
                EventStudentController controller = loader.getController();
                controller.setStudentUsername(studentId);
                controller.setStudentName(studentName);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(newPage);

            AnchorPane.setTopAnchor(newPage, 0.0);
            AnchorPane.setBottomAnchor(newPage, 0.0);
            AnchorPane.setLeftAnchor(newPage, 0.0);
            AnchorPane.setRightAnchor(newPage, 0.0);

            currentPage = fxmlFile;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + fxmlFile);
        }
    }

    @FXML
    public void handleDashboardAction(ActionEvent actionEvent) {
        loadPage("student-dashboard.fxml");
    }

    @FXML
    public void handleCourseSelection(ActionEvent actionEvent) {
        loadPage("student-course-selection.fxml");
    }

    @FXML
    public void handleSubjectSelection(ActionEvent actionEvent) {
        loadPage("student-subject-selection.fxml");
    }

    @FXML
    public void handleStudentSelection(ActionEvent actionEvent) {
        loadPage("student-student-selection.fxml");
    }

    @FXML
    public void handleEventSelection(ActionEvent actionEvent) {
        loadPage("student-event-selection.fxml");
    }

    @FXML
    public void handleLogoutAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementapp/login-page.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) toggleMenuButton.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Login Page");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login page: " + e.getMessage());
        }
    }
}