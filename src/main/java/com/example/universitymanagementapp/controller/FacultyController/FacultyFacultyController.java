package com.example.universitymanagementapp.controller.FacultyController;

import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.dao.FacultyDAO;
import com.example.universitymanagementapp.model.Faculty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    // Search bar
    @FXML
    private TextField searchField;

    // FacultyDAO instance for data access
    private FacultyDAO facultyDAO = UniversityManagementApp.facultyDAO;

    // Observable list to hold the full list of faculty (unfiltered)
    private ObservableList<Faculty> allFacultyList;

    private FacultyDashboard parentController;

    public void setParentController(FacultyDashboard parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        // Instantiate the DAO (this assumes that faculty data is already imported)
        facultyDAO = new FacultyDAO();

        // Set up cell value factories for "All Faculty" table
        facultyIdColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("degree"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load all faculty data from the DAO into an observable list
        allFacultyList = FXCollections.observableArrayList(facultyDAO.getAllFaculty());
        allFacultyTable.setItems(allFacultyList);

        // Add double-click event handler to open faculty details
        allFacultyTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double-click detected
                Faculty selectedFaculty = allFacultyTable.getSelectionModel().getSelectedItem();
                if (selectedFaculty != null) {
                    showFacultyDetails(selectedFaculty);
                }
            }
        });

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterFacultyList(newValue);
        });
    }

    // Method to filter the faculty list based on the search input
    private void filterFacultyList(String searchText) {
        ObservableList<Faculty> filteredList = FXCollections.observableArrayList();

        // If search text is empty, show all faculty
        if (searchText == null || searchText.isEmpty()) {
            allFacultyTable.setItems(allFacultyList);
            return;
        }

        // Filter the list based on name or ID (username)
        String lowerCaseSearchText = searchText.toLowerCase();
        for (Faculty faculty : allFacultyList) {
            if (faculty.getName().toLowerCase().contains(lowerCaseSearchText) ||
                    faculty.getUsername().toLowerCase().contains(lowerCaseSearchText)) {
                filteredList.add(faculty);
            }
        }

        // Update the TableView with the filtered list
        allFacultyTable.setItems(filteredList);
    }

    // Method to show faculty details in a new window
    private void showFacultyDetails(Faculty faculty) {

        Stage detailsStage = new Stage();
        detailsStage.setTitle("Faculty Details");
        detailsStage.initModality(Modality.APPLICATION_MODAL);

        // Create a layout for the details
        VBox detailsLayout = new VBox(10);
        detailsLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        // Add faculty details as text
        detailsLayout.getChildren().add(new Text("Faculty Details"));
        detailsLayout.getChildren().add(new Text("ID: " + faculty.getUsername()));
        detailsLayout.getChildren().add(new Text("Name: " + faculty.getName()));
        detailsLayout.getChildren().add(new Text("Department: " + faculty.getDegree()));
        detailsLayout.getChildren().add(new Text("Email: " + faculty.getEmail()));

        // Create a scene and set it on the stage
        Scene scene = new Scene(detailsLayout, 300, 200);
        detailsStage.setScene(scene);

        // Show the stage
        detailsStage.show();
    }
}