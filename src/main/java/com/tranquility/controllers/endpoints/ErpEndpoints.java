package com.tranquility.controllers.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface ErpEndpoints {

    public ResponseEntity<?> studentData();
    public ResponseEntity<?> attendance();
    public ResponseEntity<?> subjects();
    public ResponseEntity<?> classmates();
    public ResponseEntity<?> circulars();
    public ResponseEntity<?> academicCalendar();
}
