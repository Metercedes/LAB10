package com.example.LAB10.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/test-dashboard")
    public String showDashboard() {

        return "test-dashboard";
    }
}