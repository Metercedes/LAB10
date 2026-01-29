package com.example.LAB10.controller;

import com.example.LAB10.dto.*;
import com.example.LAB10.model.RefreshToken;
import com.example.LAB10.model.User;
import com.example.LAB10.security.JwtService;
import com.example.LAB10.security.CustomUserDetailsService;
import com.example.LAB10.service.RefreshTokenService;
import com.example.LAB10.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("New user registration attempt: {}", request.getUsername());
        userService.register(mapToUser(request));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            throw e;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getUsername());

        log.info("User logged in successfully: {}", request.getUsername());
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(newRefreshToken.getUser().getUsername());
        String newAccessToken = jwtService.generateToken(userDetails);

        log.info("Token refreshed for user: {}", userDetails.getUsername());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LoginRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        refreshTokenService.deleteByUserId(((com.example.LAB10.model.User) userDetailsService.loadUserByUsername(request.getUsername())).getId());
        log.info("User logged out: {}", request.getUsername());
        return ResponseEntity.ok("Log out successful");
    }

    private User mapToUser(RegisterRequest req) {
        User u = new User(); u.setUsername(req.getUsername()); u.setEmail(req.getEmail()); u.setPassword(req.getPassword());
        return u;
    }
}