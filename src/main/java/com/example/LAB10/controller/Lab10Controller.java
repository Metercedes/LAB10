package com.example.LAB10.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab10")
public class Lab10Controller {

    // 1. @RequestHeader Demo
    @GetMapping("/header-demo")
    public ResponseEntity<String> readHeader(@RequestHeader(value = "User-Agent") String userAgent) {
        return ResponseEntity.ok("You are using: " + userAgent);
    }

    // 2. @ModelAttribute (Form Data) Demo
    // Test etmek için Postman'de Body -> x-www-form-urlencoded seçin
    @PostMapping(value = "/form-demo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleForm(@ModelAttribute FormDataDto formData) {
        return ResponseEntity.ok("Received form data: " + formData.getName());
    }

    // 3. 415 Unsupported Media Type Demo
    // Bu endpoint sadece JSON kabul eder. Text gönderirseniz 415 hatası alırsınız.
    @PostMapping(value = "/json-only", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> acceptJsonOnly(@RequestBody String json) {
        return ResponseEntity.ok("JSON received: " + json);
    }

    // Basit DTO
    public static class FormDataDto {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
