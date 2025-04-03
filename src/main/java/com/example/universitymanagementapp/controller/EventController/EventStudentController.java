package com.example.universitymanagementapp.controller.EventController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.controller.StudentController.StudentDashboard;
import com.example.universitymanagementapp.dao.EventDAO;
import com.example.universitymanagementapp.model.Event;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.utils.ExExporter;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class EventStudentController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private TabPane tabPane;

    @FXML
    private ToolBar toolBar;

    @FXML
    private Button backButton;

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
    private Button unregisterButton;

    @FXML
    private TableView<Event> yourEventsTable;

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

    private String studentUsername;
    private String studentName;
    private StudentDashboard parentController;

    private EventDAO eventDAO = UniversityManagementApp.eventDAO;
    private ObservableList<Event> allEventsList = FXCollections.observableArrayList();
    private ObservableList<Event> registeredEventsList = FXCollections.observableArrayList();
    private Event selectedEvent = null;
    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();
    private ExExporter exporter = new ExExporter(UniversityManagementApp.courseDAO, UniversityManagementApp.studentDAO,
            UniversityManagementApp.facultyDAO, UniversityManagementApp.subjectDAO, eventDAO);

    public void setParentController(StudentDashboard parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        configureTableColumns(allEventsTable, codeColumn, nameColumn, dateColumn, locationColumn, capacityColumn, costColumn);
        configureTableColumns(yourEventsTable, codeColumnYour, nameColumnYour, dateColumnYour, locationColumnYour, capacityColumnYour, costColumnYour);

        loadAllEvents();
        // Do not call loadRegisteredEvents here; it will be called after studentUsername is set

        updateMonthLabel();
        populateCalendar();

        eventSearch.textProperty().addListener((obs, oldValue, newValue) -> filterEvents(newValue));

        // Listener for allEventsTable to update selectedEvent and registerButton state
        allEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvent = newSelection;
                System.out.println("Selected event in allEventsTable: " + selectedEvent.getEventCode());
            }
            // Disable registerButton if no event is selected or if the student is already registered
            boolean isAlreadyRegistered = registeredEventsList.contains(newSelection);
            registerButton.setDisable(newSelection == null || isAlreadyRegistered);
            System.out.println("registerButton disabled: " + registerButton.isDisabled() + " (newSelection: " + (newSelection != null ? newSelection.getEventCode() : "null") + ", isAlreadyRegistered: " + isAlreadyRegistered + ")");
        });

        // Listener for yourEventsTable to update selectedEvent and unregisterButton state
        yourEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedEvent = newSelection;
                System.out.println("Selected event in yourEventsTable: " + selectedEvent.getEventCode());
            }
            unregisterButton.setDisable(newSelection == null);
            System.out.println("unregisterButton disabled: " + unregisterButton.isDisabled() + " (newSelection: " + (newSelection != null ? newSelection.getEventCode() : "null") + ")");
        });

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
        System.out.println("Loaded all events: " + allEventsList.size());
    }

    private void loadRegisteredEvents() {
        // Preserve the current selection in allEventsTable
        Event previouslySelectedEvent = allEventsTable.getSelectionModel().getSelectedItem();

        registeredEventsList.clear();
        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            System.out.println("Cannot load registered events: studentUsername is not set.");
            return;
        }

        List<Event> allEvents = eventDAO.getAllEvents();
        for (Event event : allEvents) {
            List<String> registeredStudents = event.getRegisteredStudents();
            if (registeredStudents != null && registeredStudents.contains(studentUsername)) {
                registeredEventsList.add(event);
            }
        }
        yourEventsTable.setItems(registeredEventsList);
        System.out.println("Loaded registered events for student " + studentUsername + ": " + registeredEventsList.size());

        // Restore the selection in allEventsTable
        if (previouslySelectedEvent != null) {
            allEventsTable.getSelectionModel().select(previouslySelectedEvent);
            selectedEvent = previouslySelectedEvent;
            System.out.println("Restored selection in allEventsTable: " + previouslySelectedEvent.getEventCode());
        }
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
    private void handleRegisterStudentDialog() {
        System.out.println("handleRegisterStudentDialog called. selectedEvent: " + (selectedEvent != null ? selectedEvent.getEventCode() : "null"));

        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        if (studentUsername == null || studentUsername.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student username is not set. Cannot register.");
            return;
        }

        List<String> registeredStudents = selectedEvent.getRegisteredStudents();
        if (registeredStudents == null) {
            registeredStudents = new ArrayList<>();
            selectedEvent.setRegisteredStudents(registeredStudents);
        } else {
            registeredStudents = new ArrayList<>(registeredStudents);
        }

        if (registeredStudents.contains(studentUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Already Registered",
                    "You are already registered for this event. You may have been pre-registered via an import.");
            return;
        }

        if (registeredStudents.size() >= selectedEvent.getEventCapacity()) {
            showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", "The event has reached its maximum capacity.");
            return;
        }

        registeredStudents.add(studentUsername);
        selectedEvent.setRegisteredStudents(registeredStudents);
        eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
        ExExporter.recordNotification("Event", "Student " + studentUsername + " registered for event " + selectedEvent.getEventCode() + " (" + selectedEvent.getEventName() + ")");
        loadRegisteredEvents();
        exporter.exportData();
        showAlert(Alert.AlertType.INFORMATION, "Success", "You have been registered for " + selectedEvent.getEventName());
        tabPane.getSelectionModel().select(2);
    }

    @FXML
    private void handleUnregisterStudentDialog() {
        System.out.println("handleUnregisterStudentDialog called. selectedEvent: " + (selectedEvent != null ? selectedEvent.getEventCode() : "null"));

        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        List<String> registeredStudents = selectedEvent.getRegisteredStudents();
        if (registeredStudents == null) {
            registeredStudents = new ArrayList<>();
            selectedEvent.setRegisteredStudents(registeredStudents);
        } else {
            registeredStudents = new ArrayList<>(registeredStudents);
        }

        if (!registeredStudents.contains(studentUsername)) {
            showAlert(Alert.AlertType.INFORMATION, "Not Registered", "You are not registered for this event.");
            return;
        }

        registeredStudents.remove(studentUsername);
        selectedEvent.setRegisteredStudents(registeredStudents);
        eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
        ExExporter.recordNotification("Event", "Student " + studentUsername + " unregistered from event " + selectedEvent.getEventCode() + " (" + selectedEvent.getEventName() + ")");
        loadRegisteredEvents();
        exporter.exportData();
        showAlert(Alert.AlertType.INFORMATION, "Success", "You have been unregistered from " + selectedEvent.getEventName());
        tabPane.getSelectionModel().select(2);
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

        Label studentsLabel = new Label("Registered People:");
        ListView<String> studentsListView = new ListView<>();
        ObservableList<String> studentDetails = FXCollections.observableArrayList();
        List<String> registeredStudents = event.getRegisteredStudents();

        // Use a Set to deduplicate student IDs
        Set<String> uniqueStudentIds = new HashSet<>();
        if (registeredStudents == null || registeredStudents.isEmpty()) {
            studentDetails.add("No students registered.");
        } else {
            uniqueStudentIds.addAll(registeredStudents);

            for (String studentId : uniqueStudentIds) {
                if (studentId == null || studentId.equals("null") || studentId.trim().isEmpty()) {
                    continue;
                }
                if (studentId.equals(studentUsername) && studentName != null && !studentName.trim().isEmpty()) {
                    studentDetails.add(studentName);
                } else {
                    Student student = UniversityManagementApp.studentDAO.getStudentById(studentId);
                    if (student != null && student.getName() != null && !student.getName().trim().isEmpty()) {
                        studentDetails.add(student.getName());
                    } else {
                        studentDetails.add(studentId); // Fallback to ID if name not found
                    }
                }
            }
        }
        studentsListView.setItems(studentDetails);
        studentsListView.setPrefHeight(100);

        vbox.getChildren().addAll(
                eventHeaderImage,
                codeLabel, nameLabel, descriptionLabel, locationLabel, dateTimeLabel,
                capacityLabel, costLabel, studentsLabel, studentsListView
        );

        Scene scene = new Scene(vbox, 400, 500);
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


    public void setStudentUsername(String username) {
        this.studentUsername = username;
        System.out.println("EventStudentController: Set studentUsername to: " + studentUsername);
        // Load registered events now that studentUsername is set
        loadRegisteredEvents();
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
        System.out.println("EventStudentController: Set studentName to: " + studentName);
    }
}