package com.example.LAB10.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Lab10ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHeaderDemo() throws Exception {
        mockMvc.perform(get("/api/lab10/header-demo")
                .header("User-Agent", "JUnit Test"))
                .andExpect(status().isOk())
                .andExpect(content().string("You are using: JUnit Test"));
    }

    @Test
    public void testUnsupportedMediaType() throws Exception {
        // JSON bekleyen yere TEXT atıyoruz -> 415 bekliyoruz
        mockMvc.perform(post("/api/lab10/json-only")
                .content("Plain Text")
                .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isUnsupportedMediaType());
    }
}
