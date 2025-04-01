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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class EventAdminController {

    @FXML
    private AnchorPane rootPane;

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
    private Tab manageEventsPane;

    @FXML
    private TextField codeField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField locationField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextField timeField;

    @FXML
    private TextField capacityField;

    @FXML
    private TextField costField;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button registerStudentButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button unregisterStudentButton;

    @FXML
    private Button browseImageButton;

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

    private String currentImagePath = null; // Remains String

    @FXML
    public void initialize() {
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

        allEventsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadAllEvents();
        updateMonthLabel();
        populateCalendar();

        eventSearch.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));

        allEventsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            registerStudentButton.setDisable(newSelection == null);
            deleteButton.setDisable(newSelection == null);
            selectedEvent = newSelection;
            unregisterStudentButton.setDisable(newSelection == null);
        });

        allEventsTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Event selected = allEventsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetails(selected);
                }
            }
        });
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

    private void showEventDetails(Event event) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Event Details: " + event.getEventName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        ImageView detailImageView = new ImageView();
        String imagePath = event.getEventHeaderImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                detailImageView.setImage(new Image("file:" + imagePath));
            } catch (Exception e) {
                detailImageView.setImage(new Image(getClass().getResourceAsStream("/images/default.jpg")));
            }
        } else if (event.getEventHeaderImage() != null) {
            detailImageView.setImage(event.getEventHeaderImage());
        } else {
            detailImageView.setImage(new Image(getClass().getResourceAsStream("/images/default.jpg")));
        }
        detailImageView.setFitWidth(200);
        detailImageView.setFitHeight(200);
        detailImageView.setPreserveRatio(true);

        Label codeLabel = new Label("Code: " + event.getEventCode());
        Label nameLabel = new Label("Name: " + event.getEventName());
        Label descriptionLabel = new Label("Description: " + (event.getEventDescription() != null ? event.getEventDescription() : "Not set"));
        Label locationLabel = new Label("Location: " + event.getEventLocation());
        Label dateTimeLabel = new Label("Date & Time: " + (event.getEventDateTime() != null ? new SimpleDateFormat("MM/dd/yyyy HH:mm").format(event.getEventDateTime()) : "N/A"));
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
                studentDetails.add(student != null ? student.getName() : studentId);
            }
        }
        studentsListView.setItems(studentDetails);

        vbox.getChildren().addAll(
                detailImageView,
                codeLabel, nameLabel, descriptionLabel, locationLabel, dateTimeLabel, capacityLabel, costLabel,
                studentsLabel, studentsListView
        );

        Scene scene = new Scene(vbox, 400, 600);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    @FXML
    private void handleAddEvent() {
        handleClearForm();
        selectedEvent = null;
        tabPane.getSelectionModel().select(manageEventsPane);
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
            currentImagePath = selectedEvent.getEventHeaderImagePath();
            tabPane.getSelectionModel().select(manageEventsPane);
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an event to edit.");
        }
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path resourceDir = Paths.get("src/main/resources/images");
                if (!Files.exists(resourceDir)) {
                    Files.createDirectories(resourceDir);
                }
                String fileName = (codeField.getText().isEmpty() ?
                        "event_" + System.currentTimeMillis() :
                        codeField.getText() + "_image") + getFileExtension(selectedFile.getName());
                Path targetPath = resourceDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);
                currentImagePath = targetPath.toAbsolutePath().toString();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to copy image: " + e.getMessage());
            }
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }

    @FXML
    private void handleRegisterStudentDialog() {
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "No Event Selected", "Please select an event first.");
            return;
        }
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
            List<String> registeredStudents = new ArrayList<>(selectedEvent.getRegisteredStudents());
            if (registeredStudents.contains(studentId)) {
                showAlert(Alert.AlertType.INFORMATION, "Already Registered", "This student is already registered.");
                return;
            }
            if (registeredStudents.size() >= selectedEvent.getEventCapacity()) {
                showAlert(Alert.AlertType.ERROR, "Capacity Exceeded", "The event has reached its maximum capacity.");
                return;
            }
            registeredStudents.add(studentId);
            selectedEvent.setRegisteredStudents(registeredStudents);
            eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
            ExExporter.recordActivity("Event", "Student " + studentId + " registered for event " + selectedEvent.getEventCode());
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
            showAlert(Alert.AlertType.INFORMATION, "No Students", "No students are registered for this event.");
            return;
        }
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Unregister Student from Event: " + selectedEvent.getEventName());
        VBox dialogVBox = new VBox(10);
        dialogVBox.setPadding(new Insets(10));
        ComboBox<String> studentComboBox = new ComboBox<>();
        studentComboBox.setPromptText("Select Student");
        for (String studentId : selectedEvent.getRegisteredStudents()) {
            Student student = studentDAO.getStudentById(studentId);
            studentComboBox.getItems().add(student != null ? student.getStudentId() + " - " + student.getName() : studentId);
        }
        Button unregisterButton = new Button("Unregister");
        unregisterButton.setOnAction(e -> {
            String selectedStudentString = studentComboBox.getSelectionModel().getSelectedItem();
            if (selectedStudentString == null) {
                showAlert(Alert.AlertType.WARNING, "No Student Selected", "Please select a student to unregister.");
                return;
            }
            String studentId = selectedStudentString.split(" - ")[0];
            List<String> registeredStudents = new ArrayList<>(selectedEvent.getRegisteredStudents());
            if (!registeredStudents.contains(studentId)) {
                showAlert(Alert.AlertType.INFORMATION, "Not Registered", "This student is not registered.");
                return;
            }
            registeredStudents.remove(studentId);
            selectedEvent.setRegisteredStudents(registeredStudents);
            eventDAO.updateEvent(selectedEvent.getEventCode(), selectedEvent);
            ExExporter.recordActivity("Event", "Student " + studentId + " unregistered from event " + selectedEvent.getEventCode());
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
                    "Are you sure you want to delete the event '" + selectedEvent.getEventName() + "'?",
                    ButtonType.OK, ButtonType.CANCEL);
            confirm.setTitle("Confirm Deletion");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    eventDAO.deleteEvent(selectedEvent.getEventCode());
                    loadAllEvents();
                    populateCalendar();
                    ExExporter.recordActivity("Event", "Event " + selectedEvent.getEventCode() + " deleted.");
                    exporter.exportData();
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

        // Pass null for Image, use currentImagePath as String
        Event event = new Event(
                nameField.getText().trim(),
                codeField.getText().trim(),
                descriptionField.getText().trim(),
                currentImagePath,
                locationField.getText().trim(),
                dateTime,
                Integer.parseInt(capacityField.getText().trim()),
                costField.getText().trim(),
                selectedEvent != null ? selectedEvent.getRegisteredStudents() : new ArrayList<>()
        );

        if (selectedEvent == null) {
            eventDAO.addEvent(event);
            ExExporter.recordActivity("Event", "Event " + event.getEventCode() + " created.");
        } else {
            eventDAO.updateEvent(selectedEvent.getEventCode(), event);
            ExExporter.recordActivity("Event", "Event " + event.getEventCode() + " updated.");
            selectedEvent = null;
        }

        loadAllEvents();
        populateCalendar();
        clearForm();
        exporter.exportData();
        tabPane.getSelectionModel().select(1); // Switch back to "All Events"
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
        currentImagePath = null;
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        selectedEvent = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}