package com.tranquility.controllers;

import com.tranquility.controllers.endpoints.ErpEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mock")
public class MockController implements ErpEndpoints {

    public enum MockDataKeys {
        StudentData, Attendance, Subjects, Classmates, Circulars, AcademicCalendar
    }

    Map<String, Object> data = new HashMap<>();

    @Autowired
    private ErpController erpController;

    @GetMapping("/get-student-data")
    public ResponseEntity<?> studentData() {
        if (!data.containsKey(MockDataKeys.StudentData.name()))
            data.put(MockDataKeys.StudentData.name(), erpController.studentData().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.StudentData.name()));
    }

    @GetMapping("/get-attendance")
    public ResponseEntity<?> attendance() {
        if (!data.containsKey(MockDataKeys.Attendance.name()))
            data.put(MockDataKeys.Attendance.name(), erpController.attendance().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.Attendance.name()));
    }

    @GetMapping("/get-subjects")
    public ResponseEntity<?> subjects() {
        if (!data.containsKey(MockDataKeys.Subjects.name()))
            data.put(MockDataKeys.Subjects.name(), erpController.subjects().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.Subjects.name()));
    }

    @GetMapping("/get-classmates")
    public ResponseEntity<?> classmates() {
        if (!data.containsKey(MockDataKeys.Classmates.name()))
            data.put(MockDataKeys.Classmates.name(), erpController.classmates().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.Classmates.name()));
    }

    @GetMapping("/get-circulars")
    public ResponseEntity<?> circulars() {
        if (!data.containsKey(MockDataKeys.Circulars.name()))
            data.put(MockDataKeys.Circulars.name(), erpController.circulars().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.Circulars.name()));
    }

    @GetMapping("/get-academic-calendar")
    public ResponseEntity<?> academicCalendar() {
        if (!data.containsKey(MockDataKeys.AcademicCalendar.name()))
            data.put(MockDataKeys.AcademicCalendar.name(), erpController.academicCalendar().getBody());

        return ResponseEntity.ok(data.get(MockDataKeys.AcademicCalendar.name()));
    }
}
