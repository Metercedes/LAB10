package com.example.LAB10.service;

import com.example.LAB10.model.User;
import com.example.LAB10.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("plainPassword");
    }

    @Nested
    @DisplayName("register() tests")
    class RegisterTests {

        @Test
        @DisplayName("Should encode password when registering new user")
        void register_shouldEncodePassword() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.register(testUser);

            assertThat(result.getPassword()).isEqualTo("encodedPassword");
            verify(passwordEncoder).encode("plainPassword");
        }

        @Test
        @DisplayName("Should set default role to ROLE_USER")
        void register_shouldSetDefaultRole() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userService.register(testUser);

            assertThat(result.getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("Should save user to repository")
        void register_shouldSaveUser() {
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            userService.register(testUser);

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_shouldThrowExceptionForDuplicateEmail() {
            when(userRepository.existsByEmail(testUser.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.register(testUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email already exists");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("authenticate() tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should return user when credentials are valid")
        void authenticate_shouldReturnUserWithValidCredentials() {
            testUser.setPassword("encodedPassword");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

            User result = userService.authenticate("testuser", "correctPassword");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when username not found")
        void authenticate_shouldThrowExceptionForInvalidUsername() {
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.authenticate("nonexistent", "anyPassword"))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when password is incorrect")
        void authenticate_shouldThrowExceptionForIncorrectPassword() {
            testUser.setPassword("encodedPassword");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> userService.authenticate("testuser", "wrongPassword"))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username or password");
        }
    }

    @Nested
    @DisplayName("getUserById() tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found by ID")
        void getUserById_shouldReturnUserWhenFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            User result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw NoSuchElementException when user not found")
        void getUserById_shouldThrowExceptionWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw NullPointerException when ID is null")
        void getUserById_shouldThrowExceptionForNullId() {
            assertThatThrownBy(() -> userService.getUserById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
