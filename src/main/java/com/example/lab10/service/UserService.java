package com.example.lab10.service;

import com.example.lab10.dto.CreateUserRequest;
import com.example.lab10.model.User;
import com.example.lab10.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        return userRepository.save(user);
    }

    public boolean authenticate(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
