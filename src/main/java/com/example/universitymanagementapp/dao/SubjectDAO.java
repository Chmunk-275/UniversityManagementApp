package com.example.universitymanagementapp.dao;

import java.util.ArrayList;
import java.util.List;
import com.example.universitymanagementapp.model.Subject;

public class SubjectDAO {
    private static List<Subject> subjects = new ArrayList<>();

    //add subject
    public void addSubject(Subject subject){
        subjects.add(subject);
    }

    //remove subject
    public void removeSubject(String subjectName){
        subjects.removeIf(subject -> subject.getSubjectName().equalsIgnoreCase(subjectName));
    }

    //edit/update subject
    public void updateSubject(String originalSubjectCode, Subject updatedSubject) {
        for (Subject subject : subjects) {
            if (subject.getSubjectCode().equalsIgnoreCase(originalSubjectCode)) {
                // Update the subject's fields
                subject.setSubjectName(updatedSubject.getSubjectName());
                subject.setSubjectCode(updatedSubject.getSubjectCode());
                break; // Exit the loop once the subject is updated
            }
        }
    }

    //search subject by name
    public Subject getSubjectByName(String subjectName){
        for (Subject subject : subjects){
            if (subject.getSubjectName().equalsIgnoreCase(subjectName)){
                return subject;
            }
        }
        return null;
    }

    //get all subjects
    public List<Subject> getAllSubjects(){
        return subjects;
    }


    public void clearSubjects(){
        subjects.clear();
    }


}
