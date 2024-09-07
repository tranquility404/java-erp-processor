package com.tranquility.controllers;

import com.tranquility.data.entities.User;
import com.tranquility.services.erpservice.ErpDataHandlerService;
import com.tranquility.services.userservice.UserService;
import com.tranquility.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @Autowired
    private UserService userService;
    @Autowired
    private ErpDataHandlerService handler;

    @PostMapping("/login-me")
    public ResponseEntity<Map<String, Object>> loginCookies(@RequestBody User user) {
        try {
//            System.out.println(user);
            handler.initialize(user);
            Map<String, Object> map = new HashMap<>();

            map.put("time", Utils.getCurrentTime());
            handler.loginIfExpired();

            if (handler.isLoginCookiesSecured()) {
                userService.saveUser(user);
                map.put("message", "Login Successful");
            } else {
                map.put("message", "login failed! invalid user name or password");
            }

            return ResponseEntity.ok(map);
        } catch (IOException | InterruptedException e) {
            System.out.println("Login Failed!\n" + e);

            Map<String, Object> map = new HashMap<>();
            map.put("message", "Something went wrong");
            return new ResponseEntity<>(map, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
