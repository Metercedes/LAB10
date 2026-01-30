# LAB10 - Presentation Q&A Guide

This document provides detailed answers to all checklist questions for the live demo presentation.

---

## 1. Application Readiness (5 pts)

### Q: Does the application start without errors?
**A:** Yes. Run with:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
./gradlew bootRun
```
- Starts on **https://localhost:8443**
- HTTP (port 8080) auto-redirects to HTTPS

### Q: Can you restart the app quickly?
**A:** Yes. Press `Ctrl+C` to stop, then run `./gradlew bootRun` again. Takes ~5 seconds.

### Q: How to access the demo dashboard?
**A:** Open: **https://localhost:8443/test-dashboard.html**

---

## 2. Authentication - Registration and Login (15 pts)

### Q: Where does user registration happen?
**A:** 
- **Endpoint:** `POST /api/auth/register`
- **Controller:** `src/main/java/com/example/LAB10/controller/AuthController.java` (Lines 28-40)
- **Service:** `src/main/java/com/example/LAB10/service/UserService.java` (Lines 18-25)

### Q: How to show validation errors during registration?
**A:** In test-dashboard.html, click:
- **"Register with Bad Password"** → Shows password policy violations (HTTP 400)
- **"Register with Bad Email"** → Shows email validation error (HTTP 400)

### Q: Where are passwords hashed (bcrypt)?
**A:** 
- **File:** `src/main/java/com/example/LAB10/service/UserService.java`
- **Line 22:** `user.setPassword(passwordEncoder.encode(user.getPassword()));`
- **PasswordEncoder bean:** `src/main/java/com/example/LAB10/config/ApplicationConfig.java` (Lines 33-36)
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

### Q: How to prove passwords are hashed?
**A:** Check the database after registration:
```bash
sqlite3 build/lab10.db "SELECT username, password FROM users LIMIT 1;"
```
Output shows: `user1|$2a$12$...` (BCrypt hash starting with `$2a$12$`)

### Q: How to show a failed login attempt?
**A:** In test-dashboard.html:
1. Enter wrong password (e.g., "wrongpassword")
2. Click "Login"
3. Console shows: "Login: Failed (401) - Bad credentials"

### Q: How to show a successful login?
**A:** In test-dashboard.html:
1. First register a user (or use existing)
2. Enter correct credentials
3. Click "Login"
4. Status changes to green "Logged In"
5. Token appears (blurred - click to reveal)

### Q: Where is the JWT returned?
**A:** 
- **Response body** contains `accessToken` and `refreshToken`
- **File:** `src/main/java/com/example/LAB10/controller/AuthController.java` (Line 44-57)
- Dashboard shows token in "Token" section

### Q: Are login error messages safe?
**A:** Yes. Error response is generic:
```json
{"error": "Bad credentials"}
```
- Does NOT reveal if username exists or password is wrong
- **File:** `src/main/java/com/example/LAB10/exception/GlobalExceptionHandler.java` (Line 41-49)

---

## 3. Authorization and Access Control (20 pts)

### Q: How is access without login denied?
**A:** 
- Click **"Access Notes Without Login"** in dashboard
- Returns: **401 Unauthorized**
- **Enforced by:** `JwtAuthenticationFilter` (`src/main/java/com/example/LAB10/security/JwtAuthenticationFilter.java`)
- **Config:** `src/main/java/com/example/LAB10/config/SecurityConfig.java` (Line 70-74)

### Q: How is access with wrong role denied?
**A:** 
- Login as regular user
- Click **"Get All Users (ADMIN only)"** or **"Get Stats (ADMIN only)"**
- Returns: **403 Forbidden**
- **Enforced by:** `@PreAuthorize("hasRole('ADMIN')")` in `AdminController.java` (Line 25, 41, 50)

### Q: Which class enforces route protection?
**A:**
1. **SecurityConfig.java** (Line 70-74) - URL-based security rules:
```java
.requestMatchers("/api/auth/**").permitAll()
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.anyRequest().authenticated()
```
2. **JwtAuthenticationFilter.java** - Validates JWT tokens
3. **Controller annotations** - `@PreAuthorize` for method-level security

### Q: How to demonstrate User Data Isolation?
**A:** This is **CRITICAL** for the demo. Steps:
1. Register and login as **user1**, create a note
2. Note the Note ID from console (e.g., Note ID: 1)
3. Logout
4. Register and login as **user2**
5. Enter Note ID "1" in "Security Test" section
6. Click **"Try Access Note"** → 403 Forbidden
7. Click **"Try Delete Note"** → 403 Forbidden
8. Click **"Try Update Note"** → 403 Forbidden

### Q: Where is user_id enforced in code?
**A:** 
- **File:** `src/main/java/com/example/LAB10/service/NoteService.java`
- **Lines 49-51, 63-65, 78-80:**
```java
if (!note.getUser().getId().equals(getCurrentUser().getId())) {
    throw new AccessDeniedException("You do not have permission...");
}
```

### Q: Where is the user_id foreign key in database?
**A:** 
- **File:** `src/main/resources/db/migration/V2__Create_notes_table_and_alter_users.sql`
- **Lines 1-8:**
```sql
CREATE TABLE IF NOT EXISTS notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 4. Input Validation and Error Handling (10 pts)

### Q: Where are DTO validation annotations?
**A:**
1. **RegisterRequest.java** (`src/main/java/com/example/LAB10/dto/RegisterRequest.java`):
```java
@NotBlank(message = "Username is required")
@Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
@UsernameRule  // Custom validator
private String username;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
private String email;

@NotBlank(message = "Password is required")
@StrongPassword  // Custom validator
private String password;
```

2. **NoteDto.java** (`src/main/java/com/example/LAB10/dto/NoteDto.java`):
```java
@NotBlank(message = "Title required")
private String title;
```

### Q: What custom validation rules exist?
**A:**
1. **@StrongPassword** - `src/main/java/com/example/LAB10/validator/`
   - `StrongPassword.java` (annotation)
   - `PasswordPolicyValidator.java` (logic)
   - Checks: 8+ chars, uppercase, lowercase, digit, special char, not common password

2. **@UsernameRule** - `src/main/java/com/example/LAB10/validator/`
   - `UsernameRule.java` (annotation)
   - `UsernameValidator.java` (logic)
   - Checks: alphanumeric only, no reserved words

### Q: How to demonstrate custom validation?
**A:** Click **"Register with Bad Password"** in dashboard:
- Input: password="abc"
- Response shows all violations (HTTP 400):
  - "Password must be at least 8 characters"
  - "Password must contain at least one uppercase letter"
  - etc.

### Q: Do error responses show stack traces?
**A:** No. All errors return structured JSON:
```json
{
  "timestamp": "2026-01-30T...",
  "status": 400,
  "error": "Validation failed",
  "errors": ["Password must be at least 8 characters", ...]
}
```
- **Handled by:** `GlobalExceptionHandler.java` (all methods)

---

## 5. HTTP and Browser Security Headers (8 pts)

### Q: How to view security headers?
**A:** 
1. Open **https://localhost:8443/test-dashboard.html**
2. Press **F12** (DevTools) → **Network** tab
3. Click any request (e.g., to /api/notes)
4. Click **Headers** tab → scroll to **Response Headers**

### Q: Which headers are present?
**A:** Configured in `src/main/java/com/example/LAB10/config/SecurityConfig.java` (Lines 76-84):

| Header | Value | Line |
|--------|-------|------|
| X-Content-Type-Options | nosniff | 78 |
| X-Frame-Options | DENY | 77 |
| Content-Security-Policy | default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline' | 79-80 |
| Referrer-Policy | strict-origin-when-cross-origin | 81 |
| Strict-Transport-Security | max-age=31536000; includeSubDomains | 82-84 |

### Q: What about cookie attributes (HttpOnly, Secure, SameSite)?
**A:** This app uses **JWT in Authorization header** (not cookies), so cookie attributes don't apply. However:
- Tokens are stored in localStorage (client-side)
- Access tokens expire in 15 minutes
- Refresh tokens expire in 24 hours
- If cookies were used, they would be configured in SecurityConfig

---

## 6. Session/Token Management (7 pts)

### Q: How does logout work?
**A:** 
- **Endpoint:** `POST /api/auth/logout`
- **File:** `src/main/java/com/example/LAB10/controller/AuthController.java` (Lines 76-83)
- **Action:** Deletes refresh token from database
- **Demo:** Click "Logout" → Status changes to "Logged Out"

### Q: Does refresh after logout keep user logged out?
**A:** Yes. After logout:
- Access token still works until expiry (15 min) - this is normal for JWT
- Refresh token is **invalidated** in database
- Clicking "Refresh Token" after logout returns error

### Q: How does access token expiration work?
**A:**
- **Expiry:** 15 minutes
- **Config:** `src/main/resources/application.properties` (Line: `jwt.expiration=900000`)
- **Code:** `src/main/java/com/example/LAB10/service/JwtService.java` (Line 34-44)

### Q: How does refresh token rotation work?
**A:**
- **File:** `src/main/java/com/example/LAB10/service/RefreshTokenService.java`
- **Method:** `rotateRefreshToken()` (Lines 48-57)
```java
public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
    refreshTokenRepository.delete(oldToken);  // Delete old token
    return createRefreshToken(oldToken.getUser().getId());  // Create new one
}
```
- Old refresh token is deleted from database
- New refresh token is created and returned

### Q: How to demonstrate old refresh tokens don't work?
**A:**
1. Login and copy the refresh token
2. Click "Refresh Token" (get new tokens)
3. Manually try to use old refresh token → Returns error

---

## 7. Database and Persistence Security (5 pts)

### Q: Where is the user_id foreign key?
**A:** `src/main/resources/db/migration/V2__Create_notes_table_and_alter_users.sql` (Lines 1-8):
```sql
CREATE TABLE IF NOT EXISTS notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Q: Where are prepared statements / safe queries?
**A:** 
1. **Spring Data JPA** - All repository methods use parameterized queries automatically
2. **JdbcTemplate example:** `src/main/java/com/example/LAB10/service/NoteService.java` (Lines 89-91):
```java
String sql = "SELECT COUNT(*) FROM notes WHERE user_id = ?";
Integer count = jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
```

---

## 8. Secure Logging (5 pts)

### Q: Where are failed login attempts logged?
**A:** `src/main/java/com/example/LAB10/exception/GlobalExceptionHandler.java` (Lines 41-49):
```java
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ErrorResponse> handleBadCredentials(...) {
    String clientIp = request.getRemoteAddr();
    log.warn("SECURITY: Failed login attempt from IP: {}", clientIp);
    ...
}
```

### Q: Where are unauthorized access attempts logged?
**A:** Multiple locations:
1. **GlobalExceptionHandler.java** (Lines 51-60):
```java
@ExceptionHandler(AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(...) {
    log.warn("SECURITY: Access denied for IP: {} - {}", clientIp, ex.getMessage());
}
```

2. **JwtAuthenticationFilter.java** (Lines 55-57):
```java
} catch (Exception e) {
    log.warn("SECURITY: JWT validation failed: {}", e.getMessage());
}
```

3. **RateLimitingFilter.java** (Lines 45-47):
```java
log.warn("SECURITY: Rate limit exceeded for IP: {}", clientIp);
```

### Q: Are passwords logged?
**A:** **NO.** Search the codebase - no password values are ever logged.

### Q: Are JWTs/refresh tokens logged?
**A:** **NO.** Only validation failures are logged (generic message), not the actual tokens.

### Q: How to see logs during demo?
**A:** Logs appear in the terminal where `./gradlew bootRun` is running. Example:
```
WARN  SECURITY: Failed login attempt from IP: 0:0:0:0:0:0:0:1
WARN  SECURITY: Rate limit exceeded for IP: 0:0:0:0:0:0:0:1
```

---

## 9. Testing (Core Requirement)

### Q: How to run tests?
**A:**
```bash
./gradlew test
```

### Q: Where are the test files?
**A:** `src/test/java/com/example/LAB10/`
- **Unit tests:** `validator/PasswordPolicyValidatorTest.java`, `service/NoteServiceTest.java`
- **Integration tests:** `controller/Lab10ControllerTest.java`

### Q: What security-related tests exist?
**A:**

1. **Password Validation Tests** (`PasswordPolicyValidatorTest.java`):
   - Tests weak passwords are rejected
   - Tests common passwords are rejected
   - Tests all password requirements

2. **Access Control Tests** (`NoteServiceTest.java`):
   - `getNoteById_shouldThrowAccessDeniedForOtherUsersNote()` (Line 108-114)
   - `updateNote_shouldThrowAccessDeniedForOtherUsersNote()` (Line 133-143)
   - `deleteNote_shouldThrowAccessDeniedForOtherUsersNote()` (Line 162-172)

3. **Integration Tests** (`Lab10ControllerTest.java`):
   - Tests authentication flow
   - Tests protected endpoints

### Q: How to run tests quickly during presentation?
**A:**
```bash
./gradlew test --tests "*.PasswordPolicyValidatorTest"  # Just password tests
./gradlew test --tests "*.NoteServiceTest"               # Just access control tests
./gradlew test                                           # All tests (~10 seconds)
```

---

## 10. Bonus Features (+15 pts)

### Q: Is rate limiting implemented?
**A:** Yes.
- **File:** `src/main/java/com/example/LAB10/security/RateLimitingFilter.java`
- **Limits:** 60 req/min general, 10 req/min for /api/auth/* endpoints
- **Demo:** Click "Spam Login (10x)" in dashboard → Shows HTTP 429 errors

### Q: Is HTTPS enabled?
**A:** Yes.
- **Config:** `src/main/resources/application.properties` (Lines 8-13)
- **Certificate:** Self-signed in `src/main/resources/keystore.p12`
- **Port:** 8443

### Q: Does HTTP redirect to HTTPS?
**A:** Yes.
- **File:** `src/main/java/com/example/LAB10/config/HttpToHttpsRedirectConfig.java`
- **Demo:** Visit http://localhost:8080 → Redirects to https://localhost:8443

### Q: Is HSTS header present?
**A:** Yes.
- **File:** `src/main/java/com/example/LAB10/config/SecurityConfig.java` (Lines 48-51)
- **Value:** `max-age=31536000; includeSubDomains`
- **Demo:** Check response headers in DevTools

### Q: Is GitHub Actions CI configured?
**A:** Yes.
- **File:** `.github/workflows/ci.yml`
- **Steps:** Build → Test → JaCoCo Coverage → OWASP Dependency Check

### Q: Is OWASP Dependency Check configured?
**A:** Yes.
- **Build file:** `build.gradle.kts` (Lines 49-55)
- **CI file:** `.github/workflows/ci.yml` (Lines 53-87)

---

## Quick Demo Script (10 minutes)

1. **Start app** (30 sec): `./gradlew bootRun`
2. **Open dashboard** (10 sec): https://localhost:8443/test-dashboard.html
3. **Register user1** (30 sec): Fill form, click Register
4. **Show validation error** (30 sec): Click "Register with Bad Password"
5. **Login user1** (20 sec): Click Login, show token appears
6. **Create note** (20 sec): Click Create Note
7. **Logout** (10 sec): Click Logout
8. **Register user2** (30 sec): Change username/email, Register
9. **Login user2** (20 sec): Login
10. **Data isolation attack** (60 sec): Try Access/Delete/Update note #1 → All show 403
11. **Admin access test** (30 sec): Click "Get All Users" → 403 (not admin)
12. **No-auth test** (20 sec): Click "Access Notes Without Login" → 401
13. **Rate limit test** (30 sec): Click "Spam Login" → Shows 429
14. **Security headers** (60 sec): Open DevTools → Network → Show headers
15. **Show logs** (30 sec): Point to terminal showing SECURITY log entries
16. **Run tests** (60 sec): `./gradlew test` in new terminal
17. **Show code** (remaining time): Walk through key files mentioned above

---

## File Quick Reference

| Feature | File | Key Lines |
|---------|------|-----------|
| Password hashing | `service/UserService.java` | 22 |
| PasswordEncoder bean | `config/ApplicationConfig.java` | 33-36 |
| JWT generation | `service/JwtService.java` | 30-42 |
| JWT validation filter | `security/JwtAuthenticationFilter.java` | 35-55 |
| Data isolation check | `service/NoteService.java` | 57-59, 67-69, 77-79 |
| Security headers | `config/SecurityConfig.java` | 37-51 |
| Rate limiting | `security/RateLimitingFilter.java` | 39-65 |
| Security logging | `exception/GlobalExceptionHandler.java` | All |
| Refresh token rotation | `service/RefreshTokenService.java` | 52-63 |
| Admin authorization | `controller/AdminController.java` | 23, 39, 48 |
| DTO validation | `dto/RegisterRequest.java` | 11-22 |
| Custom validators | `validator/PasswordPolicyValidator.java` | All |
| User_id FK migration | `db/migration/V2__*.sql` | 1-8 |
| HTTPS config | `application.properties` | 8-13 |
| HTTP→HTTPS redirect | `config/HttpToHttpsRedirectConfig.java` | All |
| CI Pipeline | `.github/workflows/ci.yml` | All |

---

## Common Questions Teachers Ask

**Q: Why JWT instead of session cookies?**
A: JWT is stateless - server doesn't need to store sessions. Better for scalability and REST APIs.

**Q: What happens if someone steals a JWT?**
A: They have access until it expires (15 min). That's why we use short-lived access tokens and refresh token rotation.

**Q: Why is the refresh token longer-lived?**
A: For user convenience - they don't need to login every 15 minutes. Refresh tokens are stored securely in database and rotated on use.

**Q: What's the difference between authentication and authorization?**
A: Authentication = "Who are you?" (login/JWT). Authorization = "What can you do?" (@PreAuthorize, role checks).

**Q: Why bcrypt and not plain hashing like SHA-256?**
A: BCrypt has built-in salt and is intentionally slow (configurable), making brute-force attacks harder.

**Q: What does the strength parameter (12) in BCrypt mean?**
A: It's the "cost factor" - 2^12 iterations. Higher = slower = more secure, but also slower login.

**Q: Why do we need HTTPS?**
A: To encrypt data in transit. Without it, JWTs and passwords could be intercepted on the network.

**Q: What does HSTS do?**
A: Tells browsers to always use HTTPS for this site, even if user types http://.

**Q: What's the point of rate limiting?**
A: Prevents brute-force attacks, DoS attacks, and credential stuffing.

**Q: Why use prepared statements?**
A: Prevents SQL injection by separating SQL code from data.
