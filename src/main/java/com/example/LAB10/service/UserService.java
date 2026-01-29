package com.example.LAB10.service;

import com.example.LAB10.model.User;
import com.example.LAB10.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public User register(User user) {
        if (repo.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return repo.save(user);
    }

    public User getUserById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + id));
    }
}