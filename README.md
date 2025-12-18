# Lab 10: Implementation of HTTP with Java/Spring Boot (Updated to Spring Boot 4.0.1)

This project is a Spring Boot 4.0.1 application that implements basic HTTP concepts using a layered architecture.

## Features

- Layered architecture: Controllers, Services, Repositories, and Models.
- SQLite database integration.
- Database migrations using Flyway.
- Environment variable configuration using `.env` files.
- Basic user registration and authentication logic.
- Simple GET endpoint.

## Prerequisites

- Java 26 or higher
- Maven (included via `./mvnw`)

## Setup

1. Clone the repository.
2. Create a `.env` file in the root directory based on `.env.example`:
   ```properties
   DB_URL=jdbc:sqlite:database.db
   DB_USERNAME=sa
   DB_PASSWORD=password
   ```
3. Build the project:
   ```bash
   ./mvnw clean compile
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Endpoints

### Hello Endpoint
- **URL:** `/hello`
- **Method:** `GET`
- **Response:** `Hello, user!`

### User Registration
- **URL:** `/api/users/register`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepassword"
  }
  ```

### User Login
- **URL:** `/api/users/login`
- **Method:** `POST`
- **Body:**
  ```json
  {
    "email": "john@example.com",
    "password": "securepassword"
  }
  ```
