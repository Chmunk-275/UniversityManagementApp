package com.example.universitymanagementapp.dao;

import com.example.universitymanagementapp.model.Faculty;

import java.util.List;
import java.util.ArrayList;

public class FacultyDAO {
    private static List<Faculty> facultyList = new ArrayList<>();

    // add faculty member to the list
    public void addFaculty(Faculty faculty) {
        facultyList.add(faculty);
        System.out.println("Faculty added successfully!" + faculty.getUsername());
    }

    public void deleteFaculty(Faculty faculty){
        facultyList.remove(faculty);
        System.out.println("Faculty deleted successfully!");
    }

    // updates info of existing faculty member
    public void updateFaculty(String username, Faculty updatedFaculty) {
        for (int i = 0; i < facultyList.size(); i++) {
            Faculty faculty = facultyList.get(i);
            if (faculty.getUsername().equals(username)) {
                // Update the faculty member's details
                facultyList.set(i, updatedFaculty);
                System.out.println("Faculty with username " + username + " updated successfully!");
                return;
            }
        }
        System.out.println("Faculty with username " + username + " not found for update.");
    }

    // deletes faculty memeber by id
    public void deleteFacultyById(String facultyId) {
        facultyList.removeIf(faculty -> faculty.getUsername().equals(facultyId));
        System.out.println("Faculty removed successfully!");
    }

    // finds fauclty member based on email
    public Faculty getFacultyByEmail(String email) {
        return facultyList.stream()
                .filter(faculty -> faculty.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    //finds faculty by username
    public Faculty getFacultyByUsername(String username) {
        return facultyList.stream()
                .filter(faculty -> faculty.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // Finds faculty member based on their name
    public Faculty getFacultyByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String normalizedName = name.trim().replaceAll("\\s+", " ");
        return facultyList.stream()
                .filter(faculty -> faculty.getName() != null && faculty.getName().trim().replaceAll("\\s+", " ").equalsIgnoreCase(normalizedName))
                .findFirst()
                .orElse(null);
    }

    // finds faculty member based on their ID
    public Faculty getFacultyById(String id) {
        return facultyList.stream()
                .filter(faculty -> faculty.getUsername().equals(id))
                .findFirst()
                .orElse(null);
    }

    //get logged in faculty
    public static String getLoggedInFaculty(String username) {
        for (Faculty faculty : facultyList) {
            if (faculty.getUsername().equals(username)) {
                return faculty.getName();
            }
        }
        return null;
    }

    // returns all faculty list
    public List<Faculty> getAllFaculty() {
        return facultyList;
    }

    public void clearFaculty(){
        facultyList.clear();
    }
}
