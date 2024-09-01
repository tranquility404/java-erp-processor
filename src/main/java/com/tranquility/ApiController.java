package com.tranquility;

import com.tranquility.core.ErpDataHandler;
import com.tranquility.helpers.Utils;
import com.tranquility.model.Circular;
import com.tranquility.model.Classmate;
import com.tranquility.model.Subject;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ApiController {

    @Autowired
    private ErpDataHandler handler;

    private void initSession(Map<String, Object> body, HttpSession session) throws IOException, InterruptedException {
        Map<String, String> loginDetails = (Map<String, String>) body.get("loginDetails");
        String username = loginDetails.get("username");
        String password = loginDetails.get("password");

        session.setAttribute("username", username);
        session.setAttribute("password", password);

        handler.initialize(username, password);
//        handler.getSessionCookies();
    }

    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ResponseEntity.ok("Hello there! Welcome to my backend");
    }

    @PostMapping("/session")
    public ResponseEntity<String> session(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            initSession(body, session);
            return ResponseEntity.ok("Session Created");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login-me")
    public ResponseEntity<Map<String, Object>> loginCookies(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            initSession(body, session);
            Map<String, Object> map = new HashMap<>();

            map.put("time", Utils.getCurrentTime());
            handler.loginIfExpired();

            if (handler.isLoginCookiesSecured()) {
                map.put("message", "Login Successful");
            } else {
                map.put("message", "login failed! invalid user name or password");
            }

            return ResponseEntity.ok(map);
        } catch (IOException | InterruptedException e) {
            System.out.println("Login Failed!");

            Map<String, Object> map = new HashMap<>();
            map.put("message", "Something went wrong");
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping("/student-data")
    public ResponseEntity<Map<String, Object>> studentData(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getStudentData());
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/attendance")
    public ResponseEntity<String> attendance(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.badRequest().build();

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getAttendance());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> subjects(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.badRequest().build();

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getSubjects());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/classmates")
    public ResponseEntity<List<Classmate>> classmates(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.badRequest().build();

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getClassmates());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/circulars")
    public ResponseEntity<List<Circular>> circulars(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.badRequest().build();

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getCirculars());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/academic-calendar")
    public ResponseEntity<List<Map<String, String>>> academicCalendar(HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.badRequest().build();

        try {
            handler.loginIfExpired();
            return ResponseEntity.ok(handler.getAcademicCalendar());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}