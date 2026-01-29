# LAB 10 & 14: Secure Spring Boot Application

This project implements a secure REST API demonstrating Spring Security, JWT Authentication, and various Spring MVC features requested in Lab 10 and Lab 14.

## Endpoint Documentation (Lab 10 Requirement)

| Method | Endpoint | Description | Status Codes |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/lab10/header-demo` | Reads `User-Agent` header | 200, 400 |
| `POST` | `/api/lab10/form-demo` | Accepts `x-www-form-urlencoded` | 200, 415 |
| `POST` | `/api/lab10/json-only` | Accepts JSON only (Demonstrates 415) | 200, 415 |
| `POST` | `/api/auth/register` | Registers a new user with validation | 200, 400 |
| `POST` | `/api/auth/login` | Authenticates user (JWT) | 200, 401 |
| `POST` | `/api/auth/refresh` | Rotates refresh token | 200, 403 |

## Features Implemented

* **Security Headers:** CSP, X-Frame-Options (DENY), XSS Protection enabled.
* **Password Hashing:** BCrypt with strength 12.
* **Validation:** Strict DTO validation (`@NotNull`, `@Email`, Custom `@UsernameRule`).
* **Error Handling:** No stack traces leaked. Structured JSON responses.
* **Lab 10 Demos:** Dedicated controller for Headers, Form Data, and Media Types.
* **Testing:** Unit and Integration tests included.

## Setup & Run

1.  **Environment Variables:**
    Set `DB_URL` in your IDE run configuration or rely on the default SQLite file.
    *(Do not commit `.env` files)*.

2.  **Run:**
    ```bash
    ./gradlew bootRun
    ```

3.  **Test Dashboard:**
    Visit `http://localhost:8080/test-dashboard`