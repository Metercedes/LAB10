package com.example.LAB10.service;

import com.example.LAB10.model.RefreshToken;
import com.example.LAB10.model.User;
import com.example.LAB10.repository.RefreshTokenRepository;
import com.example.LAB10.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken());

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String token) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new NoSuchElementException("Invalid refresh token"));

        verifyExpiration(oldToken);

        String username = oldToken.getUser().getUsername();

        return createRefreshToken(username);
    }
}