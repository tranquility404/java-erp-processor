package com.tranquility.controllers;

import com.tranquility.data.entities.User;
import com.tranquility.services.userservice.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@CrossOrigin
@RestController()
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> healthcheck() {
        return ResponseEntity.status(HttpStatus.OK).body("Working...");
    }

    @PostMapping("/create-user")
    public void createUser(@RequestBody User user) {
        userService.saveUser(user);
    }

    @GetMapping("/all-users")
    public ResponseEntity<?> getAllUsers() {
        List<User> all = userService.getAll();
        if (!all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
