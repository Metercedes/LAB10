package com.example.LAB10.controller;

import com.example.LAB10.dto.NoteDto;
import com.example.LAB10.dto.RegisterRequest;
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
class AccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUp() throws Exception {
        RegisterRequest user1 = new RegisterRequest();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("SecureP@ss1");

        MvcResult result1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andReturn();
        JsonNode response1 = objectMapper.readTree(result1.getResponse().getContentAsString());
        user1Token = response1.get("accessToken").asText();

        RegisterRequest user2 = new RegisterRequest();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("SecureP@ss2");

        MvcResult result2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andReturn();
        JsonNode response2 = objectMapper.readTree(result2.getResponse().getContentAsString());
        user2Token = response2.get("accessToken").asText();
    }

    @Nested
    @DisplayName("Notes access control tests")
    class NotesAccessControlTests {

        @Test
        @DisplayName("User should only see their own notes")
        void getMyNotes_shouldOnlyReturnOwnNotes() throws Exception {
            NoteDto note1 = new NoteDto();
            note1.setTitle("User1 Note");
            note1.setContent("User1 Content");

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note1)))
                    .andExpect(status().isOk());

            NoteDto note2 = new NoteDto();
            note2.setTitle("User2 Note");
            note2.setContent("User2 Content");

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user2Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note2)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("User1 Note"))
                    .andExpect(jsonPath("$.length()").value(1));

            mockMvc.perform(get("/api/notes")
                            .header("Authorization", "Bearer " + user2Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value("User2 Note"))
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("User cannot access another user's note by ID")
        void getNoteById_shouldDenyAccessToOtherUsersNote() throws Exception {
            NoteDto note = new NoteDto();
            note.setTitle("Private Note");
            note.setContent("Private Content");

            MvcResult result = mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            Long noteId = response.get("id").asLong();

            mockMvc.perform(get("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user2Token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User cannot update another user's note")
        void updateNote_shouldDenyUpdateToOtherUsersNote() throws Exception {
            NoteDto note = new NoteDto();
            note.setTitle("Original Title");
            note.setContent("Original Content");

            MvcResult result = mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            Long noteId = response.get("id").asLong();

            NoteDto updateDto = new NoteDto();
            updateDto.setTitle("Hacked Title");
            updateDto.setContent("Hacked Content");

            mockMvc.perform(put("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user2Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("User cannot delete another user's note")
        void deleteNote_shouldDenyDeleteToOtherUsersNote() throws Exception {
            NoteDto note = new NoteDto();
            note.setTitle("Note to Delete");
            note.setContent("Content");

            MvcResult result = mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note)))
                    .andReturn();

            JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
            Long noteId = response.get("id").asLong();

            mockMvc.perform(delete("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user2Token))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Note to Delete"));
        }

        @Test
        @DisplayName("User can CRUD their own notes successfully")
        void crud_shouldWorkForOwnNotes() throws Exception {
            NoteDto note = new NoteDto();
            note.setTitle("My Note");
            note.setContent("My Content");

            MvcResult createResult = mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
            Long noteId = response.get("id").asLong();

            mockMvc.perform(get("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("My Note"));

            NoteDto updateDto = new NoteDto();
            updateDto.setTitle("Updated Note");
            updateDto.setContent("Updated Content");

            mockMvc.perform(put("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated Note"));

            mockMvc.perform(delete("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/notes/" + noteId)
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Admin access control tests")
    class AdminAccessControlTests {

        @Test
        @DisplayName("Regular user cannot access admin endpoints")
        void adminEndpoint_shouldDenyAccessToRegularUser() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + user1Token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin endpoints require authentication")
        void adminEndpoint_shouldRequireAuth() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("CSRF behavior tests (API - CSRF disabled by design)")
    class CsrfBehaviorTests {

        @Test
        @DisplayName("API should work without CSRF token (stateless JWT)")
        void api_shouldWorkWithoutCsrfToken() throws Exception {
            NoteDto note = new NoteDto();
            note.setTitle("Test Note");
            note.setContent("Test Content");

            mockMvc.perform(post("/api/notes")
                            .header("Authorization", "Bearer " + user1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(note)))
                    .andExpect(status().isOk());
        }
    }
}
