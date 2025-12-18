# Lab 10: Implementation of HTTP with Java/Spring Boot

This project is a Spring Boot application implementing basic HTTP concepts using a layered architecture.

## Prerequisites

- Java 21 or higher
- Maven (included via `./mvnw`)

## Setup

1. Clone the repository
2. Create a `.env` file in the root directory:

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
./mvnw spring-boot:run -Dspring.devtools.restart.enabled=false
```

The application will start on `http://localhost:8080`

## Running the Server

**Start the server:**

```bash
./mvnw spring-boot:run -Dspring.devtools.restart.enabled=false
```

**Stop the server:**

```bash
lsof -ti:8080 | xargs kill -9
```

## Project Structure

The project follows a layered architecture:

- **Controller**: Handles HTTP requests and responses
- **Service**: Contains business logic
- **Repository**: Manages data persistence
- **Model**: Domain entities
- **DTO**: Data Transfer Objects for request validation
- **Config**: Configuration classes

## Endpoints

### Home

- **URL**: `/`
- **Method**: `GET`
- **Response**: Welcome HTML page

### Hello

- **URL**: `/hello`
- **Method**: `GET`
- **Response**: `Hello, user!`

### User Registration

- **URL**: `/api/users/register`
- **Method**: `POST`
- **Request Body**:

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword"
}
```

- **Response** (201 Created):

```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "password": "$2a$10$..."
}
```

### User Login

- **URL**: `/api/users/login`
- **Method**: `POST`
- **Request Body**:

```json
{
  "email": "john@example.com",
  "password": "securepassword"
}
```

- **Response** (200 OK):

```json
{
  "message": "Login successful"
}
```

### Get All Users

- **URL**: `/api/users/info`
- **Method**: `GET`
- **Response** (200 OK):

```json
[
  {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
]
```

Note: Passwords are NOT returned for security

## Technologies Used

- Spring Boot 4.0.1
- Spring Web
- Spring Security (BCrypt password encoding)
- Spring Data JPA
- Hibernate 7.2.0.Final
- Flyway (Database Migration)
- SQLite Database
- Jakarta Validation

## Testing the API

**Register a user:**

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"password123"}'
```

**Login:**

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'
```

**Get all users:**

```bash
curl http://localhost:8080/api/users/info
```

**Say hello:**

```bash
curl http://localhost:8080/hello
```

## Database

- **Type**: SQLite 3.51.1.0
- **File**: `database.db`
- **Schema**: Automatically created by Flyway migration on startup
- **Connection**: Local file-based (no external database needed)
