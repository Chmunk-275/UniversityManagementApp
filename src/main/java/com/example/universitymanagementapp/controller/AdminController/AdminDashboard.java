package com.example.universitymanagementapp.controller.AdminController;

import com.example.universitymanagementapp.UniversityManagementApp;
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

public class AdminDashboard {

    @FXML
    public TabPane tabPane;

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
    public MenuItem logout;

    @FXML
    public AnchorPane contentPane;

    @FXML
    private Button addStudentButton;
    @FXML
    private Button addCourseButton;
    @FXML
    private Button addFacultyButton;
    @FXML
    private Button addSubjectButton;
    @FXML
    private Button addEventButton;

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

    private String currentPage = "admin-dashboard.fxml";
    private ObservableList<Activity> recentActivities = FXCollections.observableArrayList();
    private ObservableList<Registration> recentRegistrations = FXCollections.observableArrayList();
    private ObservableList<Event> upcomingEvents = FXCollections.observableArrayList();
    private ObservableList<Notification> notifications = FXCollections.observableArrayList();
    private Node initialDashboardContent; // Store the initial dashboard content

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
        // Bind column widths to table width
        activityTypeColumn.prefWidthProperty().bind(recentActivitiesTable.widthProperty().multiply(0.2)); // 20%
        activityDescriptionColumn.prefWidthProperty().bind(recentActivitiesTable.widthProperty().multiply(0.6)); // 60%
        activityDateColumn.prefWidthProperty().bind(recentActivitiesTable.widthProperty().multiply(0.2)); // 20%

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
        // Bind column widths to table width
        registrationStudentColumn.prefWidthProperty().bind(recentRegistrationsTable.widthProperty().multiply(0.25)); // 25%
        registrationCourseColumn.prefWidthProperty().bind(recentRegistrationsTable.widthProperty().multiply(0.25)); // 25%
        registrationCourseNameColumn.prefWidthProperty().bind(recentRegistrationsTable.widthProperty().multiply(0.25)); // 25%
        registrationDateColumn.prefWidthProperty().bind(recentRegistrationsTable.widthProperty().multiply(0.25)); // 25%

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
        // Bind column widths to table width
        eventCodeColumn.prefWidthProperty().bind(upcomingEventsTable.widthProperty().multiply(0.25)); // 25%
        eventNameColumn.prefWidthProperty().bind(upcomingEventsTable.widthProperty().multiply(0.25)); // 25%
        eventDateColumn.prefWidthProperty().bind(upcomingEventsTable.widthProperty().multiply(0.25)); // 25%
        eventLocationColumn.prefWidthProperty().bind(upcomingEventsTable.widthProperty().multiply(0.25)); // 25%

        // Configure table columns for Notifications
        notificationTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        notificationMessageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
        notificationDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")))
        );
        notificationsTable.setItems(notifications);
        // Bind column widths to table width
        notificationTypeColumn.prefWidthProperty().bind(notificationsTable.widthProperty().multiply(0.2)); // 20%
        notificationMessageColumn.prefWidthProperty().bind(notificationsTable.widthProperty().multiply(0.6)); // 60%
        notificationDateColumn.prefWidthProperty().bind(notificationsTable.widthProperty().multiply(0.2)); // 20%

        // Load data
        loadSummaryData();
        loadRecentActivities();
        loadRecentRegistrations();
        loadUpcomingEvents();
        loadNotifications();

        // Force a layout pass to ensure components adjust to the window size
        contentPane.layout();
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
        // If you truly want a fallback row, you could do it here.
        // But removing it ensures we only show real data.
        recentActivities.sort(Comparator.comparing(Activity::getDate).reversed());
    }

    private void loadRecentRegistrations() {
        recentRegistrations.clear();
        // Grab from ExExporter
        List<Registration> exportedRegistrations = ExExporter.getRecentRegistrations();
        if (exportedRegistrations != null && !exportedRegistrations.isEmpty()) {
            recentRegistrations.addAll(exportedRegistrations);
        }
        // No fallback row, so if none exist, table is simply empty
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
        if (fxmlFile.equals("admin-dashboard.fxml")) {
            if (initialDashboardContent != null) {
                contentPane.getChildren().clear();
                contentPane.getChildren().add(initialDashboardContent);
                currentPage = "admin-dashboard.fxml";
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
        if (currentPage.equals("admin-course-selection.fxml")) {
            // If the current page is the courses tab, reload it
            loadPage("admin-course-selection.fxml");
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
        loadPage("admin-dashboard.fxml"); // Restores the initial dashboard content and refreshes data
    }

    @FXML
    public void handleSubjectSelection(ActionEvent actionEvent) {
        loadPage("admin-subject-selection.fxml");
    }

    @FXML
    public void handleCourseSelection(ActionEvent actionEvent) {
        loadPage("admin-course-selection.fxml");
    }

    @FXML
    public void handleStudentSelection(ActionEvent actionEvent) {
        loadPage("admin-student-selection.fxml");
    }

    @FXML
    public void handleFacultySelection(ActionEvent actionEvent) {
        loadPage("admin-faculty-selection.fxml");
    }

    @FXML
    public void handleEventSelection(ActionEvent actionEvent) {
        loadPage("admin-events-selection.fxml");
    }

    @FXML
    public void handleSettingSelection(ActionEvent actionEvent) {
        loadPage("admin-settings-selection.fxml");
    }

    @FXML
    public void handleLogoutAction(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/universitymanagementapp/login-page.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) toggleMenuButton.getScene().getWindow();
            Scene scene = new Scene(loginPage, 601, 498); // Set the scene size explicitly
            stage.setScene(scene);
            stage.setTitle("Login Page");
            stage.setWidth(601); // Ensure the stage size remains consistent
            stage.setHeight(498);
            stage.setMaximized(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading login page: " + e.getMessage());
        }
    }

    @FXML
    public void handleAddStudent(ActionEvent actionEvent) {
        loadPage("admin-student-selection.fxml");
    }

    @FXML
    public void handleAddCourse(ActionEvent actionEvent) {
        loadPage("admin-course-selection.fxml");
    }

    @FXML
    public void handleAddFaculty(ActionEvent actionEvent) {
        loadPage("admin-faculty-selection.fxml");
    }

    @FXML
    public void handleAddSubject(ActionEvent actionEvent) {
        loadPage("admin-subject-selection.fxml");
    }

    @FXML
    public void handleAddEvent(ActionEvent actionEvent) {
        loadPage("admin-events-selection.fxml");
    }
}
