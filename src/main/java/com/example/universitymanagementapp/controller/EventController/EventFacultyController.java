package com.example.universitymanagementapp.controller.EventController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.FacultyController.FacultyDashboard;
import com.example.universitymanagementapp.dao.EventDAO;
import com.example.universitymanagementapp.model.Event;
import com.example.universitymanagementapp.model.Faculty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EventFacultyController {

    @FXML
    private TabPane tabPane;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Label monthLabel;

    @FXML
    private Button nextMonthButton;

    @FXML
    private TableView<Event> allEventsTable;

    @FXML
    private TableColumn<Event, String> codeColumn;

    @FXML
    private TableColumn<Event, String> nameColumn;

    @FXML
    private TableColumn<Event, String> dateColumn;

    @FXML
    private TableColumn<Event, String> locationColumn;

    @FXML
    private TableColumn<Event, Integer> capacityColumn;

    @FXML
    private TableColumn<Event, String> costColumn;

    @FXML
    private TextField eventSearch;

    @FXML
    private Button registerButton;

    @FXML
    private Button unregisterButton; // Renamed from unregisterStudentButton

    @FXML
    private TableView<Event> yourEventsTable;

    // Reusing same column names as allEventsTable for simplicity
    @FXML
    private TableColumn<Event, String> codeColumnYour;

    @FXML
    private TableColumn<Event, String> nameColumnYour;

    @FXML
    private TableColumn<Event, String> dateColumnYour;

    @FXML
    private TableColumn<Event, String> locationColumnYour;

    @FXML
    private TableColumn<Event, Integer> capacityColumnYour;

    @FXML
    private TableColumn<Event, String> costColumnYour;

    private String facultyUsername;
    private String facultyName;
    private FacultyDashboard parentController;

    private EventDAO eventDAO = UniversityManagementApp.eventDAO; // Assume this exists
    private ObservableList<Event> allEventsList = FXCollections.observableArrayList();
    private ObservableList<Event> registeredEventsList = FXCollections.observableArrayList();
    private Event selectedEvent = null;
    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    public void setParentController(FacultyDashboard parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {

        // If facultyName is not set but facultyUsername is, fetch the name from FacultyDAO
        if (facultyUsername != null && !facultyUsername.trim().isEmpty() && (facultyName == null || facultyName.trim().isEmpty())) {
            facultyName = UniversityManagementApp.facultyDAO.getLoggedInFaculty(facultyUsername);
            System.out.println("Fetched facultyName from FacultyDAO: '" + facultyName + "'");
        }

        // Configure columns for All Events table
        configureTableColumns(allEventsTable, codeColumn, nameColumn, dateColumn, locationColumn, capacityColumn, costColumn);
        configureTableColumns(yourEventsTable, codeColumnYour, nameColumnYour, dateColumnYour, locationColumnYour, capacityColumnYour, costColumnYour);

        // Load events
        loadAllEvents();
        loadRegisteredEvents();

        // Update calendar
        updateMonthLabel();
        populateCalendar();

        // Add search listener
        eventSearch.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Enable/disable buttons based on selection
        allEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEvent = newSelection;
            registerButton.setDisable(newSelection == null || registeredEventsList.contains(newSelection));
        });
        yourEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedEvent = newSelection;
            unregisterButton.setDisable(newSelection == null);
        });

        // Double-click to show event details
        allEventsTable.setOnMouseClicked(this::handleDoubleClick);
        yourEventsTable.setOnMouseClicked(this::handleDoubleClick);
    }

    private void configureTableColumns(TableView<Event> table, TableColumn<Event, String> codeCol,
                                       TableColumn<Event, String> nameCol, TableColumn<Event, String> dateCol,
                                       TableColumn<Event, String> locationCol, TableColumn<Event, Integer> capacityCol,
                                       TableColumn<Event, String> costCol) {
        codeCol.setCellValueFactory(new PropertyValueFactory<>("eventCode"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateCol.setCellValueFactory(cellData -> {
            java.util.Date dateTime = cellData.getValue().getEventDateTime();
            return new javafx.beans.property.SimpleStringProperty(
                    dateTime != null ? new SimpleDateFormat("MM/dd/yyyy HH:mm").format(dateTime) : "N/A");
        });
        locationCol.setCellValueFactory(new PropertyValueFactory<>("eventLocation"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("eventCapacity"));
        costCol.setCellValueFactory(new PropertyValueFactory<>("eventCost"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(table == allEventsTable ? allEventsList : registeredEventsList);
    }

    private void loadAllEvents() {
        allEventsList.clear();
        allEventsList.addAll(eventDAO.getAllEvents());
        allEventsTable.setItems(allEventsList);
    }

    private void loadRegisteredEvents() {
        registeredEventsList.clear();
        List<Event> allEvents = eventDAO.getAllEvents();
        for (Event event : allEvents) {
            List<String> registeredFaculty = event.getRegisteredStudents();
            if (registeredFaculty != null && registeredFaculty.contains(facultyUsername)) {
                registeredEventsList.add(event);
            }
        }
        yourEventsTable.setItems(registeredEventsList);
    }

    private void updateMonthLabel() {
        String monthName = YearMonth.of(currentYear, currentMonth)
                .getMonth()
                .getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault());
        monthLabel.setText(monthName + " " + currentYear);
    }

    private void populateCalendar() {
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = yearMonth.lengthOfMonth();

        List<Event> eventsInMonth = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (Event event : allEventsList) {
            if (event.getEventDateTime() != null) {
                cal.setTime(event.getEventDateTime());
                int eventYear = cal.get(Calendar.YEAR);
                int eventMonth = cal.get(Calendar.MONTH) + 1;
                if (eventYear == currentYear && eventMonth == currentMonth) {
                    eventsInMonth.add(event);
                }
            }
        }

        int row = 1;
        int col = dayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayBox = new VBox(2);
            dayBox.setPadding(new Insets(5));
            dayBox.getStyleClass().add("day-box");

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            dayBox.getChildren().add(dayLabel);

            for (Event event : eventsInMonth) {
                cal.setTime(event.getEventDateTime());
                int eventDay = cal.get(Calendar.DAY_OF_MONTH);
                if (eventDay == day) {
                    String displayText = event.getEventName().length() > 15 ? event.getEventCode() : event.getEventName();
                    Label eventLabel = new Label(displayText);
                    eventLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: blue;");
                    Tooltip tooltip = new Tooltip(event.getEventName());
                    Tooltip.install(eventLabel, tooltip);
                    eventLabel.setOnMouseClicked(e -> showEventDetails(event));
                    dayBox.getChildren().add(eventLabel);
                }
            }

            GridPane.setRowIndex(dayBox, row);
            GridPane.setColumnIndex(dayBox, col);
            calendarGrid.getChildren().add(dayBox);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    private void handlePreviousMonth() {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateMonthLabel();
        populateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateMonthLabel();
        populateCalendar();
    }

    private void filterEvents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            allEventsTable.setItems(allEventsList);
        } else {
            ObservableList<Event> filteredList = FXCollections.observableArrayList();
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Event event : allEventsList) {
                if (event.getEventName().toLowerCase().contains(lowerCaseSearch) ||
                        event.getEventCode().toLowerCase().contains(lowerCaseSearch) ||
                        event.getEventLocation().toLowerCase().contains(lowerCaseSearch)) {
                    filteredList.add(event);
                }
            }
            allEventsTable.setItems(filteredList);
        }
    }

    @FXML
    private void handleRegisterFacultyDialog() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        if (facultyUsername == null || facultyUsername.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Faculty username is not set. Cannot register.");
            return;
        }

        List<String> registeredFaculty = selectedEvent.getRegisteredStudents();
        if (registeredFaculty == null) {
            registeredFaculty = new ArrayList<>(); // Initialize the list if null
            selectedEvent.setRegisteredStudents(registeredFaculty);
        } else {
            registeredFaculty = new ArrayList<>(registeredFaculty); // Create a mutable copy
        }

        if (registeredFaculty.contains(facultyUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Already Registered", "You are already registered for this event.");
            return;
        }

        if (registeredFaculty.size() >= selectedEvent.getEventCapacity()) {
            showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", "The event has reached its maximum capacity.");
            return;
        }

        registeredFaculty.add(facultyUsername);
        selectedEvent.setRegisteredStudents(registeredFaculty);
        eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
        loadRegisteredEvents();
        showAlert(Alert.AlertType.INFORMATION, "Success", "You have been registered for " + selectedEvent.getEventName());
    }

    @FXML
    private void handleUnregisterFacultyDialog() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        List<String> registeredFaculty = selectedEvent.getRegisteredStudents();
        if (registeredFaculty == null) {
            registeredFaculty = new ArrayList<>();
            selectedEvent.setRegisteredStudents(registeredFaculty);
        } else {
            registeredFaculty = new ArrayList<>(registeredFaculty);
        }

        if (!registeredFaculty.contains(facultyUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Not Registered", "You are not registered for this event.");
            return;
        }

        registeredFaculty.remove(facultyUsername);
        selectedEvent.setRegisteredStudents(registeredFaculty);
        eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
        loadRegisteredEvents();
        showAlert(Alert.AlertType.INFORMATION, "Success", "You have been unregistered from " + selectedEvent.getEventName());
    }

    private void handleDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Event selected = ((TableView<Event>) event.getSource()).getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEventDetails(selected);
            }
        }
    }

    private void showEventDetails(Event event) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Event Details: " + event.getEventName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);

        // Event image
        ImageView eventHeaderImage = new ImageView();
        if (event.getEventHeaderImage() != null) {
            eventHeaderImage.setImage(event.getEventHeaderImage());
            System.out.println("Loaded event header image for event: " + event.getEventName());
        } else {
            System.out.println("Event header image is null for event: " + event.getEventName() + ". Loading default image.");
            try {
                eventHeaderImage.setImage(new Image(getClass().getResourceAsStream("/images/eventsdefault.jpg")));
                System.out.println("Default event image loaded successfully.");
            } catch (Exception e) {
                System.out.println("Error loading default event image: " + e.getMessage());
                eventHeaderImage.setImage(null);
            }
        }
        eventHeaderImage.setFitWidth(200);
        eventHeaderImage.setFitHeight(150);
        eventHeaderImage.setPreserveRatio(true);

        Label codeLabel = new Label("Code: " + event.getEventCode());
        Label nameLabel = new Label("Name: " + event.getEventName());
        Label descriptionLabel = new Label("Description: " + (event.getEventDescription() != null ? event.getEventDescription() : "Not set"));
        Label locationLabel = new Label("Location: " + event.getEventLocation());
        Label dateTimeLabel = new Label("Date & Time: " + (event.getEventDateTime() != null ? new SimpleDateFormat("MM/dd/yyyy HH:mm").format(event.getEventDateTime()) : "Not set"));
        Label capacityLabel = new Label("Capacity: " + event.getEventCapacity());
        Label costLabel = new Label("Cost: " + event.getEventCost());

        Label facultyLabel = new Label("Registered People:");
        ListView<String> facultyListView = new ListView<>();
        ObservableList<String> facultyDetails = FXCollections.observableArrayList();
        List<String> registeredFaculty = event.getRegisteredStudents();
        if (registeredFaculty == null || registeredFaculty.isEmpty()) {
            facultyDetails.add("No faculty registered.");
        } else {
            for (String facultyId : registeredFaculty) {
                if (facultyId == null || facultyId.equals("null")) {
                    continue; // Skip invalid entries
                }
                // If the facultyId matches the current user's facultyUsername, use facultyName
                if (facultyId.equals(facultyUsername) && facultyName != null) {
                    facultyDetails.add(facultyName);
                } else {
                    // Try to look up the faculty name via FacultyDAO
                    Faculty faculty = UniversityManagementApp.facultyDAO.getFacultyById(facultyId);
                    facultyDetails.add(faculty != null ? faculty.getName() : facultyId);
                }
            }
        }
        facultyListView.setItems(facultyDetails);
        facultyListView.setPrefHeight(100); // Set a reasonable height for the list

        // Add the ImageView to the VBox
        vbox.getChildren().addAll(
                eventHeaderImage, // Add the image at the top
                codeLabel, nameLabel, descriptionLabel, locationLabel, dateTimeLabel,
                capacityLabel, costLabel, facultyLabel, facultyListView
        );

        Scene scene = new Scene(vbox, 400, 500); // Increase height to accommodate the image
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setFacultyUsername(String username) {
        this.facultyUsername = username;
        System.out.println("EventFacultyController: Set facultyUsername to: '" + username + "' (length: " + (username != null ? username.length() : "null") + ")");
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
        System.out.println("EventFacultyController: Set facultyName to: '" + facultyName + "' (length: " + (facultyName != null ? facultyName.length() : "null") + ")");
    }
}