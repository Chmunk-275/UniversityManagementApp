package com.example.universitymanagementapp;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UniversityManagementController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}