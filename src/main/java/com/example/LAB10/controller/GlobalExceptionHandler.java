package com.example.LAB10.controller;

import com.example.LAB10.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        
        log.warn("SECURITY: Authentication failed | IP: {} | URI: {} | Reason: Invalid credentials", 
                clientIp, requestUri);
        
        return ResponseEntity.status(401)
                .body(new ErrorResponse("Authentication Failed", "Invalid username or password"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        String username = getCurrentUsername();
        
        log.warn("SECURITY: Access denied | IP: {} | URI: {} | User: {} | Reason: Insufficient permissions", 
                clientIp, requestUri, username);
        
        return ResponseEntity.status(403)
                .body(new ErrorResponse("Access Denied", "You do not have permission"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        

        log.warn("SECURITY: Unsupported media type | IP: {} | URI: {} | Content-Type: {}", 
                clientIp, requestUri, ex.getContentType());
        
        return ResponseEntity.status(415)
                .body(new ErrorResponse("Unsupported Media Type", "Content type is not supported"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        int errorCount = ex.getBindingResult().getErrorCount();
        

        log.warn("SECURITY: Validation failed | IP: {} | URI: {} | Error count: {}", 
                clientIp, requestUri, errorCount);
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(400).body(errors);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        
        log.info("Resource not found | IP: {} | URI: {}", clientIp, requestUri);
        
        return ResponseEntity.status(404)
                .body(new ErrorResponse("Resource Not Found", "The requested resource was not found"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        
        log.warn("SECURITY: Invalid request | IP: {} | URI: {} | Reason: {}", 
                clientIp, requestUri, ex.getMessage());
        
        return ResponseEntity.status(400)
                .body(new ErrorResponse("Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        String clientIp = getClientIp();
        String requestUri = getRequestUri();
        
        log.error("SECURITY: Unexpected error | IP: {} | URI: {} | Type: {}",
                clientIp, requestUri, ex.getClass().getSimpleName(), ex);
        
        Map<String, String> response = new HashMap<>();
        response.put("title", "Server Error");
        response.put("message", "An unexpected error occurred.");
        return ResponseEntity.status(500).body(response);
    }

    private String getClientIp() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) return "unknown";
            
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getRequestUri() {
        try {
            HttpServletRequest request = getCurrentRequest();
            return request != null ? request.getRequestURI() : "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String getCurrentUsername() {
        try {
            var auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            return auth != null ? auth.getName() : "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}