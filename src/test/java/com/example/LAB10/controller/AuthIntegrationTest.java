package com.example.LAB10.controller;

import com.example.LAB10.dto.LoginRequest;
import com.example.LAB10.dto.RegisterRequest;
import com.example.LAB10.dto.TokenRefreshRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SuppressWarnings("null")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "SecureP@ss1";

    @Nested
    @DisplayName("Registration tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_shouldSucceedWithValidData() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should reject registration with weak password")
        void register_shouldRejectWeakPassword() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("weakuser");
            request.setEmail("weak@example.com");
            request.setPassword("weak");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject registration with invalid username")
        void register_shouldRejectInvalidUsername() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("invalid_user!");
            request.setEmail("invalid@example.com");
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject registration with invalid email")
        void register_shouldRejectInvalidEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("validuser");
            request.setEmail("not-an-email");
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject duplicate email registration")
        void register_shouldRejectDuplicateEmail() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            RegisterRequest duplicate = new RegisterRequest();
            duplicate.setUsername("anotheruser");
            duplicate.setEmail(TEST_EMAIL);
            duplicate.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicate)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login tests")
    class LoginTests {

        @BeforeEach
        void setUp() throws Exception {
            RegisterRequest request = new RegisterRequest();;
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_shouldSucceedWithValidCredentials() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(TEST_USERNAME);
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should fail login with wrong password")
        void login_shouldFailWithWrongPassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(TEST_USERNAME);
            request.setPassword("WrongP@ssw0rd");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail login with non-existent username")
        void login_shouldFailWithNonExistentUser() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("nonexistent");
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Token refresh tests")
    class TokenRefreshTests {

        private String refreshToken;

        @BeforeEach
        void setUp() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            refreshToken = response.get("refreshToken").asText();
        }

        @Test
        @DisplayName("Should refresh token successfully with valid refresh token")
        void refresh_shouldSucceedWithValidToken() throws Exception {
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken(refreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("Should fail refresh with invalid token")
        void refresh_shouldFailWithInvalidToken() throws Exception {
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken("invalid-refresh-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should rotate refresh token (old token invalidated)")
        void refresh_shouldRotateToken() throws Exception {
            TokenRefreshRequest request = new TokenRefreshRequest();
            request.setRefreshToken(refreshToken);

            MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            String newRefreshToken = response.get("refreshToken").asText();

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            TokenRefreshRequest newRequest = new TokenRefreshRequest();
            newRequest.setRefreshToken(newRefreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Logout tests")
    class LogoutTests {

        private String refreshToken;
        private String accessToken;

        @BeforeEach
        void setUp() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            refreshToken = response.get("refreshToken").asText();
            accessToken = response.get("accessToken").asText();
        }

        @Test
        @DisplayName("Should logout successfully")
        void logout_shouldSucceed() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(TEST_USERNAME);
            request.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Log out successful"));
        }

        @Test
        @DisplayName("Should invalidate refresh token after logout")
        void logout_shouldInvalidateRefreshToken() throws Exception {
            LoginRequest logoutRequest = new LoginRequest();
            logoutRequest.setUsername(TEST_USERNAME);
            logoutRequest.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isOk());

            TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
            refreshRequest.setRefreshToken(refreshToken);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Protected endpoint tests")
    class ProtectedEndpointTests {

        private String accessToken;

        @BeforeEach
        void setUp() throws Exception {
            RegisterRequest request = new RegisterRequest();
            request.setUsername(TEST_USERNAME);
            request.setEmail(TEST_EMAIL);
            request.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            accessToken = response.get("accessToken").asText();
        }

        @Test
        @DisplayName("Should access protected endpoint with valid token")
        void protectedEndpoint_shouldAllowWithValidToken() throws Exception {
            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should reject access without token")
        void protectedEndpoint_shouldRejectWithoutToken() throws Exception {
            mockMvc.perform(get("/api/notes"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject access with invalid token")
        void protectedEndpoint_shouldRejectWithInvalidToken() throws Exception {
            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should reject access with malformed Authorization header")
        void protectedEndpoint_shouldRejectMalformedHeader() throws Exception {
            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "InvalidFormat " + accessToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Public endpoint tests")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should access /hello without authentication")
        void hello_shouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/hello"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello, user!"));
        }

        @Test
        @DisplayName("Should access lab10 endpoints without authentication")
        void lab10_shouldBeAccessibleWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/lab10/header-demo")
                            .header("User-Agent", "TestAgent"))
                    .andExpect(status().isOk());
        }
    }
}
