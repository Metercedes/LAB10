package com.example.lab10;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProbeTest {
    @Test
    void test() {
        // Verify Spring Boot application context loads successfully
        System.out.println("[INFO] Spring Boot context loaded successfully");
    }
}
