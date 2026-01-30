# LAB10 - Secure Notes Application

A Spring Boot REST API application demonstrating secure web development practices including JWT authentication, authorization, input validation, and security headers.

## Prerequisites

- Java 17
- Gradle (wrapper included)

## How to Run

```bash
# Start the application
./gradlew bootRun
```

The application runs on:
- **HTTPS**: https://localhost:8443
- **HTTP**: http://localhost:8080 (redirects to HTTPS)

> Note: The application uses a self-signed SSL certificate for development. Your browser may show a security warning - this is expected for localhost testing.

## Running Tests

```bash
./gradlew test
```

Tests include unit tests and integration tests (106 total tests).

## Project Structure

```
src/main/java/com/example/LAB10/
├── config/          # Security and application configuration
├── controller/      # REST API endpoints
├── dto/             # Data Transfer Objects with validation
├── model/           # JPA entities
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, rate limiting
├── service/         # Business logic
└── validator/       # Custom validation rules
```

## Features Implemented

### Lab 10: HTTP/REST API
- RESTful endpoints using GET, POST, PUT, PATCH, DELETE
- Proper HTTP status codes (200, 201, 400, 401, 403, 404, 429)
- Content-Type handling and validation
- Request header reading

### Lab 11-12: Authentication
- JWT-based authentication (stateless)
- Access tokens (15 min expiry) and refresh tokens (24 hour expiry)
- Token refresh with rotation (old tokens invalidated)
- User registration and login
- Logout with token revocation
- BCrypt password hashing (strength 12)

### Lab 13: Security
- Input validation with custom validators
- Strong password policy enforcement
- SQL injection prevention (parameterized queries)
- Security headers (CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy)
- Rate limiting (60 req/min general, 10 req/min for auth endpoints)
- User data isolation (users can only access their own data)
- Role-based access control (USER, ADMIN roles)
- Security event logging

### Lab 14: Testing & CI/CD
- Unit tests for services and validators
- Integration tests for authentication and access control
- JaCoCo code coverage reporting
- GitHub Actions CI pipeline
- OWASP dependency vulnerability scanning

### Bonus Features
- HTTPS with SSL/TLS
- HTTP to HTTPS redirect
- HSTS (Strict-Transport-Security) header

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout and revoke tokens |

### Notes (Requires Authentication)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/notes` | Get all user's notes |
| GET | `/api/notes/{id}` | Get specific note |
| POST | `/api/notes` | Create new note |
| PUT | `/api/notes/{id}` | Update note (full) |
| PATCH | `/api/notes/{id}` | Update note (partial) |
| DELETE | `/api/notes/{id}` | Delete note |

### Admin (Requires ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/stats` | System statistics |

## Example API Usage

### Register a User
```bash
curl -k -X POST https://localhost:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"SecurePass1!"}'
```

### Login
```bash
curl -k -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"SecurePass1!"}'
```

### Create a Note (with token)
```bash
curl -k -X POST https://localhost:8443/api/notes \
  -H "Authorization: Bearer <your-access-token>" \
  -H "Content-Type: application/json" \
  -d '{"title":"My Note","content":"Note content here"}'
```

## Password Requirements

Passwords must have:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (!@#$%^&* etc.)
- Cannot be a common password (e.g., "password123")

## Security Headers

The application includes these security headers:
- `X-Frame-Options: DENY` - Prevents clickjacking
- `X-Content-Type-Options: nosniff` - Prevents MIME sniffing
- `Content-Security-Policy` - Controls resource loading
- `Referrer-Policy: strict-origin-when-cross-origin`
- `Strict-Transport-Security` - Forces HTTPS (HSTS)

## Technologies Used

- Java 17
- Spring Boot 3.2
- Spring Security
- Spring Data JPA
- JWT (jjwt library)
- SQLite database
- Flyway (database migrations)
- JaCoCo (code coverage)
- JUnit 5 & Mockito (testing)

## Configuration

Key configuration in `application.properties`:
- JWT expiration times
- Rate limiting thresholds
- SSL/HTTPS settings
- Database connection

## Author

Created for Web Security course - Labs 10, 11-12, 13, 14
