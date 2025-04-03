package com.example.universitymanagementapp.controller.EventController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Event;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.dao.EventDAO;
import com.example.universitymanagementapp.dao.StudentDAO;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class EventAdminController {

    // registering and unregistering a student from events gets sent as notification

    @FXML private AnchorPane rootPane;
    @FXML private TabPane tabPane;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevMonthButton;
    @FXML private Label monthLabel;
    @FXML private Button nextMonthButton;
    @FXML private TableView<Event> allEventsTable;
    @FXML private TableColumn<Event, String> codeColumn;
    @FXML private TableColumn<Event, String> nameColumn;
    @FXML private TableColumn<Event, String> dateColumn;
    @FXML private TableColumn<Event, String> locationColumn;
    @FXML private TableColumn<Event, Integer> capacityColumn;
    @FXML private TableColumn<Event, String> costColumn;
    @FXML private TextField eventSearch;
    @FXML private AnchorPane manageEventsPane;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField locationField;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField capacityField;
    @FXML private TextField costField;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button registerStudentButton;
    @FXML private Button deleteButton;
    @FXML private Button saveButton;
    @FXML private Button clearButton;
    @FXML private Button unregisterStudentButton;

    // New FXML elements for image handling
    @FXML private ImageView currentHeaderImageView;
    @FXML private Button selectImageButton;
    @FXML private Button resetImageButton;
    @FXML private Label imageStatusLabel;

    // DAO and Exporter
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;
    private EventDAO eventDAO = UniversityManagementApp.eventDAO;
    private List<Student> students = UniversityManagementApp.studentDAO.getAllStudents();
    private ExExporter exporter = new ExExporter(UniversityManagementApp.courseDAO, UniversityManagementApp.studentDAO,
            UniversityManagementApp.facultyDAO, UniversityManagementApp.subjectDAO, eventDAO);

    // Observable lists
    private ObservableList<Event> allEventsList = FXCollections.observableArrayList();
    private Event selectedEvent = null;

    private int currentYear = LocalDate.now().getYear();
    private int currentMonth = LocalDate.now().getMonthValue();

    // Variables for image handling
    private Image defaultHeaderImage;
    private Image currentHeaderImage;
    private String currentHeaderImagePath;

    @FXML
    public void initialize() {
        // Configure columns for All Events table
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("eventCode"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("eventName"));
        dateColumn.setCellValueFactory(cellData -> {
            Date dateTime = cellData.getValue().getEventDateTime();
            return new javafx.beans.property.SimpleStringProperty(
                    dateTime != null ? new SimpleDateFormat("MM/dd/yyyy HH:mm").format(dateTime) : "N/A");
        });
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("eventLocation"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("eventCapacity"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("eventCost"));

        // Set table resize policies
        allEventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all events
        loadAllEvents();

        // Update the month label and populate the calendar
        updateMonthLabel();
        populateCalendar();

        // Add listener to eventSearch TextField for real-time filtering
        eventSearch.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));

        // Enable/disable buttons based on selection
        allEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            registerStudentButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
            selectedEvent = newSelection;
            unregisterStudentButton.setDisable(newSelection == null);
        });

        // Set up double-click handler for All Events table
        allEventsTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Event selected = allEventsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetails(selected);
                }
            }
        });

        // Initialize the default header image
        try {
            defaultHeaderImage = new Image(getClass().getResourceAsStream("/images/eventsdefault.jpg"));
            currentHeaderImage = defaultHeaderImage;
            currentHeaderImagePath = "default";
            currentHeaderImageView.setImage(defaultHeaderImage);
        } catch (Exception e) {
            System.out.println("Error loading default event header image: " + e.getMessage());
            imageStatusLabel.setText("Error loading default image");
        }
    }

    private void loadAllEvents() {
        allEventsList.clear();
        allEventsList.addAll(eventDAO.getAllEvents());
        allEventsTable.setItems(allEventsList);
    }

    private void updateMonthLabel() {
        String monthName = YearMonth.of(currentYear, currentMonth)
                .getMonth()
                .getDisplayName(TextStyle.FULL, Locale.getDefault());
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
                int eventDay = cal.get(Calendar.DAY_OF_MONTH);
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

            List<Event> eventsOnDay = new ArrayList<>();
            for (Event event : eventsInMonth) {
                cal.setTime(event.getEventDateTime());
                int eventDay = cal.get(Calendar.DAY_OF_MONTH);
                if (eventDay == day) {
                    eventsOnDay.add(event);
                }
            }

            if (!eventsOnDay.isEmpty()) {
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
        } else {
            try {
                eventHeaderImage.setImage(new Image(getClass().getResourceAsStream("/images/eventsdefault.jpg")));
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

        Label studentsLabel = new Label("Registered Students:");
        ListView<String> studentsListView = new ListView<>();
        ObservableList<String> studentDetails = FXCollections.observableArrayList();
        List<String> registeredStudents = event.getRegisteredStudents();
        if (registeredStudents.isEmpty()) {
            studentDetails.add("No students registered.");
        } else {
            for (String studentId : registeredStudents) {
                Student student = studentDAO.getStudentById(studentId);
                if (student != null) {
                    studentDetails.add(student.getName());
                } else {
                    studentDetails.add(studentId);
                }
            }
        }
        studentsListView.setItems(studentDetails);
        studentsListView.setPrefHeight(100);

        vbox.getChildren().addAll(
                eventHeaderImage,
                codeLabel, nameLabel, descriptionLabel, locationLabel, dateTimeLabel, capacityLabel, costLabel,
                studentsLabel, studentsListView
        );

        Scene scene = new Scene(vbox, 400, 500);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    @FXML
    private void handleAddEvent() {
        handleClearForm();
        selectedEvent = null;
        tabPane.getSelectionModel().select(2);
        // Reset image to default when adding a new event
        currentHeaderImage = defaultHeaderImage;
        currentHeaderImagePath = "default";
        currentHeaderImageView.setImage(defaultHeaderImage);
        imageStatusLabel.setText("");
    }

    @FXML
    private void handleEditEvent() {
        if (selectedEvent != null) {
            codeField.setText(selectedEvent.getEventCode());
            nameField.setText(selectedEvent.getEventName());
            descriptionField.setText(selectedEvent.getEventDescription());
            locationField.setText(selectedEvent.getEventLocation());
            if (selectedEvent.getEventDateTime() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(selectedEvent.getEventDateTime());
                datePicker.setValue(LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
                timeField.setText(new SimpleDateFormat("HH:mm").format(selectedEvent.getEventDateTime()));
            }
            capacityField.setText(String.valueOf(selectedEvent.getEventCapacity()));
            costField.setText(selectedEvent.getEventCost());

            // Load the event's header image and path
            currentHeaderImage = selectedEvent.getEventHeaderImage() != null ? selectedEvent.getEventHeaderImage() : defaultHeaderImage;
            currentHeaderImagePath = selectedEvent.getHeaderImagePath() != null ? selectedEvent.getHeaderImagePath() : "default";
            currentHeaderImageView.setImage(currentHeaderImage);
            imageStatusLabel.setText("Current image: " + (currentHeaderImagePath.equals("default") ? "Default" : "Custom"));

            tabPane.getSelectionModel().select(2);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to edit.");
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Header Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String imagePath = selectedFile.toURI().toString();
                Image newImage = new Image(imagePath);
                if (newImage.isError()) {
                    throw new Exception("Invalid image file");
                }
                currentHeaderImage = newImage;
                currentHeaderImagePath = imagePath;
                currentHeaderImageView.setImage(currentHeaderImage);
                imageStatusLabel.setText("Image selected: " + selectedFile.getName());
            } catch (Exception e) {
                imageStatusLabel.setText("Error loading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleResetImage() {
        currentHeaderImage = defaultHeaderImage;
        currentHeaderImagePath = "default";
        currentHeaderImageView.setImage(defaultHeaderImage);
        imageStatusLabel.setText("Image reset to default");
    }

    @FXML
    private void handleRegisterStudentDialog() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        // Create a dialog for registering a student
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Register Student for Event: " + selectedEvent.getEventName());

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        ComboBox<String> studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Select Student");
        List<Student> allStudents = studentDAO.getAllStudents();
        for (Student student : allStudents) {
            studentComboBox.getItems().add(student.getStudentId() + " - " + student.getName());
        }

        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            String selectedStudentString = studentComboBox.getSelectionModel().getSelectedItem();
            if (selectedStudentString == null) {
                showAlert(Alert.AlertType.WARNING, "No Student Selected", "Please select a student to register.");
                return;
            }

            String studentId = selectedStudentString.split(" - ")[0];
            // Create a mutable copy of the registered students list
            List<String> registeredStudents = new ArrayList<>(selectedEvent.getRegisteredStudents());

            if (registeredStudents.contains(studentId)) {
                showAlert(Alert.AlertType.INFORMATION, "Already Registered", "This student is already registered for the event.");
                return;
            }

            if (registeredStudents.size() >= selectedEvent.getEventCapacity()) {
                showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", "The event has reached its maximum capacity.");
                return;
            }

            registeredStudents.add(studentId);
            // Update the event's registered students list
            selectedEvent.setRegisteredStudents(registeredStudents);
            eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
            ExExporter.recordNotification("Event", "Student " + studentId + " registered for event " + selectedEvent.getEventCode() + " (" + selectedEvent.getEventName() + ").");
            exporter.exportData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student " + studentId + " registered successfully.");
            dialogStage.close();
        });

        dialogVBox.getChildren().addAll(new Label("Select a student to register:"), studentComboBox, registerButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    @FXML
    private void handleUnregisterStudentDialog() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }

        if (selectedEvent.getRegisteredStudents().isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Students", "There are no students registered for this event.");
            return;
        }

        // Create a dialog for unregistering a student
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Unregister Student from Event: " + selectedEvent.getEventName());

        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));

        ComboBox<String> studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Select Student");
        for (String studentId : selectedEvent.getRegisteredStudents()) {
            Student student = studentDAO.getStudentById(studentId);
            if (student != null) {
                studentComboBox.getItems().add(student.getStudentId() + " - " + student.getName());
            } else {
                studentComboBox.getItems().add(studentId);
            }
        }

        Button unregisterButton = new Button("Unregister");
        unregisterButton.setOnAction(e -> {
            String selectedStudentString = studentComboBox.getSelectionModel().getSelectedItem();
            if (selectedStudentString == null) {
                showAlert(Alert.AlertType.WARNING, "No Student Selected", "Please select a student to unregister.");
                return;
            }

            String studentId = selectedStudentString.split(" - ")[0];
            // Create a mutable copy of the registered students list
            List<String> registeredStudents = new ArrayList<>(selectedEvent.getRegisteredStudents());

            if (!registeredStudents.contains(studentId)) {
                showAlert(Alert.AlertType.INFORMATION, "Not Registered", "This student is not registered for the event.");
                return;
            }

            registeredStudents.remove(studentId);
            // Update the event's registered students list
            selectedEvent.setRegisteredStudents(registeredStudents);
            eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
            ExExporter.recordNotification("Event", "Student " + studentId + " unregistered from event " + selectedEvent.getEventCode() + " (" + selectedEvent.getEventName() + ").");
            exporter.exportData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Student " + studentId + " unregistered successfully.");
            dialogStage.close();
        });

        dialogVBox.getChildren().addAll(new Label("Select a student to unregister:"), studentComboBox, unregisterButton);

        Scene dialogScene = new Scene(dialogVBox, 300, 150);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    @FXML
    private void handleDeleteEvent() {
        if (selectedEvent != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete the event '" + selectedEvent.getEventName() + "' (" + selectedEvent.getEventCode() + ")?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirm Deletion");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    eventDAO.deleteEvent(selectedEvent.getEventCode());
                    ExExporter.recordActivity("Event", "Event " + selectedEvent.getEventCode() + " (" + selectedEvent.getEventName() + ") deleted.");
                    exporter.exportData();
                    loadAllEvents();
                    populateCalendar();
                }
            });
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to delete.");
        }
    }

    @FXML
    private void handleSaveEvent() {
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate date = datePicker.getValue();
        String time = timeField.getText().trim();
        String capacityStr = capacityField.getText().trim();
        String cost = costField.getText().trim();

        if (code.isEmpty() || name.isEmpty() || location.isEmpty() || date == null || time.isEmpty() || capacityStr.isEmpty() || cost.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields except description are required.");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityStr);
            if (capacity < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Capacity must be a non-negative number.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Capacity must be a valid number.");
            return;
        }

        Date dateTime = null;
        try {
            String[] timeParts = time.split(":");
            if (timeParts.length != 2) throw new IllegalArgumentException("Invalid time format");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new IllegalArgumentException("Invalid time values");
            }
            Calendar cal = Calendar.getInstance();
            cal.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth(), hour, minute);
            dateTime = cal.getTime();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Time must be in HH:MM format (e.g., 14:30).");
            return;
        }

        for (Event event : eventDAO.getAllEvents()) {
            if (selectedEvent != null && event.getEventCode().equals(selectedEvent.getEventCode())) {
                continue;
            }
            if (event.getEventCode().equals(code)) {
                showAlert(Alert.AlertType.ERROR, "Error", "Event code already exists.");
                return;
            }
        }

        Event event = new Event(
                nameField.getText().trim(),
                codeField.getText().trim(),
                descriptionField.getText().trim(),
                currentHeaderImage, // Use the current header image
                currentHeaderImagePath, // Use the current header image path
                locationField.getText().trim(),
                dateTime,
                Integer.parseInt(capacityField.getText().trim()),
                costField.getText().trim(),
                selectedEvent != null ? selectedEvent.getRegisteredStudents() : new ArrayList<>()
        );

        if (selectedEvent == null) {
            eventDAO.addEvent(event);
            ExExporter.recordActivity("Event", "Event " + event.getEventCode() + " (" + event.getEventName() + ") created.");
        } else {
            eventDAO.updateEvent(selectedEvent.getEventCode(), event);
            ExExporter.recordActivity("Event", "Event " + event.getEventCode() + " (" + event.getEventName() + ") updated.");
            selectedEvent = null;
        }

        loadAllEvents();
        populateCalendar();
        clearForm();
        exporter.exportData();
        tabPane.getSelectionModel().select(1);
    }

    private void clearForm() {
        codeField.clear();
        nameField.clear();
        descriptionField.clear();
        locationField.clear();
        datePicker.setValue(null);
        timeField.clear();
        capacityField.clear();
        costField.clear();
        currentHeaderImage = defaultHeaderImage;
        currentHeaderImagePath = "default";
        currentHeaderImageView.setImage(defaultHeaderImage);
        imageStatusLabel.setText("");
        selectedEvent = null;
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}