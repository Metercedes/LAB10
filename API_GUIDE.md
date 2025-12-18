# LAB10 API Quick Reference Guide 🚀

## ✅ Server Status
**Currently Running** on http://localhost:8080

---

## 🔧 Server Management

### Check if Server is Running
```bash
curl http://localhost:8080/hello
```
**Expected**: `Hello, user!`

### View Server Logs
```bash
tail -f server.log
```

### Stop the Server
```bash
pkill -f "java.*LAB10"
```

### Start the Server (Stable - Won't Auto-Restart)
```bash
cd /Users/meteyalcinkaya/IdeaProjects/LAB10
nohup ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false" > server.log 2>&1 &
```

**⚠️ IMPORTANT**: Wait 15-20 seconds after starting before making requests!

---

## 📍 Available Endpoints

### 1. Home Page
```bash
curl http://localhost:8080/
```
**Browser**: http://localhost:8080/

---

### 2. Hello Endpoint
```bash
curl http://localhost:8080/hello
```
**Browser**: http://localhost:8080/hello

---

### 3. Register a New User
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your_username",
    "email": "your@email.com",
    "password": "YourPassword123"
  }'
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@test.com","password":"MyPass123"}'
```

**Success Response**:
```json
{
  "username": "john",
  "email": "john@test.com",
  "password": "$2a$10$...",
  "id": 1
}
```

---

### 4. Get All Users Info
```bash
curl http://localhost:8080/api/users/info
```
**Browser**: http://localhost:8080/api/users/info

**Response**:
```json
[
  {
    "id": 1,
    "email": "test@test.com",
    "username": "testuser"
  },
  {
    "id": 2,
    "email": "alice@test.com",
    "username": "alice"
  }
]
```
**Note**: Passwords are NOT included in the response for security.

---

### 5. Login
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "your@email.com",
    "password": "YourPassword123"
  }'
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@test.com","password":"MyPass123"}'
```

**Success Response**:
```json
{
  "message": "Login successful"
}
```

**Failure Response**:
```json
{
  "error": "Invalid credentials"
}
```

---

## 🌐 Using with Postman

### Register User (POST)
1. Method: **POST**
2. URL: `http://localhost:8080/api/users/register`
3. Headers:
   - `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "Test123!"
}
```

### Login (POST)
1. Method: **POST**
2. URL: `http://localhost:8080/api/users/login`
3. Headers:
   - `Content-Type: application/json`
4. Body (raw JSON):
```json
{
  "email": "test@example.com",
  "password": "Test123!"
}
```

---

## 📊 Database Information

- **Type**: SQLite
- **Location**: `database.db` in project root
- **Password Encryption**: BCrypt (secure hashing)
- **Migrations**: Managed by Flyway

### View Database
```bash
sqlite3 database.db "SELECT * FROM users;"
```

---

## ❗ Troubleshooting

### Server Won't Start
```bash
# Kill any existing process
pkill -f "java.*LAB10"

# Wait 2 seconds
sleep 2

# Start fresh
cd /Users/meteyalcinkaya/IdeaProjects/LAB10
nohup ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false" > server.log 2>&1 &

# Wait for startup
sleep 20

# Test
curl http://localhost:8080/hello
```

### Connection Refused
- Server needs 15-20 seconds to start
- Check logs: `tail -f server.log`
- Verify it's running: `ps aux | grep LAB10`

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill it
kill -9 <PID>
```

---

## 🎯 Quick Test Workflow

1. **Start Server**:
   ```bash
   cd /Users/meteyalcinkaya/IdeaProjects/LAB10
   nohup ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=false" > server.log 2>&1 &
   ```

2. **Wait for Startup**:
   ```bash
   sleep 20
   ```

3. **Test Connection**:
   ```bash
   curl http://localhost:8080/hello
   ```

4. **Register User**:
   ```bash
   curl -X POST http://localhost:8080/api/users/register \
     -H "Content-Type: application/json" \
     -d '{"username":"demo","email":"demo@test.com","password":"Demo123"}'
   ```

5. **Login**:
   ```bash
   curl -X POST http://localhost:8080/api/users/login \
     -H "Content-Type: application/json" \
     -d '{"email":"demo@test.com","password":"Demo123"}'
   ```

---

## 📝 Notes

- **Server runs in background** using `nohup` - won't stop when you close terminal
- **Auto-restart is DISABLED** - server stays stable
- **Logs are saved** to `server.log` file
- **Database persists** between restarts
- **All endpoints allow** anonymous access (no authentication required)
