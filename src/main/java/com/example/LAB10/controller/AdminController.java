package com.example.LAB10.controller;

import com.example.LAB10.model.User;
import com.example.LAB10.repository.NoteRepository;
import com.example.LAB10.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("username", user.getUsername());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole());
                    return map;
                })
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalNotes", noteRepository.count());
        return ResponseEntity.ok(stats);
    }

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> changeUserRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        
        if (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Must be ROLE_USER or ROLE_ADMIN"));
        }

        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new java.util.NoSuchElementException("User not found"));
        
        user.setRole(role);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("message", "Role updated successfully", "newRole", role));
    }
}
