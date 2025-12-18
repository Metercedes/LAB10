package com.example.lab10.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Method not allowed. Please use " + ex.getSupportedHttpMethods());
        return ResponseEntity.status(405).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                ));
        response.put("errors", errors);
        return ResponseEntity.badRequest().body(response);
    }
}
