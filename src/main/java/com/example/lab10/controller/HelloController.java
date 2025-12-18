package com.example.lab10.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/")
    public String index() {
        return "Welcome to LAB10! Visit <a href='/hello'>/hello</a> for a greeting.";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, user!";
    }
}
