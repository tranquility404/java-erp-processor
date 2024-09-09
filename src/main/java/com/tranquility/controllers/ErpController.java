package com.tranquility.controllers;

import com.tranquility.controllers.endpoints.ErpEndpoints;
import com.tranquility.data.entities.User;
import com.tranquility.models.Circular;
import com.tranquility.models.Classmate;
import com.tranquility.models.Subject;
import com.tranquility.services.erpservice.ErpDataHandlerService;
import com.tranquility.services.userservice.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

//@CrossOrigin
@RestController
@RequestMapping("/erp")
public class ErpController implements ErpEndpoints {

    @Autowired
    private UserService userService;
    @Autowired
    private ErpDataHandlerService handler;

    private void initHandler() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.findByUserName(username);
        handler.initialize(user);
    }

    @GetMapping("/get-student-data")
    public ResponseEntity<Map<String, Object>> studentData() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getStudentData());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/get-attendance")
    public ResponseEntity<String> attendance() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getAttendance());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/get-subjects")
    public ResponseEntity<List<Subject>> subjects() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getSubjects());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/get-classmates")
    public ResponseEntity<List<Classmate>> classmates() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getClassmates());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/get-circulars")
    public ResponseEntity<List<Circular>> circulars() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getCirculars());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/get-academic-calendar")
    public ResponseEntity<List<Map<String, String>>> academicCalendar() {
        try {
            initHandler();
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getAcademicCalendar());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}