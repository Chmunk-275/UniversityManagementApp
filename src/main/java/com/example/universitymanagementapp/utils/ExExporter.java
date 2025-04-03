package com.example.universitymanagementapp.utils;

import com.example.universitymanagementapp.dao.*;
import com.example.universitymanagementapp.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ExExporter {
    private static final String FILE_PATH = "src/main/resources/UMS_Data1.xlsx";
    private CourseDAO courseDAO;
    private StudentDAO studentDAO;
    private FacultyDAO facultyDAO;
    private SubjectDAO subjectDAO;
    private EventDAO eventDAO;

    private static List<Activity> recentActivities = new ArrayList<>();
    private static List<Registration> recentRegistrations = new ArrayList<>();
    private static List<Notification> recentNotifications = new ArrayList<>();

    private static LocalDateTime lastExportTime;

    public ExExporter(CourseDAO courseDAO, StudentDAO studentDAO, FacultyDAO facultyDAO,
                      SubjectDAO subjectDAO, EventDAO eventDAO) {
        this.courseDAO = courseDAO;
        this.studentDAO = studentDAO;
        this.facultyDAO = facultyDAO;
        this.subjectDAO = subjectDAO;
        this.eventDAO = eventDAO;
    }

    public void exportData() {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create sheets
            writeCourses(workbook.createSheet("Courses"));
            writeStudents(workbook.createSheet("Students"));
            writeFaculty(workbook.createSheet("Faculties"));
            writeSubjects(workbook.createSheet("Subjects"));
            writeEvents(workbook.createSheet("Events"));

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH)) {
                workbook.write(fileOut);
                System.out.println("✅ Data successfully exported to Excel!");
                lastExportTime = LocalDateTime.now();
            }
        } catch (IOException e) {
            System.err.println("Error writing to Excel file: " + e.getMessage());
        }
        recordChanges();
    }

    // Getters for the static lists
    public static List<Activity> getRecentActivities() {
        return recentActivities;
    }

    public static List<Registration> getRecentRegistrations() {
        return recentRegistrations;
    }

    public static List<Notification> getRecentNotifications() {
        return recentNotifications;
    }

    // Record methods for adding to the static lists
    public static void recordActivity(String type, String description) {
        Activity activity = new Activity(type, description, LocalDateTime.now());
        recentActivities.add(activity);
        // Keep only the most recent 50
        if (recentActivities.size() > 50) {
            recentActivities.remove(0);
        }
    }

    public static void recordRegistration(String studentId, String courseName, int courseCode) {
        Registration registration = new Registration(studentId, courseName, courseCode, LocalDateTime.now());
        recentRegistrations.add(registration);
        // Keep only the most recent 50
        if (recentRegistrations.size() > 50) {
            recentRegistrations.remove(0);
        }
    }

    // Record Notification
    public static void recordNotification(String type, String message) {
        Notification notification = new Notification(type, message, LocalDateTime.now());
        recentNotifications.add(notification);
        // Keep only the most recent 50
        if (recentNotifications.size() > 50) {
            recentNotifications.remove(0);
        }
    }

    public static LocalDateTime getLastExportTime() {
        return lastExportTime;
    }

    public static void setLastExportTime(LocalDateTime lastExportTime) {
        ExExporter.lastExportTime = lastExportTime;
    }

    private void recordChanges() {
        // Currently does nothing, but you can log or track changes here if you wish
        List<Student> currentStudents = studentDAO.getAllStudents();
        List<Course> currentCourses = courseDAO.getAllCourses();
        List<Faculty> currentFaculty = facultyDAO.getAllFaculty();
        List<Subject> currentSubjects = subjectDAO.getAllSubjects();
        List<Event> currentEvents = eventDAO.getAllEvents();
    }

    public static void updateStudentTuition(Student student) {
        if (student == null) {
            return;
        }
        int numberOfCourses = student.getRegisteredCourses() != null ? student.getRegisteredCourses().size() : 0;
        int newTuition = numberOfCourses * 250; // $250 per course
        int oldTuition = student.getTuition(); // Get the current tuition

        // Only update and record a notification if the tuition has changed
        if (newTuition != oldTuition) {
            student.setTuition(newTuition);
            recordNotification("Student Tuition", "Tuition for " + student.getName() + " (" + student.getStudentId() + ") updated to $" + newTuition + " (" + numberOfCourses + " courses)");
            System.out.println("Updated tuition for student " + student.getStudentId() + ": $" + newTuition + " (" + numberOfCourses + " courses)");
        }
    }

    private void writeCourses(Sheet sheet) {
        List<Course> courses = courseDAO.getAllCourses();
        createHeader(sheet, new String[]{"Course Code", "Course Name", "Subject Code", "Section ID",
                "Capacity", "Meeting Days/Time", "Final Exam Date/Time",
                "Location", "Instructor"});
        int rowNum = 1;
        for (Course course : courses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(course.getCourseCode());
            row.createCell(1).setCellValue(course.getCourseName());
            row.createCell(2).setCellValue(course.getSubjectCode());
            row.createCell(3).setCellValue(course.getSectionID());
            row.createCell(4).setCellValue(course.getCapacity());
            row.createCell(5).setCellValue(course.getMeetingDaysTime());
            row.createCell(6).setCellValue(course.getFinalExamDateTime());
            row.createCell(7).setCellValue(course.getMeetingLocation());

            // Look up the faculty name based on the instructor (faculty ID)
            String instructorId = course.getInstructor();
            String instructorName = course.getInstructor();
            if (instructorId != null && !instructorId.equals("Unassigned")) {
                Faculty faculty = facultyDAO.getFacultyById(instructorId);
                if (faculty != null && faculty.getName() != null) {
                    instructorName = faculty.getName();
                } else {
                    System.out.println("Warning: No faculty found with ID: " + instructorId + " for course: " + course.getCourseCode());
                }
            }
            row.createCell(8).setCellValue(instructorName);
        }
        recordChanges();
    }

    private void writeStudents(Sheet sheet) {
        List<Student> students = studentDAO.getAllStudents();
        createHeader(sheet, new String[]{"Student ID", "Name", "Address", "Phone", "Email",
                "Academic Level", "Current Semester", "Profile Picture",
                "Registered Subjects", "Thesis Title", "Progress", "Password"});
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getStudentId());
            row.createCell(1).setCellValue(student.getName());
            row.createCell(2).setCellValue(student.getAddress());
            row.createCell(3).setCellValue(student.getPhoneNumber());
            row.createCell(4).setCellValue(student.getEmail());
            row.createCell(5).setCellValue(student.getAcademicLevel());
            row.createCell(6).setCellValue(student.getCurrentSemester());
            // Write the actual profile picture path instead of "custom" or "default"
            row.createCell(7).setCellValue(student.getProfilePicturePath() != null ? student.getProfilePicturePath() : "default");
            row.createCell(8).setCellValue(String.join(", ", student.getRegisteredSubjects()));
            row.createCell(9).setCellValue(student.getThesisTitle());
            row.createCell(10).setCellValue(student.getProgress()); // Convert back to decimal
            row.createCell(11).setCellValue(student.getPlaintextPassword() + "%");
        }
        recordChanges();
    }

    private void writeFaculty(Sheet sheet) {
        List<Faculty> faculty = facultyDAO.getAllFaculty();
        createHeader(sheet, new String[]{"Username", "Name", "Degree", "Research Interest",
                "Email", "Office Location", "Courses Offered", "Password"});
        int rowNum = 1;
        for (Faculty f : faculty) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(f.getUsername());
            row.createCell(1).setCellValue(f.getName());
            row.createCell(2).setCellValue(f.getDegree());
            row.createCell(3).setCellValue(f.getResearchInterest());
            row.createCell(4).setCellValue(f.getEmail());
            row.createCell(5).setCellValue(f.getOfficeLocation());
            row.createCell(6).setCellValue(String.join(", ", f.getCoursesOffered()));
            row.createCell(7).setCellValue(f.getPlaintextPassword());
            row.createCell(8).setCellValue(f.getProfilePicturePath() != null ? f.getProfilePicturePath() : "default");
        }
        recordChanges();
    }

    private void writeSubjects(Sheet sheet) {
        List<Subject> subjects = subjectDAO.getAllSubjects();
        createHeader(sheet, new String[]{"Subject Code", "Subject Name"});
        int rowNum = 1;
        for (Subject subject : subjects) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(subject.getSubjectCode());
            row.createCell(1).setCellValue(subject.getSubjectName());
        }
        recordChanges();
    }

    private void writeEvents(Sheet sheet) {
        List<Event> events = eventDAO.getAllEvents();
        createHeader(sheet, new String[]{"Event Code", "Name", "Description", "Location",
                "Date and Time", "Capacity", "Cost", "Header Picture",
                "Registered Students"});
        int rowNum = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        for (Event event : events) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(event.getEventCode());
            row.createCell(1).setCellValue(event.getEventName());
            row.createCell(2).setCellValue(event.getEventDescription());
            row.createCell(3).setCellValue(event.getEventLocation());
            row.createCell(4).setCellValue(event.getEventDateTime() != null ? sdf.format(event.getEventDateTime()) : "");
            row.createCell(5).setCellValue(event.getEventCapacity());
            row.createCell(6).setCellValue(event.getEventCost());
            // Write the actual header image path instead of "custom" or "default"
            row.createCell(7).setCellValue(event.getHeaderImagePath() != null ? event.getHeaderImagePath() : "default");

            // Map student IDs to names for the "Registered Students" column
            List<String> registeredStudents = event.getRegisteredStudents();
            List<String> studentNames = new ArrayList<>();
            if (registeredStudents != null && !registeredStudents.isEmpty()) {
                for (String studentId : registeredStudents) {
                    if (studentId == null || studentId.trim().isEmpty()) {
                        continue; // Skip invalid student IDs
                    }
                    Student student = studentDAO.getStudentById(studentId);
                    if (student != null && student.getName() != null && !student.getName().trim().isEmpty()) {
                        studentNames.add(student.getName());
                    } else {
                        System.out.println("Warning: No student found with ID: " + studentId + " for event: " + event.getEventCode());
                        studentNames.add(studentId); // Fallback to ID if name not found
                    }
                }
            }
            // Join the student names into a comma-separated string
            String studentNamesString = String.join(", ", studentNames);
            row.createCell(8).setCellValue(studentNamesString);
        }
        recordChanges();
    }



    private void createHeader(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
    }
}