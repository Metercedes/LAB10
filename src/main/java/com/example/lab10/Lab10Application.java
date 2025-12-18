package com.example.lab10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Lab10Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab10Application.class, args);
    }

}
