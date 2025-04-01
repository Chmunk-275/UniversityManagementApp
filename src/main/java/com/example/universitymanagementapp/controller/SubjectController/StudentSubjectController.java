package com.example.universitymanagementapp.controller.SubjectController;

import com.example.universitymanagementapp.dao.SubjectDAO;
import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Subject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentSubjectController implements Initializable {

    // FXML components
    @FXML
    private TableView<Subject> allSubjectsTable;
    @FXML
    private TableColumn<Subject, String> subjectNameColumn;
    @FXML
    private TableColumn<Subject, String> subjectCodeColumn;
    @FXML
    private TextField subjectSearch;

    // Access SubjectDAO from HelloApplication
    private SubjectDAO subjectDAO = UniversityManagementApp.subjectDAO;

    // Observable list to hold subjects
    private ObservableList<Subject> subjectsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns for the table
        subjectNameColumn.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("subjectCode"));

        // Set table resize policy
        allSubjectsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all subjects initially
        loadAllSubjects();

        // Add listener to subjectSearch TextField for real-time filtering
        subjectSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterSubjects(newValue);
        });
    }

    // Load all subjects into the table
    private void loadAllSubjects() {
        subjectsList.clear();
        subjectsList.addAll(subjectDAO.getAllSubjects());
        allSubjectsTable.setItems(subjectsList);
        System.out.println("Loaded all subjects: " + subjectsList);
    }

    // Filter subjects based on search input and update the same table
    private void filterSubjects(String searchText) {
        subjectsList.clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            subjectsList.addAll(subjectDAO.getAllSubjects());
        } else {
            String lowerCaseSearch = searchText.trim().toLowerCase();
            for (Subject subject : subjectDAO.getAllSubjects()) {
                if (subject.getSubjectName().toLowerCase().contains(lowerCaseSearch) ||
                        subject.getSubjectCode().toLowerCase().contains(lowerCaseSearch)) {
                    subjectsList.add(subject);
                }
            }
        }

        allSubjectsTable.setItems(subjectsList);
        System.out.println("Filtered subjects for '" + searchText + "': " + subjectsList);
    }
}