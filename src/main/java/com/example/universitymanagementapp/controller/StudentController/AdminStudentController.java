package com.example.universitymanagementapp.controller.StudentController;

import com.example.universitymanagementapp.controller.CourseController.CourseAdminController;
import com.example.universitymanagementapp.dao.StudentDAO;
import com.example.universitymanagementapp.dao.CourseDAO;
import com.example.universitymanagementapp.UniversityManagementApp;
import com.example.universitymanagementapp.model.Student;
import com.example.universitymanagementapp.model.Course;
import com.example.universitymanagementapp.model.Grade;
import com.example.universitymanagementapp.utils.ExExporter;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.universitymanagementapp.controller.AdminController.AdminDashboard;

public class AdminStudentController {

    private CourseAdminController courseAdminController;

    public void setCourseAdminController(CourseAdminController controller) {
        this.courseAdminController = controller;
    }

    private AdminDashboard parentController;

    public void setParentController(AdminDashboard parentController) {
        this.parentController = parentController;
    }

    @FXML
    private TextField passwordField; // Added for password

    @FXML
    private TextField addressField; // Added for address

    @FXML
    private TextField currentSemesterField; // Added for current semester

    @FXML
    private TextField academicLevelField; // Added for academic level

    @FXML
    private TextField thesisTitleField; // Added for thesis title

    @FXML
    private TabPane tabPane;

    @FXML
    private TableView<Student> allStudentsTable;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> studentIdColumn;

    @FXML
    private TableColumn<Student, String> emailColumn;

    @FXML
    private TableColumn<Student, String> phoneNumberColumn;

    @FXML
    private TableColumn<Student, Integer> enrolledCoursesColumn;

    @FXML
    private TableColumn<Student, Integer> enrolledSubjectsColumn;

    @FXML
    private Button addButton;

    @FXML
    private Button editButton;

    @FXML
    private Button enrollTabButton; // Button for navigating to Enroll tab

    @FXML
    private Button gradeTabButton; // New button for navigating to Grade tab

    @FXML
    private Button deleteButton;

    @FXML
    private TextField studentSearch;

    @FXML
    private TextField nameField;

    @FXML
    private TextField studentIdField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneNumberField;

    @FXML
    private ComboBox<Course> courseComboBox;

    @FXML
    private Button enrollButton;

    @FXML
    private Button unenrollButton;

    @FXML
    private TableView<Course> enrolledCoursesTable;

    @FXML
    private TableColumn<Course, String> enrolledCourseCodeColumn;

    @FXML
    private TableColumn<Course, String> enrolledCourseNameColumn;

    @FXML
    private ComboBox<Course> gradeCourseComboBox;

    @FXML
    private TextField gradeField;

    @FXML
    private Button updateGradeButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    // Create objects of DAO
    private StudentDAO studentDAO = UniversityManagementApp.studentDAO;
    private CourseDAO courseDAO = UniversityManagementApp.courseDAO;

    // Initialize exporter constructor
    private ExExporter exporter = new ExExporter(courseDAO, studentDAO, UniversityManagementApp.facultyDAO,
            UniversityManagementApp.subjectDAO, UniversityManagementApp.eventDAO);

    // Create observable lists for all content
    private ObservableList<Student> allStudentsList = FXCollections.observableArrayList();
    private ObservableList<Course> enrolledCoursesList = FXCollections.observableArrayList();
    private ObservableList<Course> availableCourses = FXCollections.observableArrayList();
    private Student selectedStudent = null;

    @FXML
    public void initialize() {
        // Configure columns for All Students table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        enrolledCoursesColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumberOfRegisteredCourses()).asObject());
        enrolledSubjectsColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getNumberOfRegisteredSubjects()).asObject());

        // Configure columns for Enrolled Courses table
        enrolledCourseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        enrolledCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));

        // Set table resize policies
        allStudentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        enrolledCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load all students
        loadAllStudents();

        // Add listener to studentSearch TextField for real-time search
        studentSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStudents(newValue);
        });

        // Enable/disable edit/enroll/grade/delete buttons based on selection
        allStudentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editButton.setDisable(newSelection == null);
            enrollTabButton.setDisable(newSelection == null);
            gradeTabButton.setDisable(newSelection == null); // Disable Grade button if no student is selected
            deleteButton.setDisable(newSelection == null);
        });

        // Enable/disable unenroll button based on selection
        enrolledCoursesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            unenrollButton.setDisable(newSelection == null);
        });

        // Double-click to view student details
        allStudentsTable.setRowFactory(tv -> {
            TableRow<Student> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Student student = row.getItem();
                    showStudentDetails(student);
                }
            });
            return row;
        });

        // Load available courses into ComboBox
        availableCourses.addAll(courseDAO.getAllCourses());
        courseComboBox.setItems(availableCourses);
        courseComboBox.setVisibleRowCount(5); // Limit to 5 visible rows, making it scrollable
        gradeCourseComboBox.setItems(enrolledCoursesList);
        gradeCourseComboBox.setVisibleRowCount(5); // Also limit the grade ComboBox
    }

    private void loadAllStudents() {
        allStudentsList.clear();
        allStudentsList.addAll(studentDAO.getAllStudents());
        // Update tuition for all students based on their current enrollment
        for (Student student : allStudentsList) {
            updateStudentTuition(student);
            studentDAO.updateStudent(student); // Persist the updated tuition
        }
        allStudentsTable.setItems(allStudentsList);
        System.out.println("Loaded all students: " + allStudentsList);
    }

    private void filterStudents(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            allStudentsTable.setItems(allStudentsList); // Show all students if search is empty
            return;
        }

        ObservableList<Student> filteredList = FXCollections.observableArrayList();
        String lowerCaseSearch = searchText.trim().toLowerCase();
        for (Student student : allStudentsList) {
            if (student.getName().toLowerCase().contains(lowerCaseSearch) ||
                    student.getStudentId().toLowerCase().contains(lowerCaseSearch) ||
                    student.getEmail().toLowerCase().contains(lowerCaseSearch)) {
                filteredList.add(student);
            }
        }
        allStudentsTable.setItems(filteredList);
        System.out.println("Filtered students for '" + searchText + "': " + filteredList);
    }

    private void updateStudentTuition(Student student) {
        if (student == null) {
            return;
        }
        int numberOfCourses = student.getRegisteredCourses() != null ? student.getRegisteredCourses().size() : 0;
        int tuition = numberOfCourses * 250; // $250 per course
        student.setTuition(tuition);
        System.out.println("Updated tuition for student " + student.getStudentId() + ": $" + tuition + " (" + numberOfCourses + " courses)");
    }

    @FXML
    private void handleAddStudent() {
        clearForm();
        selectedStudent = null;
        tabPane.getTabs().stream()
                .filter(tab -> "Manage Students".equals(tab.getText()))
                .findFirst()
                .ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        if (nameField != null) {
            nameField.requestFocus();
        }
    }

    @FXML
    private void handleEditStudent() {
        selectedStudent = allStudentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            nameField.setText(selectedStudent.getName());
            studentIdField.setText(selectedStudent.getStudentId());
            emailField.setText(selectedStudent.getEmail());
            phoneNumberField.setText(selectedStudent.getPhoneNumber());
            passwordField.setText(selectedStudent.getPlaintextPassword()); // Populate with plaintext password
            addressField.setText(selectedStudent.getAddress());
            currentSemesterField.setText(selectedStudent.getCurrentSemester());
            academicLevelField.setText(selectedStudent.getAcademicLevel());
            thesisTitleField.setText(selectedStudent.getThesisTitle());
            enrolledCoursesList.clear();
            enrolledCoursesList.addAll(selectedStudent.getRegisteredCourses());
            enrolledCoursesTable.setItems(enrolledCoursesList);
            tabPane.getTabs().stream()
                    .filter(tab -> "Manage Students".equals(tab.getText()))
                    .findFirst()
                    .ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        }
    }

    @FXML
    private void handleEnrollTab() {
        selectedStudent = allStudentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            enrolledCoursesList.clear();
            enrolledCoursesList.addAll(selectedStudent.getRegisteredCourses());
            enrolledCoursesTable.setItems(enrolledCoursesList);
            tabPane.getTabs().stream()
                    .filter(tab -> "Enroll Students".equals(tab.getText()))
                    .findFirst()
                    .ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        }
    }

    @FXML
    private void handleGradeTab() {
        selectedStudent = allStudentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            enrolledCoursesList.clear();
            enrolledCoursesList.addAll(selectedStudent.getRegisteredCourses());
            gradeCourseComboBox.setItems(enrolledCoursesList);
            tabPane.getTabs().stream()
                    .filter(tab -> "Manage Grades".equals(tab.getText()))
                    .findFirst()
                    .ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        }
    }

    @FXML
    private void handleDeleteStudent() {
        Student selected = allStudentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Build the confirmation message
            StringBuilder message = new StringBuilder("Delete student " + selected.getName() + " (" + selected.getStudentId() + ")?");

            // Check if the student is enrolled in any courses
            List<Course> registeredCourses = selected.getRegisteredCourses();
            if (!registeredCourses.isEmpty()) {
                message.append("\n\nThis student will also be unenrolled from the following courses:\n");
                for (Course course : registeredCourses) {
                    message.append("- ").append(course.getCourseName()).append(" (").append(course.getCourseCode()).append(")\n");
                }
            }

            // Show the confirmation dialog with the updated message
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message.toString());
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Create a copy of the registered courses list to avoid ConcurrentModificationException
                    List<Course> coursesToUnenroll = new ArrayList<>(selected.getRegisteredCourses());

                    // Unenroll the student from all courses
                    for (Course course : coursesToUnenroll) {
                        studentDAO.removeStudentFromCourse(selected, course);
                    }

                    // Update tuition after unenrolling (optional, since the student will be deleted)
                    updateStudentTuition(selected);
                    studentDAO.updateStudent(selected);

                    // Remove the student from the system
                    studentDAO.removeStudentById(selected.getStudentId());

                    // Clear the selection to avoid referencing the deleted student
                    allStudentsTable.getSelectionModel().clearSelection();

                    // Refresh the UI
                    loadAllStudents();
                    filterStudents(studentSearch.getText());

                    // Export updated data
                    exporter.exportData();
                }
            });
        }
    }

    @FXML
    private void handleEnrollCourse() {
        Course selectedCourse = courseComboBox.getSelectionModel().getSelectedItem();
        if (selectedCourse != null && !enrolledCoursesList.contains(selectedCourse)) {
            // Fetch the latest Course instance from CourseDAO
            Course courseFromDAO = courseDAO.getCourseByCode(selectedCourse.getCourseCode()).stream().findFirst().orElse(null);
            if (courseFromDAO == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Course not found.");
                return;
            }
            if (courseFromDAO.getCurrentEnrollment() >= courseFromDAO.getCapacity()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Course is full.");
                return;
            }

            // Enroll the student in the course (StudentDAO will update currentEnrollment)
            if (selectedStudent != null) {
                studentDAO.enrollStudentInCourse(selectedStudent, courseFromDAO);
                // Update the student's registered subjects
                String subjectCode = courseFromDAO.getSubjectCode();
                if (!selectedStudent.getRegisteredSubjects().contains(subjectCode)) {
                    selectedStudent.getRegisteredSubjects().add(subjectCode);
                }
                // Update tuition after enrolling in a course
                updateStudentTuition(selectedStudent);
                studentDAO.updateStudent(selectedStudent); // Persist the updated student
            }

            // Add the course to the enrolled courses list
            enrolledCoursesList.add(courseFromDAO);
            gradeCourseComboBox.setItems(enrolledCoursesList);

            // Refresh the available courses list
            availableCourses.clear();
            availableCourses.addAll(courseDAO.getAllCourses());
            courseComboBox.setItems(availableCourses);

            // Notify CourseAdminController to refresh its data
            if (courseAdminController != null) {
                courseAdminController.refreshCourses();
            }

            exporter.exportData(); // Export after enrolling a student in a course

            // Refresh the student list
            loadAllStudents();
            filterStudents(studentSearch.getText());
            tabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleUnenrollCourse() {
        Course selectedCourse = enrolledCoursesTable.getSelectionModel().getSelectedItem();
        if (selectedCourse != null) {
            // Fetch the latest Course instance from CourseDAO
            Course courseFromDAO = courseDAO.getCourseByCode(selectedCourse.getCourseCode()).stream().findFirst().orElse(null);
            if (courseFromDAO == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Course not found.");
                return;
            }

            // Unenroll the student from the course (StudentDAO will update currentEnrollment)
            if (selectedStudent != null) {
                studentDAO.removeStudentFromCourse(selectedStudent, courseFromDAO);
                // Update the student's registered subjects
                String subjectCode = courseFromDAO.getSubjectCode();
                boolean hasOtherCoursesForSubject = enrolledCoursesList.stream()
                        .anyMatch(course -> course.getSubjectCode().equals(subjectCode));
                if (!hasOtherCoursesForSubject) {
                    selectedStudent.getRegisteredSubjects().remove(subjectCode);
                }
                // Update tuition after unenrolling from a course
                updateStudentTuition(selectedStudent);
                studentDAO.updateStudent(selectedStudent); // Persist the updated student
            }

            // Remove the course from the enrolled courses list
            enrolledCoursesList.remove(selectedCourse);
            gradeCourseComboBox.setItems(enrolledCoursesList);

            // Refresh the available courses list
            availableCourses.clear();
            availableCourses.addAll(courseDAO.getAllCourses());
            courseComboBox.setItems(availableCourses);

            // Notify CourseAdminController to refresh its data
            if (courseAdminController != null) {
                courseAdminController.refreshCourses();
            }
            exporter.exportData(); // Export after unenrolling a student from a course

            // Refresh the student list
            loadAllStudents();
            filterStudents(studentSearch.getText());

            tabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleUpdateGrade() {
        Course selectedCourse = gradeCourseComboBox.getSelectionModel().getSelectedItem();
        String gradeText = gradeField.getText().trim();

        if (selectedCourse == null || gradeText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a course and enter a grade.");
            return;
        }

        int finalGrade;
        try {
            finalGrade = Integer.parseInt(gradeText);
            if (finalGrade < 0 || finalGrade > 100) {
                showAlert(Alert.AlertType.ERROR, "Error", "Grade must be between 0 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Grade must be a valid integer.");
            return;
        }

        if (selectedStudent != null) {
            // Update or create a Grade object for this course
            Grade grade = selectedStudent.getGrades().get(selectedCourse.getCourseCode());
            if (grade == null) {
                grade = new Grade(selectedCourse.getCourseCode(), finalGrade, 0, 0, 0, 0);
                selectedStudent.getGrades().put(selectedCourse.getCourseCode(), grade);
            } else {
                grade.setFinalGrade(finalGrade);
            }
            gradeField.clear();
            exporter.exportData(); // Export after updating a grade

            // Navigate back to the "All Students" tab
            tabPane.getSelectionModel().select(0);
        }
    }

    @FXML
    private void handleSaveStudent() {
        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String email = emailField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String password = passwordField.getText().trim(); // Get password
        String address = addressField.getText().trim(); // Get address
        String currentSemester = currentSemesterField.getText().trim(); // Get current semester
        String academicLevel = academicLevelField.getText().trim(); // Get academic level
        String thesisTitle = thesisTitleField.getText().trim(); // Get thesis title

        // Validate required fields
        if (name.isEmpty() || studentId.isEmpty() || email.isEmpty() || phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Name, Student ID, Email, and Phone Number are required.");
            return;
        }

        // Check for duplicate student ID or email
        boolean isDuplicate = false;
        for (Student student : allStudentsList) {
            // Skip the current student being edited (if editing)
            if (selectedStudent != null && student.getStudentId().equals(selectedStudent.getStudentId())) {
                continue; // Skip this student, as it's the one we're editing
            }
            // Check for duplicates
            if (student.getStudentId().equals(studentId) || student.getEmail().equals(email)) {
                isDuplicate = true;
                break;
            }
        }

        if (isDuplicate) {
            showAlert(Alert.AlertType.ERROR, "Error", "Student ID or Email is already in use by another student.");
            clearForm();
            return;
        }

        // Create or update the student object
        Student student = new Student(name, studentId, email, phoneNumber);
        student.setRegisteredCourses(new ArrayList<>(enrolledCoursesList));
        student.setRegisteredSubjects(new ArrayList<>(selectedStudent != null ? selectedStudent.getRegisteredSubjects() : new ArrayList<>()));
        student.setPassword(password.isEmpty() ? "defaultPassword" : password); // Set password (plaintext and hashed)
        student.setAddress(address); // Set address
        student.setCurrentSemester(currentSemester); // Set current semester
        student.setAcademicLevel(academicLevel); // Set academic level
        student.setThesisTitle(thesisTitle); // Set thesis title

        if (selectedStudent != null) {
            // Editing an existing student
            student.setProfilePicture(selectedStudent.getProfilePicture());
            student.setGrades(selectedStudent.getGrades());
            student.setProgress(selectedStudent.getProgress());

            // Unenroll from courses that are no longer in the list
            List<Course> oldCourses = selectedStudent.getRegisteredCourses();
            for (Course course : oldCourses) {
                if (!enrolledCoursesList.contains(course)) {
                    studentDAO.removeStudentFromCourse(selectedStudent, course);
                }
            }

            // Enroll in new courses
            for (Course course : enrolledCoursesList) {
                if (!oldCourses.contains(course)) {
                    studentDAO.enrollStudentInCourse(student, course);
                }
            }

            // Update tuition before saving
            updateStudentTuition(student);
            studentDAO.updateStudent(student);
        } else {
            // Adding a new student
            for (Course course : enrolledCoursesList) {
                studentDAO.enrollStudentInCourse(student, course);
            }
            // Update tuition before saving
            updateStudentTuition(student);
            studentDAO.addStudent(student);
        }

        loadAllStudents();
        filterStudents(studentSearch.getText());

        // Refresh availableCourses to reflect updated enrollment counts
        availableCourses.clear();
        availableCourses.addAll(courseDAO.getAllCourses());
        courseComboBox.setItems(availableCourses);
        clearForm();

        // Notify CourseAdminController to refresh its data
        if (courseAdminController != null) {
            courseAdminController.refreshCourses();
        }

        if (parentController != null) {
            parentController.refreshCoursesTab();
        }

        exporter.exportData(); // Export after saving a student
        tabPane.getSelectionModel().select(0); // Return to main page
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        selectedStudent = null;
    }

    private void clearForm() {
        nameField.clear();
        studentIdField.clear();
        emailField.clear();
        phoneNumberField.clear();
        passwordField.clear(); // Clear password
        addressField.clear(); // Clear address
        currentSemesterField.clear(); // Clear current semester
        academicLevelField.clear(); // Clear academic level
        thesisTitleField.clear(); // Clear thesis title
        enrolledCoursesList.clear();
        gradeField.clear();
    }

    // Student Details
    private void showStudentDetails(Student student) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Student Details: " + student.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Profile Picture
        ImageView profilePicture = new ImageView();
        if (student.getProfilePicture() != null) {
            profilePicture.setImage(student.getProfilePicture());
        } else {
            try {
                profilePicture.setImage(new Image("default_profile.png"));
            } catch (Exception e) {
                // Handle missing default image
                profilePicture.setImage(null);
            }
        }
        profilePicture.setFitWidth(100);
        profilePicture.setFitHeight(100);

        // Basic Info
        Label nameLabel = new Label("Name: " + student.getName());
        Label idLabel = new Label("Student ID: " + student.getStudentId());
        Label emailLabel = new Label("Email: " + student.getEmail());
        Label phoneLabel = new Label("Phone Number: " + student.getPhoneNumber());
        Label addressLabel = new Label("Address: " + (student.getAddress() != null ? student.getAddress() : "Not set"));
        Label semesterLabel = new Label("Current Semester: " + (student.getCurrentSemester() != null ? student.getCurrentSemester() : "Not set"));
        Label academicLevelLabel = new Label("Academic Level: " + (student.getAcademicLevel() != null ? student.getAcademicLevel() : "Not set"));
        Label thesisLabel = new Label("Thesis Title: " + (student.getThesisTitle() != null ? student.getThesisTitle() : "Not set"));

        // Enrolled Courses
        Label coursesLabel = new Label("Registered Courses:");
        ListView<String> coursesListView = new ListView<>();
        coursesListView.getItems().addAll(student.getRegisteredCourses().stream()
                .map(course -> course.getCourseName() + " (" + course.getCourseCode() + ")")
                .collect(Collectors.toList()));

        // Registered Subjects
        Label subjectsLabel = new Label("Registered Subjects:");
        ListView<String> subjectsListView = new ListView<>();
        subjectsListView.getItems().addAll(student.getRegisteredSubjects());

        // Academic Records (Grades)
        Label gradesLabel = new Label("Academic Records:");
        ListView<String> gradesListView = new ListView<>();
        for (Course course : student.getRegisteredCourses()) {
            Grade grade = student.getGrades().get(course.getCourseCode());
            if (grade != null) {
                gradesListView.getItems().add(course.getCourseName() + " (" + course.getCourseCode() + "): " +
                        "Final: " + grade.getFinalGrade() +
                        ", Midterm: " + grade.getMidtermGrade() +
                        ", Assignment: " + grade.getAssignmentGrade() +
                        ", Quiz: " + grade.getQuizGrade() +
                        ", Lab: " + grade.getLabGrade());
            }
        }

        // Progress and Tuition
        Label progressLabel = new Label("Progress: " + student.getProgress() + "%");
        Label tuitionLabel = new Label("Tuition: $" + student.getTuition());

        vbox.getChildren().addAll(
                profilePicture, nameLabel, idLabel, emailLabel, phoneLabel, addressLabel,
                semesterLabel, academicLevelLabel, thesisLabel,
                coursesLabel, coursesListView, subjectsLabel, subjectsListView,
                gradesLabel, gradesListView, progressLabel, tuitionLabel
        );

        Scene scene = new Scene(vbox, 400, 600);
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
}

