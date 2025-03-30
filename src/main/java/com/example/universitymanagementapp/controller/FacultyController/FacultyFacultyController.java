package com.example.universitymanagementapp.controller.FacultyController;
import com.example.universitymanagementapp.model.Admins;
import com.example.universitymanagementapp.dao.FacultyDAO;
import com.example.universitymanagementapp.model.Faculty;
import com.example.universitymanagementapp.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class FacultyFacultyController {

    // TableView for all faculty members
    @FXML
    private TableView<Faculty> allFacultyTable;
    @FXML
    private TableColumn<Faculty, String> facultyIdColumn;
    @FXML
    private TableColumn<Faculty, String> nameColumn;
    @FXML
    private TableColumn<Faculty, String> departmentColumn;
    @FXML
    private TableColumn<Faculty, String> emailColumn;

    // TableView for selected faculty members
    @FXML
    private TableView<Faculty> selectedFacultyTable;
    @FXML
    private TableColumn<Faculty, String> selectedFacultyIdColumn;
    @FXML
    private TableColumn<Faculty, String> selectedNameColumn;
    @FXML
    private TableColumn<Faculty, String> selectedDepartmentColumn;
    @FXML
    private TableColumn<Faculty, String> selectedEmailColumn;

    // Buttons for delete and edit
    @FXML
    private Button deleteButton;
    @FXML
    private Button editButton;

    // Text fields for editing (optional, for simplicity)
    @FXML
    private TextField editNameField;
    @FXML
    private TextField editDegreeField;
    @FXML
    private TextField editEmailField;

    // FacultyDAO instance for data access
    private FacultyDAO facultyDAO;
    private FacultyDashboard parentController;
    private Admins admin; // To use deleteUser method

    // Observable lists for the tables
    private ObservableList<Faculty> allFaculty;
    private ObservableList<Faculty> selectedFaculty;

    public void setParentController(FacultyDashboard parentController) {
        this.parentController = parentController;
    }

    // Method to set the admin (needed for deleteUser)
    public void setAdmin(Admins admin) {
        this.admin = admin;
    }

    @FXML
    public void initialize() {
        // Instantiate the DAO
        facultyDAO = new FacultyDAO();

        // Set up cell value factories for "All Faculty" table
        facultyIdColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Set up cell value factories for "Selected Faculty" table
        selectedFacultyIdColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        selectedNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        selectedDepartmentColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        selectedEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load all faculty data from the DAO into an observable list
        allFaculty = FXCollections.observableArrayList(facultyDAO.getAllFaculty());
        allFacultyTable.setItems(allFaculty);

        // Initialize the selected faculty list (empty at start)
        selectedFaculty = FXCollections.observableArrayList();
        selectedFacultyTable.setItems(selectedFaculty);

        // Add a listener to add a faculty member to the selected table when clicked
        allFacultyTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !selectedFaculty.contains(newValue)) {
                selectedFaculty.add(newValue);
            }
        });

        // Set up button actions
        deleteButton.setOnAction(event -> handleDelete());
        editButton.setOnAction(event -> handleEdit());
    }

    // Handle delete button action
    private void handleDelete() {
        Faculty selected = selectedFacultyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "No faculty member selected to delete.");
            return;
        }

        // Use the admin's deleteUser method to remove the faculty from the users list
        if (admin != null && admin.deleteUser(selected.getUsername())) {
            // Remove from both tables
            allFaculty.remove(selected);
            selectedFaculty.remove(selected);
            showAlert("Success", "Faculty member deleted successfully.");
        } else {
            showAlert("Error", "Failed to delete faculty member.");
        }
    }

    // Handle edit button action
    private void handleEdit() {
        Faculty selected = selectedFacultyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "No faculty member selected to edit.");
            return;
        }

        // Get new values from text fields
        String newName = editNameField.getText();
        String newDegree = editDegreeField.getText();
        String newEmail = editEmailField.getText();

        // Validate input
        if (newName == null || newName.trim().isEmpty() ||
                newDegree == null || newDegree.trim().isEmpty() ||
                newEmail == null || newEmail.trim().isEmpty()) {
            showAlert("Error", "All fields must be filled to edit faculty member.");
            return;
        }

        // Update the faculty member's details
        selected.setName(newName);
        selected.setDegree(newDegree);
        selected.setEmail(newEmail);

        // Refresh the tables to reflect changes
        allFacultyTable.refresh();
        selectedFacultyTable.refresh();
        showAlert("Success", "Faculty member updated successfully.");

        // Clear the text fields
        editNameField.clear();
        editDegreeField.clear();
        editEmailField.clear();
    }

    // Utility method to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


    // refresh the data, or handle other UI events.
