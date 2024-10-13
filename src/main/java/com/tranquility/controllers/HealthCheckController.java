package com.tranquility.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping
    public ResponseEntity<?> healthcheck() {
        return ResponseEntity.status(HttpStatus.OK).body("Working...");
    }
}
