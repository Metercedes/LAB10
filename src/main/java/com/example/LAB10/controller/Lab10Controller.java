package com.example.LAB10.controller;

import com.example.LAB10.dto.FormDataDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab10")
public class Lab10Controller {

    @GetMapping("/header-demo")
    public String headerDemo(@RequestHeader("User-Agent") String userAgent) {
        return "You are using: " + userAgent;
    }

    @PostMapping(value = "/form-demo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String formDemo(@ModelAttribute FormDataDto formData) {
        return "Received form data: " + formData.getName();
    }

    @PostMapping(value = "/json-only", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String jsonOnly(@RequestBody String body) {
        return "JSON received: " + body;
    }

    @RequestMapping(value = "/options-demo", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsDemo() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ALLOW, "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD")
                .build();
    }

    @RequestMapping(value = "/head-demo", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headDemo() {
        return ResponseEntity.ok()
                .header("X-Custom-Header", "HeadRequestSupported")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
