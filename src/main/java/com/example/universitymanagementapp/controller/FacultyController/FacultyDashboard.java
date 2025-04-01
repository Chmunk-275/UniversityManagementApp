package com.example.universitymanagementapp.controller.FacultyController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.CourseController.CourseFacultyController;
import com.example.universitymanagementapp.controller.EventController.EventFacultyController;
import com.example.universitymanagementapp.controller.StudentController.FacultyStudentController;
import com.example.universitymanagementapp.controller.SubjectController.FacultySubjectController;
import com.example.universitymanagementapp.model.Activity;
import com.example.universitymanagementapp.model.Event;
import com.example.universitymanagementapp.model.Notification;
import com.example.universitymanagementapp.model.Registration;
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
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FacultyDashboard {

    @FXML
    public MenuButton toggleMenuButton;
    @FXML
    public MenuItem dashboard;
    @FXML
    public MenuItem subjectSelection;
    @FXML
    public MenuItem courseSelection;
    @FXML
    public MenuItem studentSelection;
    @FXML
    public MenuItem facultySelection;
    @FXML
    public MenuItem eventSelection;
    @FXML
    public MenuItem settingsSelection;
    @FXML
    public MenuItem logout;

    @FXML
    private AnchorPane contentPane;

    @FXML
    private Text totalStudentsText;
    @FXML
    private Text totalCoursesText;
    @FXML
    private Text totalFacultyText;
    @FXML
    private Text totalSubjectsText;
    @FXML
    private Text totalEventsText;

    @FXML
    private TableView<Activity> recentActivitiesTable;
    @FXML
    private TableColumn<Activity, String> activityTypeColumn;
    @FXML
    private TableColumn<Activity, String> activityDescriptionColumn;
    @FXML
    private TableColumn<Activity, String> activityDateColumn;

    @FXML
    private TableView<Registration> recentRegistrationsTable;
    @FXML
    private TableColumn<Registration, String> registrationStudentColumn;
    @FXML
    private TableColumn<Registration, Integer> registrationCourseColumn;
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

    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, String> notificationTypeColumn;
    @FXML
    private TableColumn<Notification, String> notificationMessageColumn;
    @FXML
    private TableColumn<Notification, String> notificationDateColumn;

    private String currentPage = "faculty-dashboard.fxml";
    private String facultyName;
    private String facultyUsername;
    private Node initialDashboardContent;

    private ObservableList<Activity> recentActivities = FXCollections.observableArrayList();
    private ObservableList<Registration> recentRegistrations = FXCollections.observableArrayList();
    private ObservableList<Event> upcomingEvents = FXCollections.observableArrayList();
    private ObservableList<Notification> notifications = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        contentPane.setPickOnBounds(false);

        // Store the initial content of the contentPane (the dashboard UI)
        if (!contentPane.getChildren().isEmpty()) {
            initialDashboardContent = contentPane.getChildren().get(0);
        }

        // Configure table columns for Recent Activities
        activityTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        activityDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
        activityDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")))
        );
        recentActivitiesTable.setItems(recentActivities);

        // Configure table columns for Recent Registrations
        registrationStudentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudentId()));
        registrationCourseColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCourseCode()).asObject()
        );
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

        // Configure table columns for Notifications
        notificationTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        notificationMessageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        notificationDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")))
        );
        notificationsTable.setItems(notifications);

        // Load data
        loadSummaryData();
        loadRecentActivities();
        loadRecentRegistrations();
        loadUpcomingEvents();
        loadNotifications();
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
        System.out.println("Faculty name set in dashboard: " + facultyName);
    }

    public void setFacultyUsername(String facultyUsername) {
        this.facultyUsername = facultyUsername;
        System.out.println("Faculty username set in dashboard: " + facultyUsername);
    }

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public String getFacultyName() {
        return facultyName;
    }

    private void loadSummaryData() {
        // Force refresh from DAOs
        int studentCount = UniversityManagementApp.studentDAO.getAllStudents().size();
        int courseCount = UniversityManagementApp.courseDAO.getAllCourses().size();
        int facultyCount = UniversityManagementApp.facultyDAO.getAllFaculty().size();
        int subjectCount = UniversityManagementApp.subjectDAO.getAllSubjects().size();
        int eventCount = UniversityManagementApp.eventDAO.getAllEvents().size();

        totalStudentsText.setText(String.valueOf(studentCount));
        totalCoursesText.setText(String.valueOf(courseCount));
        totalFacultyText.setText(String.valueOf(facultyCount));
        totalSubjectsText.setText(String.valueOf(subjectCount));
        totalEventsText.setText(String.valueOf(eventCount));
    }

    private void loadRecentActivities() {
        recentActivities.clear();
        // Grab whatever is in ExExporter
        List<Activity> exportedActivities = ExExporter.getRecentActivities();
        if (exportedActivities != null && !exportedActivities.isEmpty()) {
            recentActivities.addAll(exportedActivities);
        }
        recentActivities.sort(Comparator.comparing(Activity::getDate).reversed());
    }

    private void loadRecentRegistrations() {
        recentRegistrations.clear();
        // Grab from ExExporter
        List<Registration> exportedRegistrations = ExExporter.getRecentRegistrations();
        if (exportedRegistrations != null && !exportedRegistrations.isEmpty()) {
            recentRegistrations.addAll(exportedRegistrations);
        }
        recentRegistrations.sort(Comparator.comparing(Registration::getDate).reversed());
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

    private void loadNotifications() {
        notifications.clear();
        // Instead of hard-coded notifications, load from ExExporter
        List<Notification> exportedNotifications = ExExporter.getRecentNotifications();
        if (exportedNotifications != null && !exportedNotifications.isEmpty()) {
            notifications.addAll(exportedNotifications);
        }
        notifications.sort(Comparator.comparing(Notification::getDate).reversed());
    }

    private void loadPage(String fxmlFile) {
        // If loading the dashboard, restore the initial content
        if (fxmlFile.equals("faculty-dashboard.fxml")) {
            if (initialDashboardContent != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(initialDashboardContent);
                currentPage = "faculty-dashboard.fxml";
                // Refresh the dashboard data
                recentActivities.clear();
                recentRegistrations.clear();
                upcomingEvents.clear();
                notifications.clear();
                loadSummaryData();
                loadRecentActivities();
                loadRecentRegistrations();
                loadUpcomingEvents();
                loadNotifications();
            }
            return;
        }

        // Prevent loading the same page again
        if (fxmlFile.equals(currentPage)) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementapp/" + fxmlFile));
            Parent newPage = loader.load();

            // If loading CourseFacultyController, pass the faculty name
            if (fxmlFile.equals("faculty-course-selection.fxml")) {
                CourseFacultyController controller = loader.getController();
                controller.setLoggedInFacultyName(facultyName);
                controller.setParentController(this);
            }
            // If loading FacultyStudentController, pass the faculty name and username
            else if (fxmlFile.equals("faculty-student-selection.fxml")) {
                FacultyStudentController controller = loader.getController();
                controller.setFacultyName(facultyName);
                controller.setFacultyUsername(facultyUsername);
                controller.setParentController(this);
            }
            // Set parent controller for other subpages
            else if (fxmlFile.equals("faculty-subject-selection.fxml")) {
                FacultySubjectController controller = loader.getController();
                controller.setParentController(this);
            }
            else if (fxmlFile.equals("faculty-faculty-selection.fxml")) {
                FacultyFacultyController controller = loader.getController();
                controller.setParentController(this);
            }
            else if (fxmlFile.equals("faculty-event-selection.fxml")) {
                EventFacultyController controller = loader.getController();
                controller.setFacultyUsername(facultyUsername);
                controller.setFacultyName(facultyName);
                controller.setParentController(this);
            }
            else if (fxmlFile.equals("faculty-settings-selection.fxml")) {
                FacultySettingsController controller = loader.getController();
                controller.setParentController(this);
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

    // Add this method to refresh the Courses tab
    public void refreshCoursesTab() {
        if (currentPage.equals("faculty-course-selection.fxml")) {
            // If the current page is the courses tab, reload it
            loadPage("faculty-course-selection.fxml");
        }
        // Optionally, also refresh the dashboard’s recent activities & registrations
        recentActivities.clear();
        recentRegistrations.clear();
        loadRecentActivities();
        loadRecentRegistrations();
    }

    @FXML
    public void handleDashboardAction(ActionEvent actionEvent) {
        System.out.println("Dashboard menu item clicked. Restoring dashboard content.");
        loadPage("faculty-dashboard.fxml");
    }

    @FXML
    public void handleSubjectSelection(ActionEvent actionEvent) {
        loadPage("faculty-subject-selection.fxml");
    }

    @FXML
    public void handleCourseSelection(ActionEvent actionEvent) {
        loadPage("faculty-course-selection.fxml");
    }

    @FXML
    public void handleStudentSelection(ActionEvent actionEvent) {
        loadPage("faculty-student-selection.fxml");
    }

    @FXML
    public void handleFacultySelection(ActionEvent actionEvent) {
        loadPage("faculty-faculty-selection.fxml");
    }

    @FXML
    public void handleEventSelection(ActionEvent actionEvent) {
        loadPage("faculty-event-selection.fxml");
    }

    @FXML
    public void handleSettingsSelection(ActionEvent actionEvent) {
        loadPage("faculty-settings-selection.fxml");
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