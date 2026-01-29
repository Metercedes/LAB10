# Lab 14: Testing, CI/CD & Security Gatekeeping

This project demonstrates a robust testing strategy and a secure CI/CD pipeline for a Spring Boot application. It focuses on automated unit testing, integration testing with containers, code coverage reporting, and security vulnerability scanning.

##  Lab Goals
The main objectives of this implementation are:
1.  [cite_start]**Automated Testing:** Verify code correctness using Unit and Integration tests[cite: 7, 26].
2.  [cite_start]**CI Pipeline:** Establish a GitHub Actions workflow to run checks on every push[cite: 39].
3.  [cite_start]**Security Gatekeeping:** Prevent vulnerable dependencies from merging into the codebase[cite: 58, 59].

---

##  Testing Strategy

### 1. Unit Tests
[cite_start]Unit tests focus on testing individual components in isolation without loading the full Spring context[cite: 8].
* **Tools:** JUnit 5, Mockito.
* **Scope:**
    * [cite_start]**Service Layer:** Verifies business logic (e.g., `UserService`) by mocking repositories[cite: 13, 14].
    * [cite_start]**Validation:** Ensures input validation rules (e.g., password constraints) reject invalid data[cite: 17, 18].
    * [cite_start]**Authentication Logic:** Tests login flows independently of the server[cite: 22].

### 2. Integration Tests
[cite_start]Integration tests verify that multiple components work together in a running application context[cite: 27].
* [cite_start]**Tools:** Spring Boot Test, Testcontainers (PostgreSQL), Spring Security Test[cite: 88, 105].
* **Scope:**
    * [cite_start]**Secured Endpoints:** Simulates HTTP requests to verify access control (Authorized vs. Unauthorized)[cite: 29, 31].
    * [cite_start]**CSRF Protection:** Ensures requests without valid CSRF tokens are rejected[cite: 35].
    * [cite_start]**Database Interactions:** Uses a real PostgreSQL instance via Testcontainers to match production behavior[cite: 107].

---

##  CI/CD Pipeline (GitHub Actions)

This project uses a Continuous Integration pipeline defined in `.github/workflows/maven.yml`. [cite_start]The pipeline triggers on every push and pull request[cite: 39].

### Pipeline Stages:
1.  [cite_start]**Build & Test:** Compiles the code and runs all unit and integration tests[cite: 41].
2.  [cite_start]**Quality Check (JaCoCo):** Generates a code coverage report to measure tested code percentage[cite: 46].
3.  **Security Scanning (OWASP Dependency Check):**
    * [cite_start]Scans third-party libraries for known vulnerabilities (CVEs)[cite: 54, 56].
    * [cite_start]**Gatekeeping:** The build is configured to **fail** if dependencies with High or Critical severity vulnerabilities (CVSS score ≥ 7.0) are detected.

---

## 🛠 Tech Stack & Dependencies
* **Java 17+ / Spring Boot 3.x**
* [cite_start]**JUnit 5 & Mockito:** For unit testing[cite: 96].
* [cite_start]**Testcontainers:** For integration testing with Docker[cite: 103].
* [cite_start]**JaCoCo:** For code coverage analysis[cite: 46].
* [cite_start]**OWASP Dependency Check:** For security scanning in CI[cite: 54].

##  Reports
After a successful (or failed) pipeline run, the following artifacts are available in GitHub Actions:
* `jacoco-report`: Code coverage details.
* `dependency-check-report`: Security vulnerability analysis HTML report.