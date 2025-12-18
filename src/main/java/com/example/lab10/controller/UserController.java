package com.example.lab10.controller;

import com.example.lab10.dto.CreateUserRequest;
import com.example.lab10.dto.LoginRequest;
import com.example.lab10.model.User;
import com.example.lab10.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping("/register")
    public ResponseEntity<String> registerInfo() {
        return ResponseEntity.ok("Register endpoint. Please use POST to register a new user.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (userService.authenticate(request.getEmail(), request.getPassword())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/login")
    public ResponseEntity<String> loginInfo() {
        return ResponseEntity.ok("Login endpoint. Please use POST with email and password to login.");
    }

    @GetMapping("/info")
    public ResponseEntity<List<Map<String, Object>>> getUsersInfo() {
        List<Map<String, Object>> users = userService.getAllUsers().stream()
            .map(user -> {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("email", user.getEmail());
                // Don't expose password hash
                return userInfo;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
