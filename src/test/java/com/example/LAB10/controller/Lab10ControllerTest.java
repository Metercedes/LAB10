package com.example.LAB10.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class Lab10ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void headerDemo_withUserAgent_returnsUserAgentInResponse() throws Exception {
        mockMvc.perform(get("/api/lab10/header-demo")
                        .header("User-Agent", "JUnit"))
                .andExpect(status().isOk())
                .andExpect(content().string("You are using: JUnit"));
    }

    @Test
    void jsonOnly_withTextPlain_returns415() throws Exception {
        mockMvc.perform(post("/api/lab10/json-only")
                        .contentType(Objects.requireNonNull(MediaType.TEXT_PLAIN))
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
