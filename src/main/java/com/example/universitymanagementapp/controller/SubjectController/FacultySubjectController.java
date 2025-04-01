package com.example.universitymanagementapp.controller.SubjectController;

import com.example.universitymanagementapp.controller.FacultyController.FacultyDashboard;
import com.example.universitymanagementapp.dao.SubjectDAO;
import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;


public class FacultySubjectController {

    @FXML
    public TableView<Subject> allSubjectsTable;

    @FXML
    public TextField subjectSearch;

    @FXML
    public TableColumn<Subject, String> subjectNameColumn;

    @FXML
    public TableColumn<Subject, String> subjectCodeColumn;

    private SubjectDAO subjectDAO = UniversityManagementApp.subjectDAO;
    private ObservableList<Subject> allSubjectsList = FXCollections.observableArrayList();
    private ObservableList<Subject> filteredSubjectsList = FXCollections.observableArrayList();

    private FacultyDashboard parentController;

    public void setParentController(FacultyDashboard parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        // Configure columns for All Subjects table
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));

        // Set table resize policy
        allSubjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all subjects
        loadAllSubjects();

        // Add listener to subjectSearch TextField for real-time filtering
        subjectSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSubjects(newValue);
        });
    }

    // Load all subjects into the table
    private void loadAllSubjects() {
        allSubjectsList.clear();
        allSubjectsList.addAll(subjectDAO.getAllSubjects());
        filteredSubjectsList.clear();
        filteredSubjectsList.addAll(allSubjectsList);
        allSubjectsTable.setItems(filteredSubjectsList);
        System.out.println("Loaded all subjects: " + allSubjectsList);
    }

    // Filter subjects in the allSubjectsTable based on search input
    private void filterSubjects(String searchText) {
        filteredSubjectsList.clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredSubjectsList.addAll(allSubjectsList);
        } else {
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Subject subject : allSubjectsList) {
                if (subject.getSubjectName().toLowerCase().contains(lowerCaseSearch) ||
                        subject.getSubjectCode().toLowerCase().contains(lowerCaseSearch)) {
                    filteredSubjectsList.add(subject);
                }
            }
        }
        allSubjectsTable.setItems(filteredSubjectsList);
        System.out.println("Filtered subjects for '" + searchText + "': " + filteredSubjectsList);
    }
}